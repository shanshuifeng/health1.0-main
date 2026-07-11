-- =============================================
-- 健康体检管理系统 - 数据增强脚本 V2
-- 全变量/子查询引用, 无硬编码ID
-- =============================================
USE healthsys;

-- =============================================
-- 1. 扩充用户 (5→15): +禁用/首次登录/各年龄段
-- =============================================
INSERT INTO users (phone, password_hash, real_name, id_card, gender, birth_date, status, first_login) VALUES
('13900000006','123456','吴小雨','110106200003154321',2,'2000-03-15',1,TRUE),
('13900000007','123456','郑浩然','110107199808082211',1,'1998-08-08',1,FALSE),
('13900000008','123456','王芳',  '110108197512129876',2,'1975-12-12',1,FALSE),
('13900000009','123456','冯志远','110109195601018765',1,'1956-01-01',1,FALSE),
('13900000010','123456','陈静',  '110110198803065432',2,'1988-03-06',1,FALSE),
('13900000011','123456','褚明辉','110111199507152345',1,'1995-07-15',1,FALSE),
('13900000012','123456','卫兰',  '110112196807081234',2,'1968-07-08',1,FALSE),
('13900000013','123456','蒋晓峰','110113200208028765',1,'2002-08-02',0,FALSE),
('13900000014','123456','沈霞',  '110114199203251111',2,'1992-03-25',1,TRUE),
('13900000015','123456','韩磊',  '110115198207306789',1,'1982-07-30',1,FALSE)
ON DUPLICATE KEY UPDATE phone=phone;

-- =============================================
-- 2. 补充预约: 跨6-8月, 分配到不同医生
-- =============================================
INSERT INTO appointments (user_id, group_id, doctor_id, exam_date, exam_time_slot, status, payment_status)
SELECT u.user_id, dt.gid, dt.did, dt.exam_date, dt.slot, dt.st, dt.pay
FROM (
    SELECT '13900000006' ph,  2 gid,  2 did, '2026-06-15' exam_date, '上午' slot, 'COMPLETED' st, TRUE pay UNION ALL
    SELECT '13900000007',      5,      3,     '2026-06-20',          '上午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000008',      4,      1,     '2026-06-25',          '下午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000009',      7,      5,     '2026-06-28',          '上午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000010',      3,      2,     '2026-07-01',          '上午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000011',      8,      4,     '2026-07-03',          '上午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000012',      6,     10,     '2026-07-05',          '下午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000006',      9,      1,     '2026-07-07',          '上午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000007',     10,      5,     '2026-07-08',          '上午',      'COMPLETED',    TRUE      UNION ALL
    SELECT '13900000008',      1,      1,     '2026-07-13',          '上午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000009',      6,     10,     '2026-07-14',          '上午',      'PENDING',      FALSE     UNION ALL
    SELECT '13900000010',      5,      3,     '2026-07-15',          '下午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000011',      7,      5,     '2026-07-18',          '上午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000012',      2,      2,     '2026-07-20',          '上午',      'PENDING',      FALSE     UNION ALL
    SELECT '13900000013',      4,      1,     '2026-07-22',          '下午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000014',      8,      4,     '2026-07-25',          '上午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000015',      9,      1,     '2026-07-28',          '上午',      'PENDING',      FALSE     UNION ALL
    SELECT '13900000001',     10,      5,     '2026-08-01',          '上午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000002',      3,      2,     '2026-08-05',          '下午',      'PENDING',      FALSE     UNION ALL
    SELECT '13900000014',      5,      3,     '2026-08-10',          '上午',      'PENDING',      TRUE      UNION ALL
    SELECT '13900000005',      3,      2,     '2026-06-10',          '下午',      'CANCELLED',    FALSE     UNION ALL
    SELECT '13900000015',      2,      1,     '2026-07-02',          '上午',      'CANCELLED',    FALSE     UNION ALL
    SELECT '13900000006',      8,      4,     '2026-07-12',          '上午',      'CANCELLED',    TRUE
) dt(ph, gid, did, exam_date, slot, st, pay)
JOIN users u ON u.phone=dt.ph
WHERE NOT EXISTS (
    SELECT 1 FROM appointments a WHERE a.user_id=u.user_id AND a.exam_date=dt.exam_date AND a.group_id=dt.gid
);

