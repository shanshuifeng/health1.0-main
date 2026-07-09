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
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号（登录账号）',
    password_hash VARCHAR(255) NOT NULL COMMENT '登录密码（明文）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
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
    password_hash VARCHAR(255) NOT NULL COMMENT '登录密码（明文）',
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
    password_hash VARCHAR(255) NOT NULL COMMENT '登录密码（明文）',
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
    doctor_id BIGINT COMMENT '负责医生ID',
    appointment_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约创建时间',
    exam_date DATE NOT NULL COMMENT '体检预约日期',
    exam_time_slot VARCHAR(20) COMMENT '时段（上午/下午）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/COMPLETED/CANCELLED',
    payment_status BOOLEAN DEFAULT FALSE COMMENT '支付状态: FALSE-未支付 TRUE-已支付',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_app_user_date (user_id, exam_date) COMMENT '加速用户预约列表查询',
    INDEX idx_app_status (status) COMMENT '加速状态筛选',
    INDEX idx_app_exam_date (exam_date) COMMENT '加速医生今日预约列表查询',
    INDEX idx_app_doctor (doctor_id) COMMENT '加速医生预约查询',
    CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_group FOREIGN KEY (group_id) REFERENCES check_groups(group_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL ON UPDATE CASCADE
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

-- ---------------------------------------------
-- 3.1 管理员
-- ---------------------------------------------
-- 密码: admin123（明文存储）
INSERT INTO admins (username, password_hash, real_name, role, phone) VALUES
('admin',  'admin123', '超级管理员', 'SUPER_ADMIN', '13800000000'),
('manager','admin123', '运营经理',   'MANAGER',     '13800000001')
ON DUPLICATE KEY UPDATE username = username;

-- ---------------------------------------------
-- 3.2 医生
-- ---------------------------------------------
INSERT INTO doctors (username, password_hash, name, department, title, status) VALUES
('d001', '123456', '张伟',   '内科',       '主任医师',   1),
('d002', '123456', '李芳',   '外科',       '副主任医师', 1),
('d003', '123456', '王建国', '检验科',     '主管技师',   1),
('d004', '123456', '陈敏',   '放射科',     '主治医师',   1),
('d005', '123456', '刘洋',   '超声科',     '副主任医师', 1),
('d006', '123456', '赵丽华', '心电图室',   '主治医师',   1),
('d007', '123456', '孙明',   '眼科',       '主任医师',   1),
('d008', '123456', '周晓燕', '耳鼻喉科',   '主治医师',   1),
('d009', '123456', '吴国强', '口腔科',     '副主任医师', 1),
('d010', '123456', '郑雪',   '妇科',       '主任医师',   1)
ON DUPLICATE KEY UPDATE username = username;

-- ---------------------------------------------
-- 3.3 检查项（67项，涵盖12个科室）
-- ---------------------------------------------
INSERT INTO check_items (item_name, code, category, unit, reference_range, price) VALUES
-- 一般检查 (item_id: 1-7)
('身高',              'HT001', '一般检查',   'cm',     '100-200',        5.00),
('体重',              'WT001', '一般检查',   'kg',     '30-150',         5.00),
('BMI体质指数',       'BMI01', '一般检查',   'kg/m²',  '18.5-24.0',      5.00),
('收缩压',            'BP001', '一般检查',   'mmHg',   '90-140',         10.00),
('舒张压',            'BP002', '一般检查',   'mmHg',   '60-90',          10.00),
('心率',              'HR001', '一般检查',   '次/分',  '60-100',          10.00),
('体温',              'TMP01', '一般检查',   '℃',      '36.0-37.2',      5.00),

-- 血液常规 (item_id: 8-18)
('白细胞计数(WBC)',   'BC001', '血常规',     '×10⁹/L', '3.5-9.5',      15.00),
('红细胞计数(RBC)',   'BC002', '血常规',     '×10¹²/L','3.8-5.8(男)/3.5-5.1(女)', 15.00),
('血红蛋白(HGB)',     'BC003', '血常规',     'g/L',     '120-160(男)/110-150(女)', 15.00),
('红细胞压积(HCT)',   'BC004', '血常规',     '%',       '40-50(男)/35-45(女)',     15.00),
('血小板计数(PLT)',   'BC005', '血常规',     '×10⁹/L', '125-350',        15.00),
('中性粒细胞百分比',  'BC006', '血常规',     '%',       '40-75',          15.00),
('淋巴细胞百分比',    'BC007', '血常规',     '%',       '20-50',          15.00),
('单核细胞百分比',    'BC008', '血常规',     '%',       '3-10',           15.00),
('嗜酸性粒细胞百分比','BC009', '血常规',     '%',       '0.4-8.0',        15.00),
('嗜碱性粒细胞百分比','BC010', '血常规',     '%',       '0-1',            15.00),
('血沉(ESR)',         'BC011', '血常规',     'mm/h',    '0-20(男)/0-30(女)', 20.00),

-- 生化检查 (item_id: 19-31)
('空腹血糖(GLU)',     'GLU01', '生化检查',   'mmol/L',  '3.9-6.1',        20.00),
('餐后2h血糖',        'GLU02', '生化检查',   'mmol/L',  '<7.8',            25.00),
('糖化血红蛋白(HbA1c)','GLU03','生化检查',   '%',       '4.0-6.0',        60.00),
('总胆固醇(TC)',      'TC001', '生化检查',   'mmol/L',  '2.8-5.7',        20.00),
('甘油三酯(TG)',      'TG001', '生化检查',   'mmol/L',  '0.4-1.8',        20.00),
('高密度脂蛋白(HDL)','HDL001','生化检查',    'mmol/L',  '1.0-1.9(男)/1.1-2.2(女)', 20.00),
('低密度脂蛋白(LDL)','LDL001','生化检查',    'mmol/L',  '<3.4',            20.00),
('总蛋白(TP)',        'TP001', '生化检查',   'g/L',     '60-83',          15.00),
('白蛋白(ALB)',       'ALB01', '生化检查',   'g/L',     '35-55',          15.00),
('球蛋白(GLB)',       'GLB01', '生化检查',   'g/L',     '20-30',          15.00),
('总胆红素(TBIL)',   'TBIL1', '生化检查',   'μmol/L',  '3.4-20.5',       20.00),
('直接胆红素(DBIL)',  'DBIL1','生化检查',    'μmol/L',  '0-6.8',          20.00),
('间接胆红素(IBIL)',  'IBIL1','生化检查',    'μmol/L',  '1.7-13.7',       20.00),

-- 肝功能 (item_id: 32-37)
('谷丙转氨酶(ALT)',   'ALT01', '肝功能',     'U/L',     '9-50(男)/7-40(女)', 25.00),
('谷草转氨酶(AST)',   'AST01', '肝功能',     'U/L',     '15-40(男)/13-35(女)',25.00),
('γ-谷氨酰转肽酶(GGT)','GGT01','肝功能',    'U/L',     '10-60(男)/7-45(女)', 25.00),
('碱性磷酸酶(ALP)',   'ALP01', '肝功能',     'U/L',     '45-125',         20.00),
('总胆汁酸(TBA)',     'TBA01', '肝功能',     'μmol/L',  '0-10',           30.00),
('胆碱酯酶(CHE)',     'CHE01', '肝功能',     'U/L',     '4000-12000',     25.00),

-- 肾功能 (item_id: 38-42)
('尿素氮(BUN)',       'BUN01', '肾功能',     'mmol/L',  '2.9-8.2',        20.00),
('肌酐(CREA)',        'CREA1', '肾功能',     'μmol/L',  '59-104(男)/45-84(女)', 25.00),
('尿酸(UA)',          'UA001', '肾功能',     'μmol/L',  '208-428(男)/155-357(女)', 25.00),
('胱抑素C(CysC)',     'CysC1','肾功能',      'mg/L',    '0.59-1.03',      50.00),
('尿微量白蛋白',      'MA001', '肾功能',     'mg/L',    '<30',            40.00),

-- 甲状腺功能 (item_id: 43-47)
('促甲状腺激素(TSH)', 'TSH01', '甲状腺功能', 'mIU/L',   '0.35-4.94',      60.00),
('游离T3(FT3)',       'FT301', '甲状腺功能', 'pmol/L',  '3.5-6.5',        50.00),
('游离T4(FT4)',       'FT401', '甲状腺功能', 'pmol/L',  '11.5-22.7',      50.00),
('抗甲状腺过氧化物酶抗体', 'TPOAb','甲状腺功能','IU/mL', '<34',            80.00),
('抗甲状腺球蛋白抗体', 'TgAb', '甲状腺功能',  'IU/mL',  '<115',           80.00),

-- 肿瘤标志物 (item_id: 48-53)
('甲胎蛋白(AFP)',     'AFP01', '肿瘤标志物', 'ng/mL',   '<7.0',           80.00),
('癌胚抗原(CEA)',     'CEA01', '肿瘤标志物', 'ng/mL',   '<5.0',           80.00),
('CA19-9',            'CA199','肿瘤标志物',  'U/mL',    '<37',            100.00),
('CA125',             'CA125','肿瘤标志物',  'U/mL',    '<35',            100.00),
('PSA(前列腺特异性抗原)','PSA1','肿瘤标志物','ng/mL',  '<4.0',            100.00),
('CA153',             'CA153','肿瘤标志物',  'U/mL',    '<25',            100.00),

-- 尿常规 (item_id: 54-59)
('尿蛋白(PRO)',       'UR001', '尿常规',     '-',       '阴性(-)',        10.00),
('尿糖(GLU)',         'UR002', '尿常规',     '-',       '阴性(-)',        10.00),
('尿酮体(KET)',       'UR003', '尿常规',     '-',       '阴性(-)',        10.00),
('尿潜血(BLD)',       'UR004', '尿常规',     '-',       '阴性(-)',        10.00),
('尿白细胞(LEU)',     'UR005', '尿常规',     '-',       '阴性(-)',        10.00),
('尿pH值',            'UR006', '尿常规',     '-',       '4.5-8.0',        10.00),

-- 影像检查 (item_id: 60-64)
('心电图(ECG)',       'ECG01', '影像检查',   '-',       '正常心电图',     50.00),
('胸部正位DR',        'XRY01', '影像检查',   '-',       '未见异常',       80.00),
('腹部彩超',          'US001', '影像检查',   '-',       '未见异常',       120.00),
('甲状腺彩超',        'US002', '影像检查',   '-',       '未见异常',       100.00),
('颈部血管彩超',      'US003', '影像检查',   '-',       '未见异常',       150.00),

-- 专科检查 (item_id: 65-67)
('视力检查',          'EYE01', '专科检查',   '-',       '1.0及以上',      20.00),
('眼底检查',          'EYE02', '专科检查',   '-',       '未见异常',       30.00),
('口腔检查',          'DEN01', '专科检查',   '-',       '未见异常',       25.00)
ON DUPLICATE KEY UPDATE code = code;

-- ---------------------------------------------
-- 3.4 检查组（10个套餐）
-- ---------------------------------------------
INSERT INTO check_groups (group_name, description, price, daily_limit) VALUES
('基础体检A',   '身高、体重、血压、心率、BMI等基础指标，适合日常健康监测',                    80.00,  60),
('基础体检B',   '基础A + 血常规全套，适合年度基础体检',                                    150.00, 50),
('入职体检',    '基础项目+心电图+胸部DR+血常规，满足一般企业入职需求',                      180.00, 60),
('白领精英',    '基础+生化+肝功能+肾功能，适合长期久坐的白领人群',                          280.00, 40),
('中青年男士',  '涵盖心血管、肝肾功能、肿瘤标志物的男士综合体检',                            420.00, 30),
('中青年女士',  '涵盖妇科检查、乳腺彩超、甲状腺功能等女士专项体检',                          480.00, 30),
('银发关怀',    '颈部血管彩超+甲功五项+肿瘤标志物全套，适合60岁以上长辈',                   680.00, 20),
('深度筛查',    '33项生化+6项肿瘤+5项甲功+4项影像，最全面的单次体检',                       880.00, 15),
('糖尿病专筛',  '血糖+糖化+肾功能+眼底+尿微量白蛋白，针对糖尿病风险人群',                   350.00, 30),
('心脑血管专筛','血脂全套+颈动脉彩超+心电图+同型半胱氨酸，针对心脑血管高风险人群',          400.00, 30)
ON DUPLICATE KEY UPDATE group_name = group_name;

-- ---------------------------------------------
-- 3.5 检查组↔检查项关联
-- ---------------------------------------------

-- 基础体检A (group_id=1): 1-7（一般检查）
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(1,1,1),(1,2,2),(1,3,3),(1,4,4),(1,5,5),(1,6,6),(1,7,7)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 基础体检B (group_id=2): 1-18（一般检查 + 血常规全部）
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(2,1,1),(2,2,2),(2,3,3),(2,4,4),(2,5,5),(2,6,6),(2,7,7),
(2,8,8),(2,9,9),(2,10,10),(2,11,11),(2,12,12),(2,13,13),
(2,14,14),(2,15,15),(2,16,16),(2,17,17),(2,18,18)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 入职体检 (group_id=3): 1-7 + 8-10 + 60 + 61 + 65 + 67
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(3,1,1),(3,2,2),(3,3,3),(3,4,4),(3,5,5),(3,6,6),(3,7,7),
(3,8,8),(3,9,9),(3,10,10),(3,60,11),(3,61,12),(3,65,13),(3,67,14)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 白领精英 (group_id=4): 1-7 + 8-18 + 19-31 + 32-33
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(4,1,1),(4,2,2),(4,3,3),(4,4,4),(4,5,5),(4,6,6),(4,7,7),
(4,8,8),(4,9,9),(4,10,10),(4,11,11),(4,12,12),(4,13,13),
(4,14,14),(4,15,15),(4,16,16),(4,17,17),(4,18,18),
(4,19,19),(4,20,20),(4,21,21),(4,22,22),(4,23,23),(4,24,24),
(4,25,25),(4,26,26),(4,27,27),(4,28,28),(4,29,29),(4,30,30),(4,31,31),
(4,32,32),(4,33,33)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 中青年男士 (group_id=5): 白领精英 + 肾功能 + PSA + AFP + CEA
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(5,1,1),(5,2,2),(5,3,3),(5,4,4),(5,5,5),(5,6,6),(5,7,7),
(5,8,8),(5,9,9),(5,10,10),(5,11,11),(5,12,12),(5,13,13),
(5,14,14),(5,15,15),(5,16,16),(5,17,17),(5,18,18),
(5,19,19),(5,20,20),(5,21,21),(5,22,22),(5,23,23),(5,24,24),
(5,25,25),(5,26,26),(5,27,27),(5,28,28),(5,29,29),(5,30,30),
(5,31,31),(5,32,32),(5,33,33),(5,38,34),(5,39,35),(5,40,36),
(5,41,37),(5,48,38),(5,49,39),(5,52,40)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 中青年女士 (group_id=6): 白领精英 + 妇科相关
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(6,1,1),(6,2,2),(6,3,3),(6,4,4),(6,5,5),(6,6,6),(6,7,7),
(6,8,8),(6,9,9),(6,10,10),(6,11,11),(6,12,12),(6,13,13),
(6,14,14),(6,15,15),(6,16,16),(6,17,17),(6,18,18),
(6,19,19),(6,20,20),(6,21,21),(6,22,22),(6,23,23),(6,24,24),
(6,25,25),(6,26,26),(6,27,27),(6,28,28),(6,29,29),(6,30,30),
(6,31,31),(6,32,32),(6,33,33),(6,43,34),(6,44,35),(6,45,36),
(6,48,37),(6,49,38),(6,50,39),(6,51,40),(6,53,41)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 银发关怀 (group_id=7): 全面检查
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(7,1,1),(7,2,2),(7,3,3),(7,4,4),(7,5,5),(7,6,6),(7,7,7),
(7,8,8),(7,9,9),(7,10,10),(7,11,11),(7,12,12),(7,13,13),
(7,14,14),(7,15,15),(7,16,16),(7,17,17),(7,18,18),
(7,19,19),(7,20,20),(7,21,21),(7,22,22),(7,23,23),(7,24,24),
(7,25,25),(7,26,26),(7,27,27),(7,28,28),(7,29,29),(7,30,30),
(7,31,31),(7,32,32),(7,33,33),(7,34,34),(7,35,35),(7,36,36),
(7,37,37),(7,38,38),(7,39,39),(7,40,40),(7,41,41),(7,42,42),
(7,43,43),(7,44,44),(7,45,45),(7,46,46),(7,47,47),(7,48,48),
(7,49,49),(7,50,50),(7,51,51),(7,52,52),(7,53,53),(7,60,54),
(7,62,55),(7,64,56)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 深度筛查 (group_id=8): 所有67项
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(8,1,1),(8,2,2),(8,3,3),(8,4,4),(8,5,5),(8,6,6),(8,7,7),
(8,8,8),(8,9,9),(8,10,10),(8,11,11),(8,12,12),(8,13,13),
(8,14,14),(8,15,15),(8,16,16),(8,17,17),(8,18,18),
(8,19,19),(8,20,20),(8,21,21),(8,22,22),(8,23,23),(8,24,24),
(8,25,25),(8,26,26),(8,27,27),(8,28,28),(8,29,29),(8,30,30),
(8,31,31),(8,32,32),(8,33,33),(8,34,34),(8,35,35),(8,36,36),
(8,37,37),(8,38,38),(8,39,39),(8,40,40),(8,41,41),(8,42,42),
(8,43,43),(8,44,44),(8,45,45),(8,46,46),(8,47,47),(8,48,48),
(8,49,49),(8,50,50),(8,51,51),(8,52,52),(8,53,53),
(8,54,54),(8,55,55),(8,56,56),(8,57,57),(8,58,58),(8,59,59),
(8,60,60),(8,61,61),(8,62,62),(8,63,63),(8,64,64),(8,65,65),
(8,66,66),(8,67,67)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 糖尿病专筛 (group_id=9): 血糖+糖化+肾功+眼底+尿微量白蛋白
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(9,1,1),(9,2,2),(9,3,3),(9,4,4),(9,5,5),(9,6,6),
(9,19,7),(9,20,8),(9,21,9),(9,22,10),(9,23,11),
(9,38,12),(9,39,13),(9,40,14),(9,42,15),(9,66,16)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 心脑血管专筛 (group_id=10): 血脂+颈动脉+心电图
INSERT INTO group_item_relation (group_id, item_id, sort_order) VALUES
(10,1,1),(10,2,2),(10,3,3),(10,4,4),(10,5,5),(10,6,6),
(10,22,7),(10,23,8),(10,24,9),(10,25,10),(10,60,11),(10,64,12)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- ---------------------------------------------
-- 3.6 示例用户
-- ---------------------------------------------
INSERT INTO users (phone, password_hash, real_name, id_card, gender, birth_date, status, first_login) VALUES
('13900000001', '123456', '赵小明', '110101199001011234', 1, '1990-01-01', 1, FALSE),
('13900000002', '123456', '钱小红', '110102199205062345', 2, '1992-05-06', 1, FALSE),
('13900000003', '123456', '孙大伟', '110103198512253456', 1, '1985-12-25', 1, FALSE),
('13900000004', '123456', '李美丽', '110104199508084567', 2, '1995-08-08', 1, FALSE),
('13900000005', '123456', '周建国', '110105196503035678', 1, '1965-03-03', 1, FALSE)
ON DUPLICATE KEY UPDATE phone = phone;

-- ---------------------------------------------
-- 3.7 示例预约
-- ---------------------------------------------
INSERT INTO appointments (user_id, group_id, doctor_id, exam_date, exam_time_slot, status, payment_status) VALUES
(1, 3, 1, '2026-07-10', '上午', 'PENDING',  FALSE),
(2, 4, 1, '2026-07-11', '上午', 'PENDING',  FALSE),
(3, 5, 1, '2026-07-10', '下午', 'PENDING',TRUE),
(4, 6, 1, '2026-07-12', '上午', 'PENDING',  TRUE),
(5, 7, 1, '2026-07-09', '上午', 'COMPLETED',TRUE),
(1, 8, 1, '2026-07-15', '上午', 'PENDING',  TRUE),
(2, 9, 1, '2026-07-14', '下午', 'PENDING',FALSE),
(3, 1, 1, '2026-07-08', '上午', 'COMPLETED',TRUE),
(4, 6, 1, '2026-07-07', '上午', 'COMPLETED', TRUE),
(1, 3, 1, '2026-07-06', '下午', 'COMPLETED', TRUE),
(2, 10, 1, '2026-07-05', '上午', 'COMPLETED', TRUE),
(5, 2, 1, '2026-07-16', '上午', 'PENDING', TRUE),
(4, 8, 1, '2026-07-17', '下午', 'PENDING', FALSE)
ON DUPLICATE KEY UPDATE appointment_id = appointment_id;

-- ---------------------------------------------
-- 3.8 示例检查结果（已完成的预约）
-- ---------------------------------------------
-- 银发关怀 周建国 (appointment_id=5)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, doctor_note, exam_date) VALUES
(5, 1,  1, '168',    FALSE, NULL,                               '2026-07-09 08:30'),
(5, 2,  1, '72',     FALSE, NULL,                               '2026-07-09 08:30'),
(5, 3,  1, '25.5',   TRUE,  'BMI偏高，建议控制饮食、增加运动',   '2026-07-09 08:30'),
(5, 4,  1, '155',    TRUE,  '收缩压偏高，建议低盐饮食后复查',   '2026-07-09 08:35'),
(5, 5,  1, '92',     TRUE,  '舒张压偏高',                       '2026-07-09 08:35'),
(5, 6,  1, '78',     FALSE, NULL,                               '2026-07-09 08:35'),
(5, 8,  3, '5.8',    FALSE, NULL,                               '2026-07-09 09:00'),
(5, 9,  3, '4.5',    FALSE, NULL,                               '2026-07-09 09:00'),
(5, 10, 3, '142',    FALSE, NULL,                               '2026-07-09 09:00'),
(5, 19, 3, '6.8',    TRUE,  '空腹血糖偏高，建议做糖耐量试验',    '2026-07-09 09:10'),
(5, 22, 3, '6.2',    TRUE,  '总胆固醇偏高',                     '2026-07-09 09:10'),
(5, 38, 2, '9.5',    TRUE,  '尿素氮偏高，建议低蛋白饮食',       '2026-07-09 09:20'),
(5, 39, 2, '118',    TRUE,  '肌酐偏高，建议定期复查肾功能',     '2026-07-09 09:20'),
(5, 60, 6, '正常',   FALSE, NULL,                               '2026-07-09 10:00'),
(5, 62, 5, '脂肪肝(轻度)', TRUE, '轻度脂肪肝，建议控制饮食、增加运动', '2026-07-09 10:30')
ON DUPLICATE KEY UPDATE result_id = result_id;

