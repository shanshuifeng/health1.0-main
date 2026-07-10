package com.healthsys.ui.medical;

import com.healthsys.common.entity.Users;
import com.healthsys.ui.medical.CrudPanel;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class UserDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    private Users user;
    private int option = CANCEL_OPTION;

    // 对话框组件 — 与注册表单字段顺序一致
    private JTextField nameField;
    private JTextField passwordField;
    private JTextField phoneField;
    private JComboBox<String> genderComboBox;
    private JDateChooser birthDateChooser;
    private JTextField idNumberField;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);

    public UserDialog(Users user) {
        this.user = user != null ? user : new Users();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(500, 480);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(user.getId() == null ? "新增用户" : "编辑用户");

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 表单面板 — 7行：姓名、密码、电话、性别、出生日期、身份证号
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // 统一字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // === 字段顺序：姓名 → 密码 → 电话 → 性别 → 出生日期 → 身份证号 ===
        // 与注册表单 (RegisterView) 保持一致

        // 1. 姓名
        addFormField(formPanel, "姓名:", nameField = createStyledTextField(user.getName(), fieldFont), labelFont);

        // 2. 密码（明文可见）
        addFormField(formPanel, "密码:", passwordField = createStyledPasswordField(), labelFont);

        // 3. 电话（注册时标签为"电话"，这里保持一致）
        addFormField(formPanel, "电话:", phoneField = createStyledTextField(user.getPhone(), fieldFont), labelFont);

        // 4. 性别（"男"/"女" 与注册表单一致）
        addComboBoxField(formPanel, "性别:", genderComboBox = createStyledComboBox(
                new String[]{"男", "女"}), user.getGenderDisplay(), labelFont);

        // 5. 出生日期（JDateChooser 日历控件，与注册表单一致）
        JLabel birthLabel = new JLabel("出生日期:");
        birthLabel.setFont(labelFont);
        formPanel.add(birthLabel);

        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        birthDateChooser.setMaxSelectableDate(new Date()); // 限制最大日期为今天
        // 编辑时预填用户的出生日期；新增时默认20年前
        if (user.getBirthDate() != null) {
            Date d = Date.from(user.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            birthDateChooser.setDate(d);
        } else {
            Date defaultDate = Date.from(LocalDate.now().minusYears(20).atStartOfDay(ZoneId.systemDefault()).toInstant());
            birthDateChooser.setDate(defaultDate);
        }
        formPanel.add(birthDateChooser);

        // 6. 身份证号（附加管理字段）
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
        JTextField field = new JTextField(text != null ? text : "");
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JTextField createStyledPasswordField() {
        JTextField field = new JTextField("");
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
        user.setName(nameField.getText().trim());
        String newPassword = passwordField.getText().trim();
        if (!newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }
        user.setPhone(phoneField.getText().trim());
        // 性别映射："男"/"女" — 与注册表单一致
        String genderStr = (String) genderComboBox.getSelectedItem();
        user.setGender("男".equals(genderStr) ? 1 : 2);
        // 出生日期从 JDateChooser 获取
        if (birthDateChooser.getDate() != null) {
            user.setBirthDate(birthDateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }
        user.setIdNumber(idNumberField.getText().trim());
        return user;
    }
}
