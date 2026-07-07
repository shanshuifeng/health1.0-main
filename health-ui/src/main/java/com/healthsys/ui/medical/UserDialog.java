package com.healthsys.ui.medical;

import com.healthsys.common.entity.Users;
import com.healthsys.ui.medical.CrudPanel;
import javax.swing.*;
import java.awt.*;

public class UserDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    private Users user;
    private int option = CANCEL_OPTION;

    // 对话框组件
    private JTextField phoneField;
    private JTextField nameField;
    private JComboBox<String> genderComboBox;
    private JTextField idNumberField;
    private JPasswordField passwordField;
    private JTextField birthDateField;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);

    public UserDialog(Users user) {
        this.user = user != null ? user : new Users();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(500, 450);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(user.getId() == null ? "新增用户" : "编辑用户");

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // 统一字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 添加表单字段
        addFormField(formPanel, "手机号:", phoneField = createStyledTextField(user.getPhone(), fieldFont), labelFont);
        addFormField(formPanel, "密码:", passwordField = createStyledPasswordField(), labelFont);
        addFormField(formPanel, "姓名:", nameField = createStyledTextField(user.getName(), fieldFont), labelFont);
        addFormField(formPanel, "出生日期:", birthDateField = createStyledTextField("", fieldFont), labelFont);

        // 添加下拉框
        addComboBoxField(formPanel, "性别:", genderComboBox = createStyledComboBox(
                new String[]{"MALE", "FEMALE"}), user.getGenderDisplay(), labelFont);
        // 移除角色字段 — 新架构通过doctors/admins表区分

        addFormField(formPanel, "身份证号:", idNumberField = createStyledTextField(user.getIdNumber(), fieldFont), labelFont);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = CrudPanel.createStyledButton("确定", MAIN_COLOR);
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
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, Font font) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        panel.add(label);
        panel.add(field);
    }

    private void addComboBoxField(JPanel panel, String labelText, JComboBox<String> comboBox,
                                  String selectedValue, Font font) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        panel.add(label);

        if (selectedValue != null) {
            comboBox.setSelectedItem(selectedValue);
        }
        panel.add(comboBox);
    }

    private JTextField createStyledTextField(String text, Font font) {
        JTextField field = new JTextField(text);
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
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

    public Users getUser() {
        user.setPhone(phoneField.getText());
        user.setPassword(new String(passwordField.getPassword()));
        user.setName(nameField.getText());
        String genderStr = (String) genderComboBox.getSelectedItem();
        user.setGender("MALE".equals(genderStr) ? 1 : "FEMALE".equals(genderStr) ? 2 : 0);
        user.setIdNumber(idNumberField.getText());
        return user;
    }
}
