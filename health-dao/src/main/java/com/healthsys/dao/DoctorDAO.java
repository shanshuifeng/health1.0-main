package com.healthsys.dao;

import com.healthsys.common.entity.Doctor;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public Doctor getByUsername(String username) {
        String sql = "SELECT * FROM doctors WHERE username = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Doctor getById(Long id) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Doctor> getAll() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Doctor doctor) {
        String sql = "INSERT INTO doctors (username, password_hash, name, department, title, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getUsername());
            stmt.setString(2, doctor.getPasswordHash());
            stmt.setString(3, doctor.getName());
            stmt.setString(4, doctor.getDepartment());
            stmt.setString(5, doctor.getTitle());
            stmt.setInt(6, doctor.getStatus() != null ? doctor.getStatus() : 1);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Doctor doctor) {
        String sql = "UPDATE doctors SET name=?, department=?, title=?, status=?, updated_at=? WHERE doctor_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getName());
            stmt.setString(2, doctor.getDepartment());
            stmt.setString(3, doctor.getTitle());
            stmt.setInt(4, doctor.getStatus() != null ? doctor.getStatus() : 1);
            stmt.setObject(5, LocalDateTime.now());
            stmt.setLong(6, doctor.getDoctorId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getLong("doctor_id"));
        d.setUsername(rs.getString("username"));
        d.setPasswordHash(rs.getString("password_hash"));
        d.setName(rs.getString("name"));
        d.setDepartment(rs.getString("department"));
        d.setTitle(rs.getString("title"));
        d.setStatus(rs.getInt("status"));
        d.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        d.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return d;
    }
}
