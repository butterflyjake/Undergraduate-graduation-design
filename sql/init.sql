-- ============================================================
-- 高校AED器械管理系统 数据库初始化脚本
-- 数据库: aed_system   字符集: utf8mb4
-- 兼容 MySQL 5.5+ / 8.0 (未使用高版本专有语法)
-- 执行方式: mysql -uroot -p --default-character-set=utf8mb4 < init.sql
-- ============================================================

DROP DATABASE IF EXISTS aed_system;
CREATE DATABASE aed_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE aed_system;

-- ------------------------------------------------------------
-- 1. 角色表
-- ------------------------------------------------------------
CREATE TABLE sys_role (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码: admin-系统管理员, device_manager-设备管理员, inspector-巡检人员',
  role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
  description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ------------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------------
CREATE TABLE sys_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  password    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密存储)',
  real_name   VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
  phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  role_id     BIGINT       NOT NULL COMMENT '角色ID',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 3. 楼宇表
-- ------------------------------------------------------------
CREATE TABLE building (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '楼宇ID',
  building_name VARCHAR(100) NOT NULL COMMENT '楼宇名称',
  campus_area   VARCHAR(100) DEFAULT NULL COMMENT '所属校区',
  address       VARCHAR(200) DEFAULT NULL COMMENT '位置描述',
  remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼宇表';

-- ------------------------------------------------------------
-- 4. AED设备表
-- ------------------------------------------------------------
CREATE TABLE aed_device (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  device_code   VARCHAR(50)  NOT NULL COMMENT '设备编号, 如 AED-001',
  device_name   VARCHAR(100) NOT NULL COMMENT '设备型号, 如 迈瑞 BeneHeart D1',
  brand         VARCHAR(50)  DEFAULT NULL COMMENT '品牌',
  serial_no     VARCHAR(100) DEFAULT NULL COMMENT '序列号',
  manufacturer  VARCHAR(100) DEFAULT NULL COMMENT '生产厂家',
  purchase_date DATE         DEFAULT NULL COMMENT '购置日期',
  service_life  INT          DEFAULT NULL COMMENT '设计使用年限(年)',
  building_id   BIGINT       DEFAULT NULL COMMENT '所在楼宇ID',
  floor         VARCHAR(20)  DEFAULT NULL COMMENT '所在楼层',
  position_desc VARCHAR(200) DEFAULT NULL COMMENT '具体位置描述',
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-在用 1-维修中 2-停用 3-已报废',
  photo         VARCHAR(255) DEFAULT NULL COMMENT '设备照片URL',
  remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_code (device_code),
  KEY idx_building_id (building_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AED设备表';

-- ------------------------------------------------------------
-- 5. 设备配件表(电极片/电池)
-- ------------------------------------------------------------
CREATE TABLE device_part (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配件ID',
  device_id    BIGINT       NOT NULL COMMENT '所属设备ID',
  part_type    TINYINT      NOT NULL COMMENT '配件类型: 1-电极片 2-电池',
  batch_no     VARCHAR(50)  DEFAULT NULL COMMENT '批次号',
  install_date DATE         DEFAULT NULL COMMENT '安装日期',
  expire_date  DATE         NOT NULL COMMENT '有效期至',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-使用中 1-已更换',
  remark       VARCHAR(200) DEFAULT NULL COMMENT '备注',
  create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  KEY idx_device_id (device_id),
  KEY idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备配件表(电极片/电池)';

-- ------------------------------------------------------------
-- 6. 巡检任务表
-- ------------------------------------------------------------
CREATE TABLE inspection_task (
  id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  device_id     BIGINT   NOT NULL COMMENT '设备ID',
  inspector_id  BIGINT   NOT NULL COMMENT '巡检人员ID',
  plan_date     DATE     NOT NULL COMMENT '计划巡检日期',
  status        TINYINT  NOT NULL DEFAULT 0 COMMENT '状态: 0-待巡检 1-已完成 2-已逾期 3-已取消',
  result        TINYINT  DEFAULT NULL COMMENT '巡检结果: 1-正常 2-异常',
  complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
  create_time   DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time   DATETIME DEFAULT NULL COMMENT '更新时间',
  deleted       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  KEY idx_device_id (device_id),
  KEY idx_inspector_id (inspector_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检任务表';

-- ------------------------------------------------------------
-- 7. 巡检记录表
-- ------------------------------------------------------------
CREATE TABLE inspection_record (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  task_id      BIGINT       NOT NULL COMMENT '关联任务ID',
  device_id    BIGINT       NOT NULL COMMENT '设备ID',
  inspector_id BIGINT       NOT NULL COMMENT '巡检人ID',
  inspect_time DATETIME     NOT NULL COMMENT '巡检时间',
  result       TINYINT      NOT NULL COMMENT '巡检结果: 1-正常 2-异常',
  summary      VARCHAR(500) DEFAULT NULL COMMENT '总体说明',
  create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_id (task_id),
  KEY idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检记录表';

-- ------------------------------------------------------------
-- 8. 巡检明细表
-- ------------------------------------------------------------
CREATE TABLE inspection_item (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  record_id   BIGINT       NOT NULL COMMENT '关联巡检记录ID',
  item_name   VARCHAR(100) NOT NULL COMMENT '检查项名称',
  item_result TINYINT      NOT NULL COMMENT '检查结果: 1-正常 2-异常',
  remark      VARCHAR(200) DEFAULT NULL COMMENT '异常说明',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  KEY idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检明细表';

-- ------------------------------------------------------------
-- 9. 维修工单表
-- ------------------------------------------------------------
CREATE TABLE repair_order (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  order_no      VARCHAR(50)   NOT NULL COMMENT '工单编号, 如 WX20261028153000123',
  device_id     BIGINT        NOT NULL COMMENT '设备ID',
  report_type   TINYINT       NOT NULL COMMENT '报修来源: 1-巡检异常 2-手动报修',
  reporter_id   BIGINT        NOT NULL COMMENT '报修人ID',
  fault_desc    VARCHAR(500)  NOT NULL COMMENT '故障描述',
  fault_photo   VARCHAR(255)  DEFAULT NULL COMMENT '故障照片URL',
  status        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-维修中 2-已完成 3-已驳回',
  handler_id    BIGINT        DEFAULT NULL COMMENT '处理人ID',
  repair_desc   VARCHAR(500)  DEFAULT NULL COMMENT '维修说明',
  repair_cost   DECIMAL(10,2) DEFAULT NULL COMMENT '维修费用(元)',
  repair_time   DATETIME      DEFAULT NULL COMMENT '完成维修时间',
  reject_reason VARCHAR(200)  DEFAULT NULL COMMENT '驳回原因',
  create_time   DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_time   DATETIME      DEFAULT NULL COMMENT '更新时间',
  deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_device_id (device_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单表';

-- ------------------------------------------------------------
-- 10. AED使用记录表
-- ------------------------------------------------------------
CREATE TABLE usage_record (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  device_id     BIGINT       NOT NULL COMMENT '设备ID',
  occur_time    DATETIME     NOT NULL COMMENT '使用发生时间',
  location_desc VARCHAR(200) DEFAULT NULL COMMENT '事发地点描述',
  patient_desc  VARCHAR(200) DEFAULT NULL COMMENT '患者情况(匿名描述, 不记录隐私信息)',
  result_desc   VARCHAR(500) DEFAULT NULL COMMENT '处置结果',
  recorder_id   BIGINT       NOT NULL COMMENT '登记人ID',
  remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  KEY idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AED使用记录表';

-- ------------------------------------------------------------
-- 11. 配件到期预警表
-- ------------------------------------------------------------
CREATE TABLE expiry_warning (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  part_id        BIGINT       NOT NULL COMMENT '配件ID',
  device_id      BIGINT       NOT NULL COMMENT '设备ID',
  warning_type   TINYINT      NOT NULL COMMENT '预警类型: 1-临期(30天内到期) 2-已过期',
  days_remaining INT          NOT NULL COMMENT '剩余天数(已过期为负数)',
  warning_date   DATE         NOT NULL COMMENT '预警生成日期',
  status         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-未处理 1-已处理',
  handle_desc    VARCHAR(200) DEFAULT NULL COMMENT '处理说明',
  handle_time    DATETIME     DEFAULT NULL COMMENT '处理时间',
  create_time    DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time    DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id),
  KEY idx_device_id (device_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件到期预警表';

-- ------------------------------------------------------------
-- 12. 公告表
-- ------------------------------------------------------------
CREATE TABLE notice (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title        VARCHAR(200) NOT NULL COMMENT '标题',
  content      TEXT         NOT NULL COMMENT '内容',
  notice_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '类型: 1-系统公告 2-急救科普',
  publisher_id BIGINT       NOT NULL COMMENT '发布人ID',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-已发布 0-草稿',
  publish_time DATETIME     DEFAULT NULL COMMENT '发布时间',
  create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ------------------------------------------------------------
-- 13. 操作日志表
-- ------------------------------------------------------------
CREATE TABLE operation_log (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  user_id     BIGINT        DEFAULT NULL COMMENT '操作用户ID',
  username    VARCHAR(50)   DEFAULT NULL COMMENT '操作用户名',
  operation   VARCHAR(100)  DEFAULT NULL COMMENT '操作描述(控制器.方法名)',
  method      VARCHAR(200)  DEFAULT NULL COMMENT '请求方式与地址',
  params      VARCHAR(1000) DEFAULT NULL COMMENT '请求参数(JSON, 最多1000字符)',
  ip          VARCHAR(50)   DEFAULT NULL COMMENT '请求IP',
  create_time DATETIME      DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ------------------------------------------------------------
-- 初始数据: 3 个角色
-- (默认管理员 admin/123456 由系统首次启动时自动创建)
-- ------------------------------------------------------------
INSERT INTO sys_role (role_code, role_name, description, create_time) VALUES
('admin',          '系统管理员', '拥有系统全部权限', NOW()),
('device_manager', '设备管理员', '负责设备台账、配件、维修工单与预警处理', NOW()),
('inspector',      '巡检人员',   '负责设备日常巡检与故障上报', NOW());
