package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.dao.CheckItemGroupDAO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class CheckGroupPanel extends CrudPanel<CheckItemGroup> {
    private CheckItemGroupDAO checkItemGroupDAO;
    private JTable table;
    private CheckGroupTableModel tableModel;
    private JTextField idSearchField;
    private JTextField nameSearchField;

    private static final Color SUCCESS_BG = new Color(232, 245, 233);
    private static final Color SUCCESS_FG = new Color(46, 125, 50);
    private static final Color DANGER_BG = new Color(255, 235, 238);
    private static final Color DANGER_FG = new Color(198, 40, 40);

    public CheckGroupPanel() {
        checkItemGroupDAO = new CheckItemGroupDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        getSearchPanel().add(new JLabel("检查组ID:"));
        idSearchField = new JTextField(8);
        getSearchPanel().add(idSearchField);

        getSearchPanel().add(new JLabel("检查组名称:"));
        nameSearchField = new JTextField(15);
        getSearchPanel().add(nameSearchField);

        getSearchButton().addActionListener(e -> searchCheckGroups());
    }

    private void searchCheckGroups() {
        String idStr = idSearchField.getText().trim();
        String name = nameSearchField.getText().trim();
        Long id = null;
        try {
            if (!idStr.isEmpty()) id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的ID", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<CheckItemGroup> result = checkItemGroupDAO.search(id, name);
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private void setupButtonListeners() {
        getAddButton().addActionListener(e -> {
            CheckGroupDialog dialog = new CheckGroupDialog(null);
            if (dialog.showDialog() == CheckGroupDialog.OK_OPTION) {
                CheckItemGroup newGroup = dialog.getCheckItemGroup();
                List<Long> itemIds = dialog.getSelectedItemIds();
                if (checkItemGroupDAO.createGroup(newGroup, itemIds)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查组添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查组添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        getEditButton().addActionListener(e -> {
            CheckItemGroup selected = getSelectedCheckGroup();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的检查组", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CheckGroupDialog dialog = new CheckGroupDialog(selected);
            if (dialog.showDialog() == CheckGroupDialog.OK_OPTION) {
                CheckItemGroup updatedGroup = dialog.getCheckItemGroup();
                List<Long> itemIds = dialog.getSelectedItemIds();
                if (checkItemGroupDAO.updateGroup(updatedGroup, itemIds)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查组更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查组更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        getDeleteButton().addActionListener(e -> {
            CheckItemGroup selected = getSelectedCheckGroup();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的检查组", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除检查组 " + selected.getName() + " 吗？",
                    "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (checkItemGroupDAO.delete(selected.getId())) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查组删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查组删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 详情按钮
        JButton detailsButton = createStyledButton("查看详情", new Color(102, 153, 204));
        detailsButton.addActionListener(e -> {
            CheckItemGroup selected = getSelectedCheckGroup();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要查看的检查组", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<CheckItem> items = CheckItemDAO.getItemsByGroupId(selected.getId());
            new PackageDetailsDialog(selected, items).setVisible(true);
        });
        ((JPanel) getAddButton().getParent()).add(detailsButton);
    }

    private void initializeTable() {
        tableModel = new CheckGroupTableModel();
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 隔行变色
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // 状态列 — 彩色标签
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    if ("上架".equals(value)) {
                        lbl.setBackground(SUCCESS_BG);
                        lbl.setForeground(SUCCESS_FG);
                    } else {
                        lbl.setBackground(DANGER_BG);
                        lbl.setForeground(DANGER_FG);
                    }
                }
                lbl.setFont(new Font("微软雅黑", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return lbl;
            }
        });

        // 价格列 — 右对齐
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setFont(new Font("微软雅黑", Font.BOLD, 13));
                if (!isSelected) lbl.setForeground(new Color(46, 125, 50));
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setContent(scrollPane);
    }

    @Override
    public void refreshData() {
        tableModel.setData(checkItemGroupDAO.getAll());
        tableModel.fireTableDataChanged();
    }

    public CheckItemGroup getSelectedCheckGroup() {
        int row = table.getSelectedRow();
        return row >= 0 ? tableModel.getItemAt(row) : null;
    }

    private class CheckGroupTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "名称", "描述", "价格", "每日限额", "状态", "创建时间"};
        private List<CheckItemGroup> data;

        public void setData(List<CheckItemGroup> data) { this.data = data; }
        public CheckItemGroup getItemAt(int i) { return data.get(i); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public String getColumnName(int c) { return columns[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            CheckItemGroup g = data.get(row);
            switch (col) {
                case 0: return g.getId();
                case 1: return g.getName();
                case 2: // 描述截断
                    String desc = g.getDescription();
                    return desc != null && desc.length() > 30 ? desc.substring(0, 30) + "…" : desc;
                case 3: return String.format("¥%.2f", g.getPrice());
                case 4: return g.getDailyLimit();
                case 5: return g.getStatusDisplay();
                case 6: return g.getCreatedAt();
                default: return null;
            }
        }
    }
}