-- =============================================
-- 3. 为所有已完成但无报告的预约补报告
-- =============================================
INSERT INTO reports (appointment_id, doctor_id, summary, upload_time)
SELECT a.appointment_id, COALESCE(a.doctor_id, 1),
    CASE
        WHEN g.group_id IN (1,2,3) THEN CONCAT('【综合评估】\n',u.real_name,'，本次',g.group_name,'体检各项指标均在正常范围。\n\n【结论】体检合格，身体健康。')
        WHEN g.group_id=4 THEN CONCAT('【综合评估】\n',u.real_name,'，白领精英套餐体检发现指标总体正常。\n\n【建议】长期久坐办公需注意颈椎腰椎保健，每周运动不少于3次。')
        WHEN g.group_id=5 THEN CONCAT('【综合评估】\n',u.real_name,'，中青年男士套餐体检总体良好。\n\n【建议】关注血脂和肝功能，控制饮酒，每年定期体检。')
        WHEN g.group_id=6 THEN CONCAT('【综合评估】\n',u.real_name,'，中青年女士套餐体检结果良好。\n\n【建议】定期妇科体检，关注乳腺和宫颈健康。')
        WHEN g.group_id IN (7,8) THEN CONCAT('【综合评估】\n',u.real_name,'，深度全面体检已完成。请根据异常项进行针对性复查，保持健康生活方式。')
        WHEN g.group_id=9 THEN CONCAT('【综合评估】\n',u.real_name,'，糖尿病专筛结果显示胰岛功能正常。\n\n【建议】保持低糖饮食，定期监测血糖。')
        WHEN g.group_id=10 THEN CONCAT('【综合评估】\n',u.real_name,'，心脑血管专筛结果可接受。\n\n【建议】关注血压血脂，低盐低脂饮食，坚持有氧运动。')
        ELSE CONCAT('【综合评估】\n',u.real_name,'体检完成。\n\n【建议】保持健康生活习惯，每年定期体检。')
    END,
    TIMESTAMPADD(HOUR, 8, a.exam_date)
FROM appointments a
JOIN users u ON a.user_id=u.user_id
JOIN check_groups g ON a.group_id=g.group_id
WHERE a.status='COMPLETED'
  AND NOT EXISTS (SELECT 1 FROM reports r WHERE r.appointment_id=a.appointment_id);

-- =============================================
-- 4. 为新增已完成预约补充检查结果
-- =============================================
SET @a23=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000006' AND a.group_id=2  AND a.exam_date='2026-06-15');
SET @a24=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000007' AND a.group_id=5  AND a.exam_date='2026-06-20');
SET @a25=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000008' AND a.group_id=4  AND a.exam_date='2026-06-25');
SET @a26=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000009' AND a.group_id=7  AND a.exam_date='2026-06-28');
SET @a27=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000010' AND a.group_id=3  AND a.exam_date='2026-07-01');
SET @a28=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000011' AND a.group_id=8  AND a.exam_date='2026-07-03');
SET @a29=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000012' AND a.group_id=6  AND a.exam_date='2026-07-05');
SET @a30=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000006' AND a.group_id=9  AND a.exam_date='2026-07-07');
SET @a31=(SELECT a.appointment_id FROM appointments a JOIN users u ON a.user_id=u.user_id WHERE u.phone='13900000007' AND a.group_id=10 AND a.exam_date='2026-07-08');

-- @a23 基础B 吴小雨 (全部正常)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, exam_date)
SELECT @a23, item_id, 2, '正常', FALSE, '2026-06-15 08:30:00'
FROM group_item_relation WHERE group_id=2
ON DUPLICATE KEY UPDATE result_id=result_id;

-- @a27 入职体检 陈静 (全部正常)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, exam_date)
SELECT @a27, item_id, 2, '正常', FALSE, '2026-07-01 08:30:00'
FROM group_item_relation WHERE group_id=3
ON DUPLICATE KEY UPDATE result_id=result_id;

-- @a30 糖尿病专筛 吴小雨 (全部正常)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, exam_date)
SELECT @a30, item_id, 1, '正常', FALSE, '2026-07-07 08:30:00'
FROM group_item_relation WHERE group_id=9
ON DUPLICATE KEY UPDATE result_id=result_id;

-- @a31 心脑血管专筛 郑浩然 (LDL临界)
INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, exam_date)
SELECT @a31, item_id, 5,
    CASE WHEN item_id=25 THEN '3.5' ELSE '正常' END,
    CASE WHEN item_id=25 THEN TRUE ELSE FALSE END,
    '2026-07-08 08:30:00'
FROM group_item_relation WHERE group_id=10
ON DUPLICATE KEY UPDATE result_id=result_id;

-- =============================================
-- 5. 验证数据完整性
-- =============================================
SELECT '=== 增强后数据统计 ===' AS '';
SELECT '用户' t, COUNT(*) n FROM users
UNION ALL SELECT '禁用', COUNT(*) FROM users WHERE status=0
UNION ALL SELECT '首次登录', COUNT(*) FROM users WHERE first_login=TRUE
UNION ALL SELECT '医生', COUNT(*) FROM doctors
UNION ALL SELECT '检查项', COUNT(*) FROM check_items
UNION ALL SELECT '检查组', COUNT(*) FROM check_groups
UNION ALL SELECT '预约总数', COUNT(*) FROM appointments
UNION ALL SELECT '待检', COUNT(*) FROM appointments WHERE status='PENDING'
UNION ALL SELECT '已完成', COUNT(*) FROM appointments WHERE status='COMPLETED'
UNION ALL SELECT '已取消', COUNT(*) FROM appointments WHERE status='CANCELLED'
UNION ALL SELECT '检查结果', COUNT(*) FROM check_results
UNION ALL SELECT '报告', COUNT(*) FROM reports
UNION ALL SELECT '已完成缺报告', COUNT(*) FROM (
    SELECT 1 FROM appointments a WHERE a.status='COMPLETED'
    AND NOT EXISTS (SELECT 1 FROM reports r WHERE r.appointment_id=a.appointment_id)
) x;
