-- ============================================================
-- Schema: equipment-reservation-system
-- Description: 实验室设备预约与耗材管理系统 - 建表脚本
-- Database: ers
-- Character: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS ers DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ers;

-- ============================================================
-- 1. 设备分类表
-- ============================================================
CREATE TABLE IF NOT EXISTS equipment_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL COMMENT '分类名称',
    description VARCHAR(200)    DEFAULT NULL COMMENT '分类描述',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='设备分类表';

-- ============================================================
-- 2. 设备/耗材主表
-- ============================================================
CREATE TABLE IF NOT EXISTS equipment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL COMMENT '设备名称',
    model           VARCHAR(100)    NOT NULL COMMENT '型号/规格',
    code            VARCHAR(50)     DEFAULT NULL COMMENT '设备编号',
    total_quantity  INT             NOT NULL COMMENT '总库存',
    available_qty   INT             NOT NULL COMMENT '当前可用库存',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1-启用 0-停用',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    category_id     BIGINT          DEFAULT NULL COMMENT '分类ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_model (name, model),
    INDEX idx_category (category_id),
    CONSTRAINT fk_equip_category FOREIGN KEY (category_id) REFERENCES equipment_category(id)
) COMMENT='设备/耗材表';

-- ============================================================
-- 3. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           VARCHAR(50)     NOT NULL COMMENT '学号/工号',
    name              VARCHAR(50)     NOT NULL COMMENT '姓名',
    password          VARCHAR(255)    NOT NULL COMMENT '密码(Bcrypt加密)',
    role              VARCHAR(20)     NOT NULL DEFAULT 'STUDENT'
                      COMMENT 'STUDENT-学生 ADMIN-管理员 SYSTEM-系统',
    is_blacklisted    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否黑名单',
    blacklisted_until DATETIME        DEFAULT NULL COMMENT '黑名单截止时间',
    overdue_count     INT             NOT NULL DEFAULT 0 COMMENT '逾期次数',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) COMMENT='用户表';

-- ============================================================
-- 4. 预约单表
-- ============================================================
CREATE TABLE IF NOT EXISTS reservation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id    BIGINT          NOT NULL COMMENT '设备ID',
    applicant       VARCHAR(50)     NOT NULL COMMENT '申请人姓名',
    user_id         BIGINT          DEFAULT NULL COMMENT '申请人用户ID',
    quantity        INT             NOT NULL COMMENT '预约数量',
    start_time      DATETIME        NOT NULL COMMENT '预约开始时间',
    end_time        DATETIME        NOT NULL COMMENT '预约结束时间',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING-待审批 APPROVED-已通过 REJECTED-已驳回 RETURNED-已归还 CANCELLED-已取消',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_equip_time (equipment_id, start_time, end_time),
    INDEX idx_status (status),
    INDEX idx_user (user_id),
    CONSTRAINT fk_res_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id)
) COMMENT='预约单表';

-- ============================================================
-- 5. 库存变更流水表
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id    BIGINT          NOT NULL COMMENT '设备ID',
    reservation_id  BIGINT          DEFAULT NULL COMMENT '关联预约ID',
    change_type     VARCHAR(20)     NOT NULL COMMENT 'DEDUCT-扣减 RETURN-归还',
    qty_before      INT             NOT NULL COMMENT '变更前可用库存',
    qty_after       INT             NOT NULL COMMENT '变更后可用库存',
    operator        VARCHAR(50)     NOT NULL COMMENT '操作人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_equipment (equipment_id),
    INDEX idx_reservation (reservation_id),
    CONSTRAINT fk_log_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_log_res FOREIGN KEY (reservation_id) REFERENCES reservation(id)
) COMMENT='库存变更流水表';
