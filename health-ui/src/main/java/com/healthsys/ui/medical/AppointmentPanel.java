package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.dao.AppointmentDAO;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentPanel extends CrudPanel<Appointment> {
    private final Long doctorId;
    private AppointmentDAO appointmentDAO;
    private JTable table;
    private AppointmentTableModel tableModel;
    private JDateChooser dateFromChooser;
    private JDateChooser dateToChooser;
    private JTextField userNameSearchField;
    private String quickStatusFilter = null;

    public AppointmentPanel(Long doctorId) {
        this.doctorId = doctorId;
        appointmentDAO = new AppointmentDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        JPanel sp = getSearchPanel();
        sp.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 12);

        dateFromChooser = new JDateChooser();
        dateFromChooser.setPreferredSize(new Dimension(105, 28));
        dateFromChooser.setDateFormatString("yyyy-MM-dd");
        dateFromChooser.setDate(Date.from(LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        dateToChooser = new JDateChooser();
        dateToChooser.setPreferredSize(new Dimension(105, 28));
        dateToChooser.setDateFormatString("yyyy-MM-dd");
        dateToChooser.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        userNameSearchField = new JTextField(6);
        userNameSearchField.setFont(fieldFont);
        userNameSearchField.setPreferredSize(new Dimension(70, 28));

        JLabel dateLabel = new JLabel("日期");
        dateLabel.setFont(fieldFont);
        JLabel toLabel = new JLabel("至");
        toLabel.setFont(fieldFont);
        JLabel userLabel = new JLabel("用户");
        userLabel.setFont(fieldFont);

        sp.add(dateLabel);
        sp.add(dateFromChooser);
        sp.add(toLabel);
        sp.add(dateToChooser);
        sp.add(userLabel);
        sp.add(userNameSearchField);

        getSearchButton().addActionListener(e -> refreshData());
    }

    private void setupButtonListeners() {
        getAddButton().setVisible(false);
        getEditButton().setVisible(false);
        getDeleteButton().setVisible(false);

        JPanel buttonPanel = (JPanel) getAddButton().getParent();

        JButton startExamBtn = CrudPanel.createStyledButton("开始检查", new Color(102, 204, 153));
        startExamBtn.addActionListener(e -> {
            Appointment selected = getSelectedAppointment();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要检查的预约", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("COMPLETED".equals(selected.getStatus())) {
                JOptionPane.showMessageDialog(this, "该预约已完成检查", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if ("CANCELLED".equals(selected.getStatus())) {
                JOptionPane.showMessageDialog(this, "该预约已取消", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ExamResultEntryDialog dialog = new ExamResultEntryDialog(doctorId, selected);
            if (dialog.showDialog() == ExamResultEntryDialog.OK_OPTION) {
                refreshData();
            }
        });
        buttonPanel.add(startExamBtn, 0);

        JButton viewResultBtn = CrudPanel.createStyledButton("查看结果", new Color(255, 204, 153));
        viewResultBtn.addActionListener(e -> {
            Appointment selected = getSelectedAppointment();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择预约", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showExistingResults(selected);
        });
        buttonPanel.add(viewResultBtn);
    }

    private void showExistingResults(Appointment appointment) {
        com.healthsys.dao.ExamRecordDAO examRecordDAO = new com.healthsys.dao.ExamRecordDAO();
        java.util.List<com.healthsys.common.entity.ExamRecord> records =
                examRecordDAO.getExamRecordsByAppointment(appointment.getId());

        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该预约暂无检查结果。", "查看结果", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("预约ID: ").append(appointment.getId())
          .append("  患者: ").append(appointment.getUserName())
          .append("  检查组: ").append(appointment.getGroupName()).append("\n\n");

        for (com.healthsys.common.entity.ExamRecord r : records) {
            sb.append("● ").append(r.getItemName() != null ? r.getItemName() : "检查项#" + r.getItemId())
              .append(": ").append(r.getResultValue() != null ? r.getResultValue() : "-")
              .append(r.getIsAbnormal() != null && r.getIsAbnormal() ? " 【异常】" : "")
              .append(r.getDoctorNote() != null ? "\n  备注: " + r.getDoctorNote() : "")
              .append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 350));

        JOptionPane.showMessageDialog(this, scrollPane, "检查结果", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initializeTable() {
        tableModel = new AppointmentTableModel();
        table = new JTable(tableModel);

        Font tableFont = new Font("微软雅黑", Font.PLAIN, 14);
        table.setFont(tableFont);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(36);
        table.setSelectionBackground(new Color(220, 230, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(190, 190, 190));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // 列: 用户, 检查组, 检查日期, 时段, 状态, 支付
        int[] widths = {80, 160, 100, 60, 70, 70};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // 快速分类按钮
        JPanel quickFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton pendingBtn = new JButton("待检查");
        JButton completedBtn = new JButton("已完成");
        JButton cancelledBtn = new JButton("已取消");
        JButton allBtn = new JButton("全部");
        Font ff = new Font("微软雅黑", Font.BOLD, 12);
        for (JButton btn : new JButton[]{pendingBtn, completedBtn, cancelledBtn, allBtn}) {
            btn.setFont(ff); btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(80, 28));
        }
        pendingBtn.setBackground(new Color(255, 193, 7)); pendingBtn.setForeground(Color.BLACK);
        completedBtn.setBackground(new Color(76, 175, 80)); completedBtn.setForeground(Color.BLACK);
        cancelledBtn.setBackground(new Color(158, 158, 158)); cancelledBtn.setForeground(Color.BLACK);
        allBtn.setBackground(new Color(70, 104, 197)); allBtn.setForeground(Color.BLACK);

        pendingBtn.addActionListener(e -> { quickStatusFilter = "PENDING"; refreshData(); });
        completedBtn.addActionListener(e -> { quickStatusFilter = "COMPLETED"; refreshData(); });
        cancelledBtn.addActionListener(e -> { quickStatusFilter = "CANCELLED"; refreshData(); });
        allBtn.addActionListener(e -> { quickStatusFilter = null; refreshData(); });

        quickFilterPanel.add(allBtn);
        quickFilterPanel.add(pendingBtn);
        quickFilterPanel.add(completedBtn);
        quickFilterPanel.add(cancelledBtn);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(quickFilterPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        setContent(contentPanel);
    }

    @Override
    public void refreshData() {
        LocalDate today = LocalDate.now();

        Date fromDate = dateFromChooser != null ? dateFromChooser.getDate() : null;
        Date toDate = dateToChooser != null ? dateToChooser.getDate() : null;
        LocalDate dateFrom = fromDate != null
                ? fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : today.minusDays(30);
        LocalDate dateTo = toDate != null
                ? toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : today;

        String userName = userNameSearchField != null ? userNameSearchField.getText().trim() : "";

        List<Appointment> result;
        if (!userName.isEmpty()) {
            result = appointmentDAO.search(userName);
            if (dateFrom != null || dateTo != null || quickStatusFilter != null) {
                LocalDate finalDateFrom = dateFrom;
                LocalDate finalDateTo = dateTo;
                result = result.stream().filter(a -> {
                    if (finalDateFrom != null && a.getExamDate() != null && a.getExamDate().isBefore(finalDateFrom)) return false;
                    if (finalDateTo != null && a.getExamDate() != null && a.getExamDate().isAfter(finalDateTo)) return false;
                    if (quickStatusFilter != null && !quickStatusFilter.equals(a.getStatus())) return false;
                    return true;
                }).collect(Collectors.toList());
            }
        } else {
            result = appointmentDAO.searchByFilters(doctorId, dateFrom, dateTo, quickStatusFilter);
        }
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    public Appointment getSelectedAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getItemAt(selectedRow);
        }
        return null;
    }

    private class AppointmentTableModel extends AbstractTableModel {
        private String[] columnNames = {"用户", "检查组", "检查日期", "时段", "状态", "支付"};
        private List<Appointment> data;

        public void setData(List<Appointment> data) {
            this.data = data;
        }

        public Appointment getItemAt(int index) {
            return data.get(index);
        }

        @Override public int getColumnCount() { return columnNames.length; }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Appointment a = data.get(row);
            return switch (col) {
                case 0 -> a.getUserName();
                case 1 -> a.getGroupName();
                case 2 -> a.getExamDate();
                case 3 -> a.getExamTimeSlot() != null ? a.getExamTimeSlot() : "";
                case 4 -> a.getStatusDisplay();
                case 5 -> a.getPaymentStatusDisplay();
                default -> null;
            };
        }
    }
}
