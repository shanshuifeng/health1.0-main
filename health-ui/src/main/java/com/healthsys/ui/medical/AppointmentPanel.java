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

public class AppointmentPanel extends CrudPanel<Appointment> {
    private final Long doctorId;
    private AppointmentDAO appointmentDAO;
    private JTable table;
    private AppointmentTableModel tableModel;
    private JTextField userNameSearchField;
    private JDateChooser dateFromChooser;
    private JDateChooser dateToChooser;
    private JComboBox<String> statusFilterCombo;
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

        sp.add(new JLabel("日期从:"));
        dateFromChooser = new JDateChooser();
        dateFromChooser.setPreferredSize(new Dimension(120, 25));
        dateFromChooser.setDate(Date.from(LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sp.add(dateFromChooser);

        sp.add(new JLabel("到:"));
        dateToChooser = new JDateChooser();
        dateToChooser.setPreferredSize(new Dimension(120, 25));
        dateToChooser.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sp.add(dateToChooser);

        sp.add(new JLabel("状态:"));
        statusFilterCombo = new JComboBox<>(new String[]{"全部", "待检查", "已完成", "已取消"});
        statusFilterCombo.setPreferredSize(new Dimension(90, 25));
        statusFilterCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sp.add(statusFilterCombo);

        sp.add(new JLabel("用户:"));
        userNameSearchField = new JTextField(8);
        sp.add(userNameSearchField);

        getSearchButton().addActionListener(e -> searchAppointments());
    }

    private void searchAppointments() {
        Date fromDate = dateFromChooser.getDate();
        Date toDate = dateToChooser.getDate();
        final LocalDate dateFrom = fromDate != null
                ? fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        final LocalDate dateTo = toDate != null
                ? toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;

        String statusDisplay = (String) statusFilterCombo.getSelectedItem();
        String status = statusToCode(statusDisplay);

        String userName = userNameSearchField.getText().trim();

        List<Appointment> result;
        if (!userName.isEmpty()) {
            result = appointmentDAO.search(userName);
            if (dateFrom != null || dateTo != null || status != null) {
                result = result.stream().filter(a -> {
                    boolean match = true;
                    if (dateFrom != null && a.getExamDate() != null && a.getExamDate().isBefore(dateFrom)) match = false;
                    if (dateTo != null && a.getExamDate() != null && a.getExamDate().isAfter(dateTo)) match = false;
                    if (status != null && !status.equals(a.getStatus())) match = false;
                    return match;
                }).toList();
            }
        } else {
            result = appointmentDAO.searchByFilters(doctorId, dateFrom, dateTo, status);
        }
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private String statusToCode(String display) {
        return switch (display) {
            case "待检查" -> "PENDING";
            case "已完成" -> "COMPLETED";
            case "已取消" -> "CANCELLED";
            default -> null;
        };
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
        table.setGridColor(new Color(220, 220, 220));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));

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
        List<Appointment> result = appointmentDAO.searchByFilters(doctorId, today.minusDays(30), today, quickStatusFilter);
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
