package com.healthsys.dao;

import com.healthsys.common.entity.Users;
import com.healthsys.common.util.DbUtil;
import com.healthsys.common.util.EncryptUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static final String INITIAL_PASSWORD = "123456";

    // ============ 管理端CRUD ============

    public List<Users> search(Long id, String name) {
        List<Users> usersList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");

        if (id != null) sql.append(" AND user_id = ?");
        if (name != null && !name.isEmpty()) sql.append(" AND real_name LIKE ?");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (id != null) pstmt.setLong(paramIndex++, id);
            if (name != null && !name.isEmpty()) pstmt.setString(paramIndex++, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    usersList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return usersList;
    }

    public boolean add(Users user) {
        String sql = "INSERT INTO users (phone, password_hash, real_name, id_card, gender, birth_date, status, first_login) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getPhone());
            pstmt.setString(2, EncryptUtil.encrypt(user.getPasswordHash() != null ? user.getPasswordHash() : "123456"));
            pstmt.setString(3, user.getRealName());
            pstmt.setString(4, user.getIdCard());
            pstmt.setInt(5, user.getGender() != null ? user.getGender() : 0);
            pstmt.setObject(6, user.getBirthDate());
            pstmt.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setBoolean(8, user.isFirstLogin() != null ? user.isFirstLogin() : true);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Users user) {
        String sql = "UPDATE users SET phone=?, password_hash=?, real_name=?, id_card=?, gender=?, " +
                "birth_date=?, status=?, updated_at=? WHERE user_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getPhone());
            pstmt.setString(2, EncryptUtil.encrypt(user.getPasswordHash()));
            pstmt.setString(3, user.getRealName());
            pstmt.setString(4, user.getIdCard());
            pstmt.setInt(5, user.getGender() != null ? user.getGender() : 0);
            pstmt.setObject(6, user.getBirthDate());
            pstmt.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setObject(8, LocalDateTime.now());
            pstmt.setLong(9, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Users> getAll() {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) usersList.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return usersList;
    }

    public Users getById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ============ 用户端方法 ============

    public Users getUserByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean addUser(Users user) {
        return add(user);
    }

    public boolean updateUserPassword(Long userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ?, first_login = FALSE WHERE user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, EncryptUtil.encrypt(newPassword));
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateUserProfile(Users user) {
        String sql = "UPDATE users SET real_name=?, birth_date=?, gender=?, id_card=?, updated_at=NOW() WHERE user_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getRealName());
            stmt.setDate(2, user.getBirthDate() != null ? java.sql.Date.valueOf(user.getBirthDate()) : null);
            stmt.setInt(3, user.getGender() != null ? user.getGender() : 0);
            stmt.setString(4, user.getIdCard());
            stmt.setLong(5, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Users getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE real_name = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ============ Helper ============

    private Users mapRow(ResultSet rs) throws SQLException {
        Users user = new Users();
        user.setUserId(rs.getLong("user_id"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRealName(rs.getString("real_name"));
        user.setIdCard(rs.getString("id_card"));
        user.setGender(rs.getInt("gender"));
        user.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
        user.setStatus(rs.getInt("status"));
        user.setFirstLogin(rs.getBoolean("first_login"));
        user.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        user.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return user;
    }
}
