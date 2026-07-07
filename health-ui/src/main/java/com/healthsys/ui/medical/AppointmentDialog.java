package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.ui.medical.CrudPanel;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.util.Date;

public class AppointmentDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private Appointment appointment;
    private int option = CANCEL_OPTION;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);
    private final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private final Font FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    // 对话框组件
    private JTextField userIdField;
    private JTextField groupIdField;
    private JComboBox<String> statusComboBox;
    private JCheckBox paymentStatusCheckBox;
    private JDateChooser appointmentDateChooser;
    private JDateChooser examDateChooser;

    public AppointmentDialog(Appointment appointment) {
        this.appointment = appointment != null ? appointment : new Appointment();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(500, 450); // 适当增加高度以适应时间选择器
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(appointment.getId() == null ? "新增预约" : "编辑预约");

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // 用户ID字段
        addFormField(formPanel, "用户ID:",
                userIdField = createStyledTextField(
                        appointment.getUserId() != null ? appointment.getUserId().toString() : ""
                )
        );

        // 检查组ID字段
        addFormField(formPanel, "检查组ID:",
                groupIdField = createStyledTextField(
                        appointment.getGroupId() != null ? appointment.getGroupId().toString() : ""
                )
        );

        // 预约时间选择器
        addFormField(formPanel, "预约时间:", createDateChooserPanel(true));

        // 检查时间选择器
        addFormField(formPanel, "检查时间:", createDateChooserPanel(false));

        // 状态下拉框
        addFormField(formPanel, "状态:", createStatusComboBox());

        // 支付状态复选框
        addFormField(formPanel, "支付状态:", createPaymentCheckBox());

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = CrudPanel.createStyledButton("确定", MAIN_COLOR);
        okButton.addActionListener(e -> {
            if (validateInput()) {
                option = OK_OPTION;
                dispose();
            }
        });

        JButton cancelButton = CrudPanel.createStyledButton("取消", new Color(204, 153, 153));
        cancelButton.addActionListener(e -> {
            option = CANCEL_OPTION;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addFormField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        panel.add(label);
        panel.add(field);
    }

    private JPanel createDateChooserPanel(boolean isAppointmentTime) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd HH:mm");
        dateChooser.getCalendarButton().setBackground(MAIN_COLOR);
        dateChooser.getCalendarButton().setForeground(Color.WHITE);
        dateChooser.setFont(FIELD_FONT);

        if (isAppointmentTime) {
            appointmentDateChooser = dateChooser;
            if (appointment.getAppointmentTime() != null) {
                dateChooser.setDate(Date.from(
                        appointment.getAppointmentTime().atZone(ZoneId.systemDefault()).toInstant()
                ));
            }
        } else {
            examDateChooser = dateChooser;
            if (appointment.getExamDate() != null) {
                dateChooser.setDate(Date.from(
                        appointment.getExamDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
                ));
            }
        }

        panel.add(dateChooser, BorderLayout.CENTER);
        return panel;
    }

    private JComboBox<String> createStatusComboBox() {
        statusComboBox = new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "COMPLETED"});
        statusComboBox.setFont(FIELD_FONT);
        statusComboBox.setBackground(Color.WHITE);
        statusComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        if (appointment.getStatus() != null) {
            statusComboBox.setSelectedItem(appointment.getStatus());
        } else {
            statusComboBox.setSelectedItem("PENDING");
        }

        return statusComboBox;
    }

    private JPanel createPaymentCheckBox() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        paymentStatusCheckBox = new JCheckBox("已支付");
        paymentStatusCheckBox.setFont(FIELD_FONT);
        paymentStatusCheckBox.setBackground(Color.WHITE);
        paymentStatusCheckBox.setSelected(
                appointment.getPaymentStatus() != null && appointment.getPaymentStatus()
        );

        panel.add(paymentStatusCheckBox);
        return panel;
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private boolean validateInput() {
        try {
            // 验证用户ID和检查组ID
            Long.parseLong(userIdField.getText());
            Long.parseLong(groupIdField.getText());

            // 验证预约时间和检查时间
            if (appointmentDateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "请选择预约时间", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (examDateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "请选择检查时间", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的用户ID和检查组ID", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public int showDialog() {
        setVisible(true);
        return option;
    }

    public Appointment getAppointment() {
        appointment.setUserId(Long.parseLong(userIdField.getText()));
        appointment.setGroupId(Long.parseLong(groupIdField.getText()));
        appointment.setAppointmentTime(
                appointmentDateChooser.getDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        java.time.LocalDateTime examDateTime = examDateChooser.getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        appointment.setExamDate(examDateTime.toLocalDate());
        appointment.setExamTimeSlot(examDateTime.getHour() < 12 ? "上午" : "下午");
        appointment.setStatus((String) statusComboBox.getSelectedItem());
        appointment.setPaymentStatus(paymentStatusCheckBox.isSelected());
        return appointment;
    }
}
