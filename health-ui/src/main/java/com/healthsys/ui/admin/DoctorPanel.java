package com.healthsys.ui.admin;

import com.healthsys.common.entity.Doctor;
import com.healthsys.dao.DoctorDAO;
import com.healthsys.ui.medical.CrudPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class DoctorPanel extends CrudPanel<Doctor> {
    private DoctorDAO doctorDAO;
    private JTable table;
    private DoctorTableModel tableModel;
    private JTextField idSearchField;
    private JTextField nameSearchField;

    public DoctorPanel() {
        doctorDAO = new DoctorDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        getSearchPanel().add(new JLabel("ID:"));
        idSearchField = new JTextField(8);
        getSearchPanel().add(idSearchField);

        getSearchPanel().add(new JLabel("姓名:"));
        nameSearchField = new JTextField(15);
        getSearchPanel().add(nameSearchField);

        getSearchButton().addActionListener(e -> searchDoctors());
    }

    private void searchDoctors() {
        String idStr = idSearchField.getText().trim();
        String name = nameSearchField.getText().trim();
        Long id = null;
        if (!idStr.isEmpty()) {
            try {
                id = Long.parseLong(idStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的ID", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        List<Doctor> result = doctorDAO.getAll();
        if (id != null || !name.isEmpty()) {
            result = doctorDAO.search(id, name);
        }
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private void setupButtonListeners() {
        getAddButton().addActionListener(e -> {
            DoctorDialog dialog = new DoctorDialog(null);
            if (dialog.showDialog() == DoctorDialog.OK_OPTION) {
                Doctor doctor = dialog.getDoctor();
                if (doctorDAO.add(doctor)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "医生添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "医生添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        getEditButton().addActionListener(e -> {
            Doctor selected = getSelectedDoctor();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的医生", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DoctorDialog dialog = new DoctorDialog(selected);
            if (dialog.showDialog() == DoctorDialog.OK_OPTION) {
                Doctor doctor = dialog.getDoctor();
                if (doctorDAO.update(doctor)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "医生更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "医生更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        getDeleteButton().addActionListener(e -> {
            Doctor selected = getSelectedDoctor();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的医生", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除医生 " + selected.getName() + " 吗?",
                    "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (doctorDAO.delete(selected.getDoctorId())) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "医生删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "医生删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void initializeTable() {
        tableModel = new DoctorTableModel();
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
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
        tableModel.setData(doctorDAO.getAll());
        tableModel.fireTableDataChanged();
    }

    private Doctor getSelectedDoctor() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getItemAt(selectedRow);
        }
        return null;
    }

    private class DoctorTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "登录名", "密码", "姓名", "科室", "职称", "状态", "创建时间"};
        private List<Doctor> data;

        public void setData(List<Doctor> data) {
            this.data = data;
        }

        public Doctor getItemAt(int index) {
            return data == null ? null : data.get(index);
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Doctor doctor = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> doctor.getDoctorId();
                case 1 -> doctor.getUsername();
                case 2 -> doctor.getPasswordHash();
                case 3 -> doctor.getName();
                case 4 -> doctor.getDepartment();
                case 5 -> doctor.getTitle();
                case 6 -> doctor.getStatusDisplay();
                case 7 -> doctor.getCreatedAt();
                default -> null;
            };
        }
    }
}
