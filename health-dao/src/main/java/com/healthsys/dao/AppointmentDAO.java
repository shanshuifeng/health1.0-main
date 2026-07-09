package com.healthsys.dao;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    // ============ 管理端搜索 ============

    public List<Appointment> search(String userName) {
        List<Appointment> appointments = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, u.real_name as user_name, cg.group_name as group_name, d.name as doctor_name, " +
                "(r.report_id IS NOT NULL) as has_report " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN check_groups cg ON a.group_id = cg.group_id " +
                "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "LEFT JOIN reports r ON a.appointment_id = r.appointment_id " +
                "WHERE 1=1");
        if (userName != null && !userName.isEmpty()) sql.append(" AND u.real_name LIKE ?");
        sql.append(" ORDER BY a.appointment_time DESC");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (userName != null && !userName.isEmpty()) pstmt.setString(paramIndex++, "%" + userName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) appointments.add(mapRowWithJoin(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return appointments;
    }

    public boolean add(Appointment appointment) {
        String sql = "INSERT INTO appointments (user_id, group_id, doctor_id, appointment_time, exam_date, exam_time_slot, " +
                "status, payment_status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, appointment.getUserId());
            pstmt.setLong(2, appointment.getGroupId());
            if (appointment.getDoctorId() != null) pstmt.setLong(3, appointment.getDoctorId());
            else pstmt.setNull(3, Types.BIGINT);
            pstmt.setObject(4, appointment.getAppointmentTime());
            pstmt.setObject(5, appointment.getExamDate());
            pstmt.setString(6, appointment.getExamTimeSlot());
            pstmt.setString(7, appointment.getStatus() != null ? appointment.getStatus() : "PENDING");
            pstmt.setBoolean(8, Boolean.TRUE.equals(appointment.getPaymentStatus()));
            pstmt.setObject(9, LocalDateTime.now());
            if (pstmt.executeUpdate() > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) { appointment.setAppointmentId(keys.getLong(1)); return true; }
                }
            }
            return false;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET user_id=?, group_id=?, appointment_time=?, exam_date=?, " +
                "exam_time_slot=?, status=?, payment_status=? WHERE appointment_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, appointment.getUserId());
            pstmt.setLong(2, appointment.getGroupId());
            pstmt.setObject(3, appointment.getAppointmentTime());
            pstmt.setObject(4, appointment.getExamDate());
            pstmt.setString(5, appointment.getExamTimeSlot());
            pstmt.setString(6, appointment.getStatus());
            pstmt.setBoolean(7, Boolean.TRUE.equals(appointment.getPaymentStatus()));
            pstmt.setLong(8, appointment.getAppointmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Appointment> getAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, u.real_name as user_name, cg.group_name as group_name, d.name as doctor_name " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN check_groups cg ON a.group_id = cg.group_id " +
                "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "ORDER BY a.appointment_time DESC";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) appointments.add(mapRowWithJoin(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return appointments;
    }

    public Appointment getById(Long id) {
        String sql = "SELECT a.*, u.real_name as user_name, cg.group_name as group_name, d.name as doctor_name " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN check_groups cg ON a.group_id = cg.group_id " +
                "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "WHERE a.appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRowWithJoin(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ============ 用户端方法 ============

    public Appointment getAppointmentById(Long appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (user_id, group_id, doctor_id, appointment_time, exam_date, exam_time_slot, status, payment_status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, appointment.getUserId());
            stmt.setObject(2, appointment.getGroupId(), Types.BIGINT);
            if (appointment.getDoctorId() != null) stmt.setLong(3, appointment.getDoctorId());
            else stmt.setNull(3, Types.BIGINT);
            stmt.setObject(4, appointment.getAppointmentTime());
            stmt.setObject(5, appointment.getExamDate());
            stmt.setString(6, appointment.getExamTimeSlot());
            stmt.setString(7, appointment.getStatus() != null ? appointment.getStatus() : "PENDING");
            stmt.setBoolean(8, Boolean.TRUE.equals(appointment.getPaymentStatus()));
            stmt.setObject(9, LocalDateTime.now());
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) { appointment.setAppointmentId(rs.getLong(1)); return true; }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Appointment> getUserAppointments(long userId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE user_id = ? ORDER BY appointment_id ASC";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) appointments.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return appointments;
    }

    public boolean cancelAppointment(long appointmentId) {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean completeAppointment(long appointmentId) {
        String sql = "UPDATE appointments SET status = 'COMPLETED' WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updatePaymentStatus(Long appointmentId, boolean paid) {
        String sql = "UPDATE appointments SET payment_status = ? WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, paid);
            stmt.setLong(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean assignDoctor(Long appointmentId, Long doctorId, String status) {
        String sql = "UPDATE appointments SET doctor_id = ?, status = ? WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            stmt.setString(2, status);
            stmt.setLong(3, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ============ 医生端方法 ============

    public List<Appointment> searchByFilters(Long doctorId, java.time.LocalDate dateFrom, java.time.LocalDate dateTo, String status) {
        List<Appointment> appointments = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, u.real_name as user_name, cg.group_name as group_name, d.name as doctor_name, " +
                "(r.report_id IS NOT NULL) as has_report " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN check_groups cg ON a.group_id = cg.group_id " +
                "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "LEFT JOIN reports r ON a.appointment_id = r.appointment_id " +
                "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (doctorId != null) {
            sql.append(" AND a.doctor_id = ?");
            params.add(doctorId);
        }
        if (dateFrom != null) {
            sql.append(" AND a.exam_date >= ?");
            params.add(dateFrom);
        }
        if (dateTo != null) {
            sql.append(" AND a.exam_date <= ?");
            params.add(dateTo);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND a.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY a.exam_date ASC, a.exam_time_slot ASC");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) appointments.add(mapRowWithJoin(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return appointments;
    }

    // ============ Helpers ============

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment app = new Appointment(
                rs.getLong("user_id"),
                rs.getObject("group_id", Long.class),
                rs.getObject("appointment_time", LocalDateTime.class));
        app.setAppointmentId(rs.getLong("appointment_id"));
        long doctorId = rs.getLong("doctor_id");
        if (!rs.wasNull()) app.setDoctorId(doctorId);
        app.setExamDate(rs.getObject("exam_date", java.time.LocalDate.class));
        app.setExamTimeSlot(rs.getString("exam_time_slot"));
        app.setStatus(rs.getString("status"));
        app.setPaymentStatus(rs.getBoolean("payment_status"));
        app.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        app.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return app;
    }

    private Appointment mapRowWithJoin(ResultSet rs) throws SQLException {
        Appointment app = mapRow(rs);
        try { app.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        try { app.setGroupName(rs.getString("group_name")); } catch (SQLException ignored) {}
        try { app.setDoctorName(rs.getString("doctor_name")); } catch (SQLException ignored) {}
        boolean hr = rs.getBoolean("has_report");
        if (!rs.wasNull()) app.setHasReport(hr);
        return app;
    }
}
