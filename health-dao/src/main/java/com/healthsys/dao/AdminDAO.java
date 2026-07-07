package com.healthsys.dao;

import com.healthsys.common.entity.Admin;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    public Admin getByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Admin getById(Long id) {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Admin> getAll() {
        List<Admin> list = new ArrayList<>();
        String sql = "SELECT * FROM admins ORDER BY real_name";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Admin admin) {
        String sql = "INSERT INTO admins (username, password_hash, real_name, role, phone, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPasswordHash());
            stmt.setString(3, admin.getRealName());
            stmt.setString(4, admin.getRole() != null ? admin.getRole() : "MANAGER");
            stmt.setString(5, admin.getPhone());
            stmt.setInt(6, admin.getStatus() != null ? admin.getStatus() : 1);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Admin admin) {
        String sql = "UPDATE admins SET real_name=?, role=?, phone=?, status=?, updated_at=? WHERE admin_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, admin.getRealName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getPhone());
            stmt.setInt(4, admin.getStatus() != null ? admin.getStatus() : 1);
            stmt.setObject(5, LocalDateTime.now());
            stmt.setLong(6, admin.getAdminId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM admins WHERE admin_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId(rs.getLong("admin_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setRealName(rs.getString("real_name"));
        a.setRole(rs.getString("role"));
        a.setPhone(rs.getString("phone"));
        a.setStatus(rs.getInt("status"));
        a.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        a.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return a;
    }
}
