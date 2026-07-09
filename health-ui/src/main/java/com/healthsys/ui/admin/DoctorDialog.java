package com.healthsys.ui.admin;

import com.healthsys.common.entity.Doctor;
import com.healthsys.ui.medical.CrudPanel;

import javax.swing.*;
import java.awt.*;

public class DoctorDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    private int option = CANCEL_OPTION;
    private Doctor doctor;

    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField nameField;
    private JTextField departmentField;
    private JTextField titleField;
    private JComboBox<String> statusComboBox;

    public DoctorDialog(Doctor doctor) {
        this.doctor = doctor != null ? doctor : new Doctor();
        initializeUI();
    }

    private void initializeUI() {
        setTitle(doctor.getDoctorId() == null ? "新增医生" : "编辑医生");
        setModal(true);
        setSize(500, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 12, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        addFormField(formPanel, "用户名:", usernameField = createStyledTextField(doctor.getUsername(), fieldFont), labelFont);
        addFormField(formPanel, "密码:", passwordField = createStyledPasswordField(), labelFont);
        addFormField(formPanel, "姓名:", nameField = createStyledTextField(doctor.getName(), fieldFont), labelFont);
        addFormField(formPanel, "科室:", departmentField = createStyledTextField(doctor.getDepartment(), fieldFont), labelFont);
        addFormField(formPanel, "职称:", titleField = createStyledTextField(doctor.getTitle(), fieldFont), labelFont);
        addFormField(formPanel, "状态:", statusComboBox = createStyledComboBox(new String[]{"在职", "离职"}), labelFont);

        if (doctor.getStatus() != null) {
            statusComboBox.setSelectedIndex(doctor.getStatus() == 1 ? 0 : 1);
        }

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton okButton = CrudPanel.createStyledButton("确定", new Color(70, 104, 197));
        okButton.addActionListener(e -> {
            option = OK_OPTION;
            dispose();
        });

        JButton cancelButton = CrudPanel.createStyledButton("取消", new Color(204, 153, 153));
        cancelButton.addActionListener(e -> {
            option = CANCEL_OPTION;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, Font font) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        panel.add(label);
        panel.add(field);
    }

    private JTextField createStyledTextField(String text, Font font) {
        JTextField field = new JTextField(text != null ? text : "");
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JTextField createStyledPasswordField() {
        JTextField field = new JTextField(doctor.getPasswordHash() != null ? doctor.getPasswordHash() : "");
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return comboBox;
    }

    public int showDialog() {
        setVisible(true);
        return option;
    }

    public Doctor getDoctor() {
        doctor.setUsername(usernameField.getText().trim());
        String pwd = passwordField.getText();
        if (pwd != null && !pwd.isEmpty()) {
            doctor.setPasswordHash(pwd);
        }
        doctor.setName(nameField.getText().trim());
        doctor.setDepartment(departmentField.getText().trim());
        doctor.setTitle(titleField.getText().trim());
        doctor.setStatus(statusComboBox.getSelectedIndex() == 0 ? 1 : 0);
        return doctor;
    }
}
