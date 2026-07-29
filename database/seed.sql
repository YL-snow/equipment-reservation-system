-- ============================================================
-- Seed data: equipment-reservation-system
-- Description: 设备预约管理系统 - 初始数据
-- ============================================================

USE ers;

-- ============================================================
-- 1. 设备分类数据
-- ============================================================
INSERT INTO equipment_category (name) VALUES
    ('办公设备'),
    ('网络设备'),
    ('实验仪器'),
    ('影像设备'),
    ('存储设备'),
    ('耗材配件'),
    ('其他设备');

-- ============================================================
-- 2. 设备/耗材数据
-- ============================================================
INSERT INTO equipment (name, model, total_quantity, available_qty, status, category_id) VALUES
    ('激光打印机',   'HP LaserJet Pro M404dn',  10, 10, 1, 1),
    ('投影仪',       'Epson EB-FH06',            5,  5,  1, 4),
    ('碳粉盒',       'HP 78A 原装黑色硒鼓',      20, 20, 1, 6),
    ('笔记本电脑',   'Dell XPS 13',              5,  5,  1, 1),
    ('笔记本电脑',   'MacBook Pro',              3,  2,  1, 1),
    ('台式电脑',     'Lenovo ThinkStation',      8,  8,  1, 1),
    ('投影仪',       'EPSON CB-X06',             3,  3,  1, 4),
    ('投影仪',       'BenQ MH560',               2,  1,  1, 4),
    ('数码相机',     'Canon EOS R6',             2,  2,  1, 4),
    ('数码相机',     'Sony A7M4',                3,  3,  1, 4),
    ('摄像机',       'Sony ZV-E10',              2,  2,  1, 4),
    ('三脚架',       'Manfrotto MT055CXPRO3',    5,  5,  1, 4),
    ('路由器',       'Cisco C9200',              4,  4,  1, 2),
    ('交换机',       'H3C S5130',                6,  6,  1, 2),
    ('万用表',       'FLUKE 17B+',              10,  8,  1, 3),
    ('示波器',       'Tektronix TBS1102',        3,  3,  1, 3),
    ('恒温箱',       'Thermo Scientific',        2,  2,  1, 3),
    ('离心机',       'Eppendorf 5810R',          2,  2,  1, 3),
    ('电子天平',     'Mettler Toledo',           5,  4,  1, 3),
    ('激光测距仪',   'Leica DISTO D510',         3,  3,  1, 3),
    ('移动硬盘',     'WD My Passport 2TB',      10,  7,  1, 5),
    ('U盘',          'SanDisk Ultra 128GB',     20, 15, 1, 5),
    ('碳粉盒',       'HP CF280A',               15, 10, 1, 6),
    ('打印纸',       'A4 80g',                  50, 40, 1, 6),
    ('墨盒',         'Canon PG-545',            10,  6,  1, 6),
    ('投影仪灯泡',   'EPSON ELPLP96',            5,  3,  1, 6),
    ('HDMI线',       '绿联 HDMI 2.0 5m',        20, 18, 1, 6),
    ('录音笔',       'Sony ICD-UX570F',          5,  5,  1, 7);

-- ============================================================
-- 3. 用户数据
-- 管理员密码: password (BCrypt加密)
-- 学生密码: 123456Ab (BCrypt加密)
-- ============================================================
INSERT INTO user (user_id, name, password, role, is_blacklisted, overdue_count) VALUES
    ('admin',   '管理员',   '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'ADMIN',   0, 0),
    ('user001', '王校晓',   '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'STUDENT', 0, 0),
    ('user002', '王一晴',   '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'STUDENT', 0, 0),
    ('user003', '张三',     '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'STUDENT', 0, 0),
    ('user004', '李四',     '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'STUDENT', 0, 0),
    ('user005', '王五',     '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'STUDENT', 0, 0);
