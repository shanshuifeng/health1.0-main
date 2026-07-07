package com.healthsys.dao;

import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamRecordDAO {

    // 添加检查结果
    public boolean addExamRecord(ExamRecord record) {
        String sql = "INSERT INTO check_results (appointment_id, item_id, doctor_id, result_value, is_abnormal, doctor_note, exam_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, record.getAppointmentId());
            stmt.setLong(2, record.getItemId());
            stmt.setLong(3, record.getDoctorId() != null ? record.getDoctorId() : 1L);
            stmt.setString(4, record.getResultValue());
            stmt.setBoolean(5, record.getIsAbnormal() != null ? record.getIsAbnormal() : false);
            stmt.setString(6, record.getDoctorNote());
            stmt.setObject(7, record.getExamDate());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 根据预约ID获取所有检查结果
    public List<ExamRecord> getExamRecordsByAppointment(Long appointmentId) {
        List<ExamRecord> records = new ArrayList<>();
        String sql = "SELECT cr.*, ci.item_name FROM check_results cr " +
                "LEFT JOIN check_items ci ON cr.item_id = ci.item_id " +
                "WHERE cr.appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExamRecord record = new ExamRecord(
                            rs.getLong("result_id"),
                            rs.getLong("appointment_id"),
                            rs.getLong("item_id"),
                            rs.getString("result_value"),
                            rs.getObject("exam_date", LocalDateTime.class)
                    );
                    record.setDoctorId(rs.getLong("doctor_id"));
                    record.setIsAbnormal(rs.getBoolean("is_abnormal"));
                    record.setDoctorNote(rs.getString("doctor_note"));
                    record.setItemName(rs.getString("item_name"));
                    records.add(record);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    // 更新检查结果
    public boolean updateExamResult(Long appointmentId, Long itemId, String resultValue) {
        String sql = "UPDATE check_results SET result_value = ?, exam_date = NOW() WHERE appointment_id = ? AND item_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, resultValue);
            stmt.setLong(2, appointmentId);
            stmt.setLong(3, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 检查是否已有该检查项的记录
    public boolean existsRecord(Long appointmentId, Long itemId) {
        String sql = "SELECT COUNT(*) FROM check_results WHERE appointment_id = ? AND item_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            stmt.setLong(2, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