-- 基础A 孙大伟 (appointment_id=8)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, doctor_note, exam_date) VALUES
(8, 1, 1, '175',  FALSE, NULL, '2026-07-08 08:30'),
(8, 2, 1, '70',   FALSE, NULL, '2026-07-08 08:30'),
(8, 3, 1, '22.9', FALSE, NULL, '2026-07-08 08:30'),
(8, 4, 1, '120',  FALSE, NULL, '2026-07-08 08:35'),
(8, 5, 1, '78',   FALSE, NULL, '2026-07-08 08:35'),
(8, 6, 1, '72',   FALSE, NULL, '2026-07-08 08:35')
ON DUPLICATE KEY UPDATE result_id = result_id;

-- 中青年女士 李美丽 (appointment_id=9)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, doctor_note, exam_date) VALUES
(9, 1, 1, '160', FALSE, NULL, '2026-07-07 08:30'),
(9, 2, 1, '55', FALSE, NULL, '2026-07-07 08:30'),
(9, 3, 1, '21.5', FALSE, NULL, '2026-07-07 08:30'),
(9, 4, 1, '118', FALSE, NULL, '2026-07-07 08:35'),
(9, 5, 1, '76', FALSE, NULL, '2026-07-07 08:35'),
(9, 6, 1, '72', FALSE, NULL, '2026-07-07 08:35'),
(9, 8, 3, '6.2', FALSE, NULL, '2026-07-07 09:00'),
(9, 9, 3, '4.2', FALSE, NULL, '2026-07-07 09:00'),
(9, 10, 3, '128', FALSE, NULL, '2026-07-07 09:00'),
(9, 19, 3, '5.2', FALSE, NULL, '2026-07-07 09:10'),
(9, 22, 3, '4.8', FALSE, NULL, '2026-07-07 09:10'),
(9, 23, 3, '1.2', FALSE, NULL, '2026-07-07 09:10'),
(9, 32, 3, '25', FALSE, NULL, '2026-07-07 09:15'),
(9, 43, 3, '2.1', FALSE, NULL, '2026-07-07 09:20'),
(9, 48, 3, '3.2', FALSE, NULL, '2026-07-07 09:30'),
(9, 51, 3, '15', FALSE, NULL, '2026-07-07 09:30')
ON DUPLICATE KEY UPDATE result_id = result_id;

