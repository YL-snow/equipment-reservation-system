-- 设备表
CREATE TABLE equipment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL COMMENT '设备名称',
    model           VARCHAR(100)    NOT NULL COMMENT '型号/规格',
    total_quantity  INT             NOT NULL COMMENT '总库存',
    available_qty   INT             NOT NULL COMMENT '当前可用库存',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1-启用 0-停用',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_model (name, model)
) COMMENT='设备/耗材表';

-- 预约单表
CREATE TABLE reservation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id    BIGINT          NOT NULL COMMENT '设备ID',
    applicant       VARCHAR(50)     NOT NULL COMMENT '申请人',
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
    CONSTRAINT fk_res_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id)
) COMMENT='预约单表';

-- 库存日志表
CREATE TABLE inventory_log (
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
