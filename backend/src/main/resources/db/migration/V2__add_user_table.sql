CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` VARCHAR(50) NOT NULL COMMENT '学号或工号，唯一标识',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT-学生 ADMIN-管理员 SYSTEM-系统用户',
    `is_blacklisted` TINYINT NOT NULL DEFAULT 0 COMMENT '0-正常 1-失信人员',
    `blacklisted_until` DATETIME DEFAULT NULL COMMENT '失信到期时间',
    `overdue_count` INT NOT NULL DEFAULT 0 COMMENT '逾期归还次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_role` (`role`),
    KEY `idx_is_blacklisted` (`is_blacklisted`)
) COMMENT='用户表';

INSERT INTO `user` (`user_id`, `name`, `password`, `role`) VALUES
('admin', '管理员', '$2a$10$bElBxjPwM0FmAiNeL2OM0.Xv2fNvHJKtCV7zkHqwBxfYo7sxvCi9u', 'ADMIN'),
('system', '系统用户', '$2a$10$bElBxjPwM0FmAiNeL2OM0.Xv2fNvHJKtCV7zkHqwBxfYo7sxvCi9u', 'SYSTEM');

ALTER TABLE `reservation` ADD COLUMN `user_id` BIGINT NOT NULL DEFAULT 2 COMMENT '关联用户ID，默认关联系统用户';
ALTER TABLE `reservation` ADD CONSTRAINT `fk_res_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE;
ALTER TABLE `reservation` ADD KEY `idx_res_user_id` (`user_id`);
