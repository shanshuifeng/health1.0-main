package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.ui.medical.AppointmentDialog;
import com.healthsys.dao.AppointmentDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class AppointmentPanel extends CrudPanel<Appointment> {
    private AppointmentDAO appointmentDAO;
    private JTable table;
    private AppointmentTableModel tableModel;
    private JTextField userNameSearchField;

    public AppointmentPanel() {
        appointmentDAO = new AppointmentDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        // 添加查询字段
        getSearchPanel().add(new JLabel("用户名:"));
        userNameSearchField = new JTextField(15);
        getSearchPanel().add(userNameSearchField);


        // 设置查询按钮事件
        getSearchButton().addActionListener(e -> searchAppointments());
    }

    private void searchAppointments() {
        String userName = userNameSearchField.getText().trim();

        List<Appointment> result = appointmentDAO.search(userName);
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private void setupButtonListeners() {
        // 添加按钮事件
        getAddButton().addActionListener(e -> {
            AppointmentDialog dialog = new AppointmentDialog(null);
            if (dialog.showDialog() == AppointmentDialog.OK_OPTION) {
                Appointment newAppointment = dialog.getAppointment();
                if (appointmentDAO.add(newAppointment)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "预约添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "预约添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 编辑按钮事件
        getEditButton().addActionListener(e -> {
            Appointment selected = getSelectedAppointment();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的预约", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AppointmentDialog dialog = new AppointmentDialog(selected);
            if (dialog.showDialog() == AppointmentDialog.OK_OPTION) {
                Appointment updatedAppointment = dialog.getAppointment();
                if (appointmentDAO.update(updatedAppointment)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "预约更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "预约更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 删除按钮事件
        getDeleteButton().addActionListener(e -> {
            Appointment selected = getSelectedAppointment();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的预约", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除预约ID为 " + selected.getId() + " 的记录吗?",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (appointmentDAO.delete(selected.getId())) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "预约删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "预约删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void initializeTable() {
        tableModel = new AppointmentTableModel();
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
        tableModel.setData(appointmentDAO.getAll());
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
        private String[] columnNames = {"ID", "用户", "检查组", "预约时间", "检查时间", "状态", "支付状态", "创建时间"};
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
                case 3: return appointment.getAppointmentTime();
                case 4: return appointment.getExamDate();
                case 5: return appointment.getStatus();
                case 6: return appointment.getPaymentStatus() ? "已支付" : "未支付";
                case 7: return appointment.getCreatedAt();
                default: return null;
            }
        }
    }
}
