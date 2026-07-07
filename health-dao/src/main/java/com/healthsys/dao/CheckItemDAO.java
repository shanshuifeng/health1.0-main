package com.healthsys.dao;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckItemDAO {

    public List<CheckItem> search(String name, String code) {
        List<CheckItem> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM check_items WHERE 1=1");
        if (name != null && !name.isEmpty()) sql.append(" AND item_name LIKE ?");
        if (code != null && !code.isEmpty()) sql.append(" AND code LIKE ?");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (name != null && !name.isEmpty()) pstmt.setString(paramIndex++, "%" + name + "%");
            if (code != null && !code.isEmpty()) pstmt.setString(paramIndex++, "%" + code + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) items.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    public List<CheckItem> getAll() {
        List<CheckItem> items = new ArrayList<>();
        String sql = "SELECT * FROM check_items ORDER BY item_name";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) items.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    public CheckItem getById(Long id) {
        String sql = "SELECT * FROM check_items WHERE item_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean add(CheckItem item) {
        String sql = "INSERT INTO check_items (item_name, code, category, unit, reference_range, price, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCode());
            pstmt.setString(3, item.getCategory());
            pstmt.setString(4, item.getUnit());
            pstmt.setString(5, item.getReferenceRange());
            pstmt.setDouble(6, item.getPrice() != null ? item.getPrice() : 0.0);
            pstmt.setInt(7, item.getStatus() != null ? item.getStatus() : 1);
            pstmt.setObject(8, LocalDateTime.now());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(CheckItem item) {
        String sql = "UPDATE check_items SET item_name=?, code=?, category=?, unit=?, " +
                "reference_range=?, price=?, status=?, updated_at=? WHERE item_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCode());
            pstmt.setString(3, item.getCategory());
            pstmt.setString(4, item.getUnit());
            pstmt.setString(5, item.getReferenceRange());
            pstmt.setDouble(6, item.getPrice() != null ? item.getPrice() : 0.0);
            pstmt.setInt(7, item.getStatus() != null ? item.getStatus() : 1);
            pstmt.setObject(8, LocalDateTime.now());
            pstmt.setLong(9, item.getItemId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM check_items WHERE item_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static List<CheckItem> getItemsByGroupId(Long groupId) {
        List<CheckItem> items = new ArrayList<>();
        String sql = "SELECT ci.* FROM check_items ci " +
                "JOIN group_item_relation gir ON ci.item_id = gir.item_id " +
                "WHERE gir.group_id = ? ORDER BY gir.sort_order";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) items.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    // 兼容旧代码的别名方法
    public List<CheckItem> getAllTests() { return getAll(); }
    public CheckItem getTestById(Long id) { return getById(id); }
    public boolean addTest(CheckItem test) { return add(test); }
    public boolean updateTest(CheckItem test) { return update(test); }
    public boolean deleteTest(Long id) { return delete(id); }

    // Row mapper (also used by CheckItemGroupDAO)
    public static CheckItem mapRow(ResultSet rs) throws SQLException {
        CheckItem item = new CheckItem();
        item.setItemId(rs.getLong("item_id"));
        item.setItemName(rs.getString("item_name"));
        item.setCode(rs.getString("code"));
        item.setCategory(rs.getString("category"));
        item.setUnit(rs.getString("unit"));
        item.setReferenceRange(rs.getString("reference_range"));
        item.setPrice(rs.getDouble("price"));
        item.setStatus(rs.getInt("status"));
        item.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return item;
    }
}
