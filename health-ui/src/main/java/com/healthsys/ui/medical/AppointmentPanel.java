package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.dao.AppointmentDAO;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
        dateFromChooser.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sp.add(dateFromChooser);

        sp.add(new JLabel("到:"));
        dateToChooser = new JDateChooser();
        dateToChooser.setPreferredSize(new Dimension(120, 25));
        dateToChooser.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sp.add(dateToChooser);

        sp.add(new JLabel("状态:"));
        statusFilterCombo = new JComboBox<>(new String[]{"全部", "待检查", "已确认", "已完成", "已取消"});
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
            result = appointmentDAO.searchByFilters(null, dateFrom, dateTo, status);
        }
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private String statusToCode(String display) {
        return switch (display) {
            case "待检查" -> "PENDING";
            case "已确认" -> "CONFIRMED";
            case "已完成" -> "COMPLETED";
            case "已取消" -> "CANCELLED";
            default -> null;
        };
    }

    private void setupButtonListeners() {
        // 医生端不需要添加/编辑/删除预约，隐藏这些按钮
        getAddButton().setVisible(false);
        getEditButton().setVisible(false);
        getDeleteButton().setVisible(false);

        // 开始检查按钮
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
        JPanel buttonPanel = (JPanel) getAddButton().getParent();
        buttonPanel.add(startExamBtn, 0);
    }

    private void initializeTable() {
        tableModel = new AppointmentTableModel();
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
        setContent(scrollPane);
    }

    @Override
    public void refreshData() {
        LocalDate today = LocalDate.now();
        List<Appointment> result = appointmentDAO.searchByFilters(null, today, today, null);
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
        private String[] columnNames = {"ID", "用户", "检查组", "负责医生", "预约时间", "检查时间", "状态", "支付状态", "创建时间"};
        private List<Appointment> data;

        public void setData(List<Appointment> data) {
            this.data = data;
        }

        public Appointment getItemAt(int index) {
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
            Appointment appointment = data.get(row);
            switch (col) {
                case 0: return appointment.getId();
                case 1: return appointment.getUserName();
                case 2: return appointment.getGroupName();
                case 3: return appointment.getDoctorName() != null ? appointment.getDoctorName() : "";
                case 4: return appointment.getAppointmentTime();
                case 5: return appointment.getExamDate();
                case 6: return appointment.getStatusDisplay();
                case 7: return appointment.getPaymentStatusDisplay();
                case 8: return appointment.getCreatedAt();
                default: return null;
            }
        }
    }
}
