package com.healthsys.ui.user;

import com.healthsys.dao.CheckItemGroupDAO;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.CheckItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PackageDetailView {
    private JPanel detailPanel;
    private CheckItemGroup checkItemGroup;
    private CheckItemGroupDAO checkItemGroupDAO = new CheckItemGroupDAO();

    public PackageDetailView(CheckItemGroup checkItemGroup) {
        this.checkItemGroup = checkItemGroup;
        initializeUI();
    }

    private void initializeUI() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 检查组基本信息面板
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("检查组信息"));

        infoPanel.add(new JLabel("检查组名称:"));
        infoPanel.add(new JLabel(checkItemGroup.getName()));

        infoPanel.add(new JLabel("描述:"));
        infoPanel.add(new JLabel(checkItemGroup.getDescription()));

        infoPanel.add(new JLabel("价格:"));
        infoPanel.add(new JLabel(String.valueOf(checkItemGroup.getPrice())));

        detailPanel.add(infoPanel, BorderLayout.NORTH);

        // 检查项目列表
        String[] columnNames = { "ID", "检查项目名称", "描述", "价格" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable testTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(testTable);

        loadTestsData(model);

        detailPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTestsData(DefaultTableModel model) {
        model.setRowCount(0);
        List<CheckItem> tests = checkItemGroupDAO.getCheckItemsByGroup(checkItemGroup.getId());

        for (CheckItem test : tests) {
            Object[] rowData = {
                    test.getId(),
                    test.getName(),
                    test.getDescription(),
                    test.getPrice()
            };
            model.addRow(rowData);
        }
    }

    public JPanel getDetailPanel() {
        return detailPanel;
    }
}
