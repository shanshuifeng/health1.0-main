-- =============================================
-- 健康体检管理系统 - 数据库初始化脚本
-- 版本: 1.0
-- 日期: 2026-07-07
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS healthsys CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE healthsys;

-- =============================================
-- 1. 认证相关表
-- =============================================

-- 1.1 普通用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一标识',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号（登录账号，AES加密存储）',
    password_hash VARCHAR(255) NOT NULL COMMENT 'AES加密密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号（AES加密）',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    birth_date DATE COMMENT '出生日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
    first_login BOOLEAN DEFAULT TRUE COMMENT '首次登录（需改密）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='普通用户表';

-- 1.2 医生表
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生唯一标识',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '工号（登录账号）',
    password_hash VARCHAR(255) NOT NULL COMMENT 'AES加密密码',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    department VARCHAR(100) COMMENT '所属科室',
    title VARCHAR(50) COMMENT '职称（主治/副主任等）',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-在职 0-离职',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生表';

-- 1.3 管理员表
CREATE TABLE IF NOT EXISTS admins (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员唯一标识',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(255) NOT NULL COMMENT 'AES加密密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) DEFAULT 'MANAGER' COMMENT '角色: SUPER_ADMIN / MANAGER',
    phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- =============================================
-- 2. 业务核心表
-- =============================================

