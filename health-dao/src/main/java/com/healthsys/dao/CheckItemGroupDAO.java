package com.healthsys.dao;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckItemGroupDAO {

    // ==================== 检查组基本CRUD ====================

    public List<CheckItemGroup> search(Long id, String name) {
        List<CheckItemGroup> groups = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM check_groups WHERE 1=1");
        if (id != null) sql.append(" AND group_id = ?");
        if (name != null && !name.isEmpty()) sql.append(" AND group_name LIKE ?");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (id != null) pstmt.setLong(paramIndex++, id);
            if (name != null && !name.isEmpty()) pstmt.setString(paramIndex++, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) groups.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return groups;
    }

    public List<CheckItemGroup> getAll() {
        List<CheckItemGroup> groups = new ArrayList<>();
        String sql = "SELECT * FROM check_groups ORDER BY group_name";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) groups.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return groups;
    }

    public CheckItemGroup getById(Long id) {
        String sql = "SELECT * FROM check_groups WHERE group_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean add(CheckItemGroup group) {
        String sql = "INSERT INTO check_groups (group_name, description, price, daily_limit, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getGroupName());
            pstmt.setString(2, group.getDescription());
            pstmt.setDouble(3, group.getPrice());
            pstmt.setInt(4, group.getDailyLimit() != null ? group.getDailyLimit() : 50);
            pstmt.setInt(5, group.getStatus() != null ? group.getStatus() : 1);
            pstmt.setObject(6, LocalDateTime.now());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(CheckItemGroup group) {
        String sql = "UPDATE check_groups SET group_name=?, description=?, price=?, daily_limit=?, status=?, updated_at=? " +
                "WHERE group_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getGroupName());
            pstmt.setString(2, group.getDescription());
            pstmt.setDouble(3, group.getPrice());
            pstmt.setInt(4, group.getDailyLimit() != null ? group.getDailyLimit() : 50);
            pstmt.setInt(5, group.getStatus() != null ? group.getStatus() : 1);
            pstmt.setObject(6, LocalDateTime.now());
            pstmt.setLong(7, group.getGroupId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM check_groups WHERE group_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ==================== 检查组↔检查项关联 ====================

    public boolean addItemToGroup(Long groupId, Long itemId) {
        String sql = "INSERT INTO group_item_relation (group_id, item_id) VALUES (?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, groupId);
            stmt.setLong(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean removeItemFromGroup(Long groupId, Long itemId) {
        String sql = "DELETE FROM group_item_relation WHERE group_id = ? AND item_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, groupId);
            stmt.setLong(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<CheckItem> getCheckItemsByGroup(Long groupId) {
        List<CheckItem> items = new ArrayList<>();
        String sql = "SELECT ci.* FROM check_items ci " +
                "JOIN group_item_relation gir ON ci.item_id = gir.item_id " +
                "WHERE gir.group_id = ? ORDER BY gir.sort_order";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) items.add(CheckItemDAO.mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    // ==================== 创建检查组（含检查项，事务） ====================

    public boolean createGroup(CheckItemGroup group, List<Long> itemIds) {
        Connection conn = null;
        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO check_groups (group_name, description, price, daily_limit, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, group.getGroupName());
                stmt.setString(2, group.getDescription());
                stmt.setDouble(3, group.getPrice());
                stmt.setInt(4, group.getDailyLimit() != null ? group.getDailyLimit() : 50);
                stmt.setInt(5, 1);
                stmt.setObject(6, LocalDateTime.now());

                if (stmt.executeUpdate() == 0) { conn.rollback(); return false; }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long groupId = rs.getLong(1);
                        group.setGroupId(groupId);

                        String insertRel = "INSERT INTO group_item_relation (group_id, item_id) VALUES (?, ?)";
                        try (PreparedStatement relStmt = conn.prepareStatement(insertRel)) {
                            for (Long itemId : itemIds) {
                                relStmt.setLong(1, groupId);
                                relStmt.setLong(2, itemId);
                                relStmt.addBatch();
                            }
                            relStmt.executeBatch();
                        }
                        conn.commit();
                        return true;
                    }
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // ==================== Helper ====================

    private CheckItemGroup mapRow(ResultSet rs) throws SQLException {
        CheckItemGroup group = new CheckItemGroup();
        group.setGroupId(rs.getLong("group_id"));
        group.setGroupName(rs.getString("group_name"));
        group.setDescription(rs.getString("description"));
        group.setPrice(rs.getDouble("price"));
        group.setDailyLimit(rs.getInt("daily_limit"));
        group.setStatus(rs.getInt("status"));
        group.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        group.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return group;
    }
}
