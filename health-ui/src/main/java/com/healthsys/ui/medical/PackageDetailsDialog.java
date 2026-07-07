package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.ui.medical.CrudPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class PackageDetailsDialog extends JDialog {
    private final Color MAIN_COLOR = new Color(70, 104, 197);

    public PackageDetailsDialog(CheckItemGroup packageItem, List<CheckItem> items) {
        setTitle("检查组详情 - " + packageItem.getName());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setModal(true);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 检查组基本信息面板
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("检查组基本信息"));
        infoPanel.setBackground(Color.WHITE);

        addInfoField(infoPanel, "检查组ID:", packageItem.getId().toString());
        addInfoField(infoPanel, "检查组名称:", packageItem.getName());
        addInfoField(infoPanel, "检查组描述:", packageItem.getDescription());
        addInfoField(infoPanel, "检查组价格:", String.format("¥%.2f", packageItem.getPrice()));

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // 检查项表格
        JTable itemsTable = new JTable(new CheckItemTableModel(items));
        itemsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        itemsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        itemsTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("包含的检查项"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 关闭按钮
        JButton closeButton = CrudPanel.createStyledButton("关闭", MAIN_COLOR);
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addInfoField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelComponent);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(valueComponent);
    }

    private class CheckItemTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "名称", "代码", "描述", "正常范围", "价格"};
        private final List<CheckItem> data;

        public CheckItemTableModel(List<CheckItem> data) {
            this.data = data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CheckItem item = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return item.getId();
                case 1: return item.getName();
                case 2: return item.getCode();
                case 3: return item.getDescription();
                case 4: return item.getNormalRange();
                case 5: return String.format("¥%.2f", item.getPrice());
                default: return null;
            }
        }
    }
}