-- 入职体检 赵小明 (appointment_id=10)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, doctor_note, exam_date) VALUES
(10, 1, 1, '172', FALSE, NULL, '2026-07-06 14:00'),
(10, 2, 1, '80', FALSE, NULL, '2026-07-06 14:00'),
(10, 3, 1, '27.1', TRUE, 'BMI偏高，建议控制体重', '2026-07-06 14:00'),
(10, 4, 1, '135', FALSE, NULL, '2026-07-06 14:05'),
(10, 5, 1, '88', FALSE, NULL, '2026-07-06 14:05'),
(10, 6, 1, '85', FALSE, NULL, '2026-07-06 14:05'),
(10, 8, 3, '7.5', FALSE, NULL, '2026-07-06 14:20'),
(10, 9, 3, '4.8', FALSE, NULL, '2026-07-06 14:20'),
(10, 10, 3, '145', FALSE, NULL, '2026-07-06 14:20'),
(10, 60, 6, '窦性心律不齐', TRUE, '轻度窦性心律不齐，建议复查', '2026-07-06 15:00'),
(10, 61, 4, '未见异常', FALSE, NULL, '2026-07-06 15:30'),
(10, 65, 7, '1.2', FALSE, NULL, '2026-07-06 15:45'),
(10, 67, 9, '未见异常', FALSE, NULL, '2026-07-06 15:50')
ON DUPLICATE KEY UPDATE result_id = result_id;

