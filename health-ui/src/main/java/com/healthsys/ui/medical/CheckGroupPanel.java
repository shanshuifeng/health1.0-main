package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.ui.medical.CheckGroupDialog;
import com.healthsys.ui.medical.PackageDetailsDialog;
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

    public CheckGroupPanel() {
        checkItemGroupDAO = new CheckItemGroupDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        // 添加查询字段
        getSearchPanel().add(new JLabel("检查组ID:"));
        idSearchField = new JTextField(8);
        getSearchPanel().add(idSearchField);

        getSearchPanel().add(new JLabel("检查组名称:"));
        nameSearchField = new JTextField(15);
        getSearchPanel().add(nameSearchField);

        // 设置查询按钮事件
        getSearchButton().addActionListener(e -> searchCheckGroups());
    }

    private void searchCheckGroups() {
        String idStr = idSearchField.getText().trim();
        String name = nameSearchField.getText().trim();
        Long id = null;

        try {
            if (!idStr.isEmpty()) {
                id = Long.parseLong(idStr);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的ID", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<CheckItemGroup> result = checkItemGroupDAO.search(id, name);
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private void setupButtonListeners() {
        // 添加按钮事件
        getAddButton().addActionListener(e -> {
            CheckGroupDialog dialog = new CheckGroupDialog(null);
            if (dialog.showDialog() == CheckGroupDialog.OK_OPTION) {
                CheckItemGroup newGroup = dialog.getCheckItemGroup();
                if (checkItemGroupDAO.add(newGroup)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查组添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查组添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 编辑按钮事件
        getEditButton().addActionListener(e -> {
            CheckItemGroup selected = getSelectedCheckGroup();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的检查组", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CheckGroupDialog dialog = new CheckGroupDialog(selected);
            if (dialog.showDialog() == CheckGroupDialog.OK_OPTION) {
                CheckItemGroup updatedGroup = dialog.getCheckItemGroup();
                if (checkItemGroupDAO.update(updatedGroup)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查组更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查组更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 删除按钮事件
        getDeleteButton().addActionListener(e -> {
            CheckItemGroup selected = getSelectedCheckGroup();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的检查组", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除检查组 " + selected.getName() + " 吗?",
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

        // 详情按钮事件
        JButton detailsButton = createStyledButton("查看详情", new Color(102, 153, 204));
        detailsButton.addActionListener(e -> {
            CheckItemGroup selected = getSelectedCheckGroup();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要查看的检查组", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取检查组包含的检查项
            List<CheckItem> items = CheckItemDAO.getItemsByGroupId(selected.getId());
            new PackageDetailsDialog(selected, items).setVisible(true);
        });

        // 将详情按钮添加到工具栏
        JPanel buttonPanel = (JPanel) ((JPanel)getComponent(0)).getComponent(0);
        buttonPanel.add(detailsButton);
    }



    private void initializeTable() {
        tableModel = new CheckGroupTableModel();
        table = new JTable(tableModel);

        // 表格样式优化
        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(Color.BLACK);

        // 隔行变色
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                return c;
            }
        });

        // 列宽自动调整
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

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
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getItemAt(selectedRow);
        }
        return null;
    }

    private class CheckGroupTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID", "检查组名称", "检查组描述", "价格", "创建时间"};
        private List<CheckItemGroup> data;

        public void setData(List<CheckItemGroup> data) {
            this.data = data;
        }

        public CheckItemGroup getItemAt(int index) {
            return data.get(index);
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            CheckItemGroup group = data.get(row);
            switch (col) {
                case 0: return group.getId();
                case 1: return group.getName();
                case 2: return group.getDescription();
                case 3: return String.format("¥%.2f", group.getPrice());
                case 4: return group.getCreatedAt();
                default: return null;
            }
        }
    }
}

