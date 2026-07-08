package com.healthsys.ui.medical;

import com.healthsys.common.entity.Report;
import com.healthsys.dao.ReportDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class ReportPanel extends JPanel {
    private final Long doctorId;
    private final ReportDAO reportDAO = new ReportDAO();
    private JTable table;
    private ReportTableModel tableModel;

    public ReportPanel(Long doctorId) {
        this.doctorId = doctorId;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        createToolbar();
        initializeTable();
        refreshData();
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(245, 245, 245));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton writeBtn = CrudPanel.createStyledButton("撰写报告", new Color(102, 204, 153));
        writeBtn.addActionListener(e -> {
            ReportEditDialog dialog = new ReportEditDialog(doctorId, null);
            if (dialog.showDialog() == ReportEditDialog.OK_OPTION) {
                refreshData();
            }
        });

        JButton editBtn = CrudPanel.createStyledButton("查看/编辑", new Color(153, 204, 255));
        editBtn.addActionListener(e -> {
            Report selected = getSelectedReport();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择一份报告", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ReportEditDialog dialog = new ReportEditDialog(doctorId, selected);
            if (dialog.showDialog() == ReportEditDialog.OK_OPTION) {
                refreshData();
            }
        });

        buttonPanel.add(writeBtn);
        buttonPanel.add(editBtn);
        toolbar.add(buttonPanel, BorderLayout.WEST);
        add(toolbar, BorderLayout.NORTH);
    }

    private void initializeTable() {
        tableModel = new ReportTableModel();
        table = new JTable(tableModel);

        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(Color.BLACK);

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

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        tableModel.setData(reportDAO.getByDoctorIdWithJoin(doctorId));
        tableModel.fireTableDataChanged();
    }

    private Report getSelectedReport() {
        int row = table.getSelectedRow();
        if (row >= 0) return tableModel.getItemAt(row);
        return null;
    }

    private static class ReportTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "患者", "上传时间", "有PDF"};
        private List<Report> data;

        public void setData(List<Report> data) { this.data = data; }

        public Report getItemAt(int index) { return data.get(index); }

        @Override public int getColumnCount() { return columnNames.length; }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Report r = data.get(row);
            return switch (col) {
                case 0 -> r.getReportId();
                case 1 -> r.getUserName() != null ? r.getUserName() : "";
                case 2 -> r.getUploadTime();
                case 3 -> (r.getPdfFilePath() != null && !r.getPdfFilePath().isEmpty()) ? "是" : "否";
                default -> null;
            };
        }
    }
}