-- 2.1 检查组表
CREATE TABLE IF NOT EXISTS check_groups (
    group_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '检查组唯一标识',
    group_name VARCHAR(100) NOT NULL COMMENT '检查组名称',
    description TEXT COMMENT '检查组描述',
    price DECIMAL(10,2) NOT NULL COMMENT '检查组总价',
    daily_limit INT DEFAULT 50 COMMENT '每日可预约名额上限',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查组表';

-- 2.2 检查项表
CREATE TABLE IF NOT EXISTS check_items (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '检查项唯一标识',
    item_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    code VARCHAR(50) UNIQUE COMMENT '项目编码',
    category VARCHAR(100) COMMENT '所属科室/分类',
    unit VARCHAR(20) COMMENT '计量单位',
    reference_range VARCHAR(255) COMMENT '正常参考值范围',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '单项价格',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查项表';

-- 2.3 检查组-检查项关联表
CREATE TABLE IF NOT EXISTS group_item_relation (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联唯一标识',
    group_id BIGINT NOT NULL COMMENT '检查组ID',
    item_id BIGINT NOT NULL COMMENT '检查项ID',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_group_item (group_id, item_id) COMMENT '联合唯一索引，避免重复绑定',
    CONSTRAINT fk_gir_group FOREIGN KEY (group_id) REFERENCES check_groups(group_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_gir_item FOREIGN KEY (item_id) REFERENCES check_items(item_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查组-检查项关联表';

-- 2.4 预约表
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    group_id BIGINT NOT NULL COMMENT '检查组ID',
    appointment_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约创建时间',
    exam_date DATE NOT NULL COMMENT '体检预约日期',
    exam_time_slot VARCHAR(20) COMMENT '时段（上午/下午）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/CONFIRMED/COMPLETED/CANCELLED',
    payment_status BOOLEAN DEFAULT FALSE COMMENT '支付状态: FALSE-未支付 TRUE-已支付',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_app_user_date (user_id, exam_date) COMMENT '加速用户预约列表查询',
    INDEX idx_app_status (status) COMMENT '加速状态筛选',
    INDEX idx_app_exam_date (exam_date) COMMENT '加速医生今日预约列表查询',
    CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_group FOREIGN KEY (group_id) REFERENCES check_groups(group_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- 2.5 检查结果明细表
CREATE TABLE IF NOT EXISTS check_results (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果唯一标识',
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    item_id BIGINT NOT NULL COMMENT '检查项ID',
    doctor_id BIGINT NOT NULL COMMENT '录入医生ID',
    result_value VARCHAR(500) COMMENT '检查结果值',
    is_abnormal BOOLEAN DEFAULT FALSE COMMENT '是否异常: TRUE-异常 FALSE-正常',
    doctor_note TEXT COMMENT '医生备注',
    exam_date DATETIME COMMENT '体检执行时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_appointment_item (appointment_id, item_id) COMMENT '联合唯一索引，防止重复录入',
    INDEX idx_result_abnormal (is_abnormal) COMMENT '加速异常项快速筛选',
    CONSTRAINT fk_result_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_result_item FOREIGN KEY (item_id) REFERENCES check_items(item_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_result_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查结果明细表';

-- 2.6 体检报告表
CREATE TABLE IF NOT EXISTS reports (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告唯一标识',
    appointment_id BIGINT NOT NULL COMMENT '关联预约（一对一）',
    doctor_id BIGINT NOT NULL COMMENT '上传/审核医生ID',
    pdf_file_path VARCHAR(500) COMMENT 'PDF文件存储路径/URL',
    summary TEXT COMMENT '报告总结/医生综合建议',
    upload_time DATETIME COMMENT '上传时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_report_appointment (appointment_id) COMMENT '唯一约束，一次预约只生成一份报告',
    CONSTRAINT fk_report_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_report_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体检报告表';

-- =============================================
-- 3. 初始化数据
-- =============================================

-- 插入默认管理员（密码: admin123，使用EncryptUtil加密）
INSERT INTO admins (username, password_hash, real_name, role)
VALUES ('admin', 'admin123', '超级管理员', 'SUPER_ADMIN')
ON DUPLICATE KEY UPDATE username = username;

-- 插入示例检查项
INSERT INTO check_items (item_name, code, category, unit, reference_range, price) VALUES
('身高', 'HT001', '一般检查', 'cm', '100-200', 5.00),
('体重', 'WT001', '一般检查', 'kg', '30-150', 5.00),
('血压（收缩压）', 'BP001', '一般检查', 'mmHg', '90-140', 10.00),
('血压（舒张压）', 'BP002', '一般检查', 'mmHg', '60-90', 10.00),
('心率', 'HR001', '一般检查', '次/分', '60-100', 10.00),
('血常规-白细胞', 'BC001', '血液检查', '×10⁹/L', '4.0-10.0', 20.00),
('血常规-红细胞', 'BC002', '血液检查', '×10¹²/L', '3.5-5.5', 20.00),
('血红蛋白', 'BC003', '血液检查', 'g/L', '120-160', 20.00),
('空腹血糖', 'GLU001', '生化检查', 'mmol/L', '3.9-6.1', 25.00),
('总胆固醇', 'TC001', '生化检查', 'mmol/L', '3.1-5.7', 25.00),
('甘油三酯', 'TG001', '生化检查', 'mmol/L', '0.4-1.8', 25.00),
('谷丙转氨酶(ALT)', 'ALT001', '肝功能', 'U/L', '0-40', 30.00),
('谷草转氨酶(AST)', 'AST001', '肝功能', 'U/L', '0-40', 30.00),
('肌酐', 'CR001', '肾功能', 'μmol/L', '44-133', 30.00),
('尿素氮', 'BUN001', '肾功能', 'mmol/L', '2.9-8.2', 30.00),
('尿常规-蛋白', 'UR001', '尿常规', '-', '阴性', 15.00),
('尿常规-葡萄糖', 'UR002', '尿常规', '-', '阴性', 15.00),
('心电图', 'ECG001', '心电检查', '-', '正常心电图', 50.00),
('胸部X光', 'XRAY001', '影像检查', '-', '未见异常', 80.00),
('腹部B超', 'US001', '影像检查', '-', '未见异常', 100.00)
ON DUPLICATE KEY UPDATE code = code;

-- 插入示例检查组
INSERT INTO check_groups (group_name, description, price, daily_limit) VALUES
('基础体检套餐', '包含身高、体重、血压、心率等一般检查项目', 80.00, 50),
('标准体检套餐', '包含一般检查+血液检查+生化检查', 280.00, 40),
('全面体检套餐', '包含全部检查项目，最全面的健康体检', 580.00, 30),
('入职体检套餐', '包含基础项目和常规检查，满足入职体检需求', 120.00, 60)
ON DUPLICATE KEY UPDATE group_name = group_name;

-- 示例检查组与检查项关联
-- 基础体检套餐 (group_id=1): 身高、体重、血压、心率
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 标准体检套餐 (group_id=2): 基础+血液+生化
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(2, 1, 1), (2, 2, 2), (2, 3, 3), (2, 4, 4), (2, 5, 5),
(2, 6, 6), (2, 7, 7), (2, 8, 8), (2, 9, 9), (2, 10, 10),
(2, 11, 11), (2, 12, 12), (2, 13, 13), (2, 14, 14), (2, 15, 15)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 全面体检套餐 (group_id=3): 所有检查项
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(3, 1, 1), (3, 2, 2), (3, 3, 3), (3, 4, 4), (3, 5, 5),
(3, 6, 6), (3, 7, 7), (3, 8, 8), (3, 9, 9), (3, 10, 10),
(3, 11, 11), (3, 12, 12), (3, 13, 13), (3, 14, 14), (3, 15, 15),
(3, 16, 16), (3, 17, 17), (3, 18, 18), (3, 19, 19), (3, 20, 20)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 入职体检套餐 (group_id=4): 基础+心电图+胸部X光
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(4, 1, 1), (4, 2, 2), (4, 3, 3), (4, 4, 4), (4, 5, 5),
(4, 18, 6), (4, 19, 7)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
