CREATE TABLE `equipment_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_category_name` (`name`)
) COMMENT='设备分类表';

INSERT INTO `equipment_category` (`name`, `description`) VALUES
('办公设备', '电脑、打印机等办公常用设备'),
('网络设备', '路由器、交换机等网络设备'),
('实验仪器', '万用表、示波器等实验测量仪器'),
('影像设备', '相机、摄像机、投影仪等影像设备'),
('存储设备', '硬盘、U盘等存储设备'),
('耗材配件', '打印纸、墨盒、线材等耗材配件'),
('其他设备', '其他未分类设备');

ALTER TABLE `equipment` ADD COLUMN `category_id` BIGINT DEFAULT NULL COMMENT '设备分类ID';
ALTER TABLE `equipment` ADD CONSTRAINT `fk_equip_category` FOREIGN KEY (`category_id`) REFERENCES `equipment_category`(`id`);
ALTER TABLE `equipment` ADD KEY `idx_category_id` (`category_id`);

UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '办公设备') WHERE name IN ('激光打印机', '笔记本电脑', '台式电脑');
UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '网络设备') WHERE name IN ('路由器', '交换机');
UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '实验仪器') WHERE name IN ('万用表', '示波器', '恒温箱', '离心机', '电子天平', '激光测距仪');
UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '影像设备') WHERE name IN ('投影仪', '数码相机', '摄像机', '三脚架');
UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '存储设备') WHERE name IN ('移动硬盘', 'U盘');
UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '耗材配件') WHERE name IN ('碳粉盒', '打印纸', '墨盒', '投影仪灯泡', 'HDMI线');
UPDATE `equipment` SET `category_id` = (SELECT id FROM equipment_category WHERE name = '其他设备') WHERE name IN ('录音笔');