-- 心脑血管专筛 钱小红 (appointment_id=11)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, doctor_note, exam_date) VALUES
(11, 1, 1, '158', FALSE, NULL, '2026-07-05 08:30'),
(11, 2, 1, '52', FALSE, NULL, '2026-07-05 08:30'),
(11, 3, 1, '20.8', FALSE, NULL, '2026-07-05 08:30'),
(11, 4, 1, '125', FALSE, NULL, '2026-07-05 08:35'),
(11, 5, 1, '82', FALSE, NULL, '2026-07-05 08:35'),
(11, 6, 1, '70', FALSE, NULL, '2026-07-05 08:35'),
(11, 22, 3, '5.8', TRUE, '总胆固醇偏高，建议低脂饮食', '2026-07-05 09:00'),
(11, 23, 3, '2.1', TRUE, '甘油三酯偏高，建议控制油脂摄入', '2026-07-05 09:00'),
(11, 24, 3, '1.0', FALSE, NULL, '2026-07-05 09:00'),
(11, 25, 3, '3.8', TRUE, '低密度脂蛋白偏高，心血管风险增加', '2026-07-05 09:00'),
(11, 60, 6, '正常', FALSE, NULL, '2026-07-05 09:30'),
(11, 64, 5, '未见明显斑块', FALSE, NULL, '2026-07-05 10:00')
ON DUPLICATE KEY UPDATE result_id = result_id;
