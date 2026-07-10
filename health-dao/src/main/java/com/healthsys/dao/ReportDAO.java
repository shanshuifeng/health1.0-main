package com.healthsys.dao;

import com.healthsys.common.entity.Report;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public Report getByAppointmentId(Long appointmentId) {
        String sql = "SELECT r.*, d.name as doctor_name FROM reports r " +
                "LEFT JOIN doctors d ON r.doctor_id = d.doctor_id " +
                "WHERE r.appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Report r = mapRow(rs);
                    r.setDoctorName(rs.getString("doctor_name"));
                    return r;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Report getById(Long id) {
        String sql = "SELECT r.*, d.name as doctor_name FROM reports r " +
                "LEFT JOIN doctors d ON r.doctor_id = d.doctor_id " +
                "WHERE r.report_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Report r = mapRow(rs);
                    r.setDoctorName(rs.getString("doctor_name"));
                    return r;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Report> getByDoctorId(Long doctorId) {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE doctor_id = ? ORDER BY upload_time DESC";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean create(Report report) {
        String sql = "INSERT INTO reports (appointment_id, doctor_id, summary, upload_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, report.getAppointmentId());
            stmt.setLong(2, report.getDoctorId());
            stmt.setString(3, report.getSummary());
            stmt.setObject(4, report.getUploadTime() != null ? report.getUploadTime() : LocalDateTime.now());
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) { report.setReportId(rs.getLong(1)); return true; }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Report report) {
        String sql = "UPDATE reports SET summary=?, updated_at=? WHERE report_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, report.getSummary());
            stmt.setObject(2, LocalDateTime.now());
            stmt.setLong(3, report.getReportId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM reports WHERE report_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Report> getByDoctorIdWithJoin(Long doctorId) {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT r.*, d.name as doctor_name, u.real_name as user_name, " +
                "a.exam_date, cg.group_name " +
                "FROM reports r " +
                "LEFT JOIN doctors d ON r.doctor_id = d.doctor_id " +
                "LEFT JOIN appointments a ON r.appointment_id = a.appointment_id " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN check_groups cg ON a.group_id = cg.group_id " +
                "WHERE r.doctor_id = ? " +
                "ORDER BY r.upload_time DESC";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Report r = mapRow(rs);
                    r.setDoctorName(rs.getString("doctor_name"));
                    r.setUserName(rs.getString("user_name"));
                    r.setGroupName(rs.getString("group_name"));
                    java.sql.Date examDate = rs.getDate("exam_date");
                    if (examDate != null) r.setExamDate(examDate.toLocalDate());
                    list.add(r);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Report mapRow(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setReportId(rs.getLong("report_id"));
        r.setAppointmentId(rs.getLong("appointment_id"));
        r.setDoctorId(rs.getLong("doctor_id"));
        r.setSummary(rs.getString("summary"));
        r.setUploadTime(rs.getObject("upload_time", LocalDateTime.class));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return r;
    }
}
