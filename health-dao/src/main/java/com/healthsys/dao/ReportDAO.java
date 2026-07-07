package com.healthsys.dao;

import com.healthsys.common.entity.Report;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public Report getByAppointmentId(Long appointmentId) {
        String sql = "SELECT * FROM reports WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Report getById(Long id) {
        String sql = "SELECT * FROM reports WHERE report_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
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
        String sql = "INSERT INTO reports (appointment_id, doctor_id, pdf_file_path, summary, upload_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, report.getAppointmentId());
            stmt.setLong(2, report.getDoctorId());
            stmt.setString(3, report.getPdfFilePath());
            stmt.setString(4, report.getSummary());
            stmt.setObject(5, report.getUploadTime() != null ? report.getUploadTime() : LocalDateTime.now());
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) { report.setReportId(rs.getLong(1)); return true; }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Report report) {
        String sql = "UPDATE reports SET pdf_file_path=?, summary=?, updated_at=? WHERE report_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, report.getPdfFilePath());
            stmt.setString(2, report.getSummary());
            stmt.setObject(3, LocalDateTime.now());
            stmt.setLong(4, report.getReportId());
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

    private Report mapRow(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setReportId(rs.getLong("report_id"));
        r.setAppointmentId(rs.getLong("appointment_id"));
        r.setDoctorId(rs.getLong("doctor_id"));
        r.setPdfFilePath(rs.getString("pdf_file_path"));
        r.setSummary(rs.getString("summary"));
        r.setUploadTime(rs.getObject("upload_time", LocalDateTime.class));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return r;
    }
}
