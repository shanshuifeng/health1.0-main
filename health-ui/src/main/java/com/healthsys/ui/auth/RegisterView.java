package com.healthsys.ui.auth;

import com.healthsys.dao.UserDAO;
import com.healthsys.common.entity.Users;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class RegisterView extends JFrame {
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField nameField;
    private JTextField phoneField;
    private JComboBox<String> genderComboBox;
    private JDateChooser birthDateChooser;
    private JButton registerButton;
    private JButton passwordToggleBtn;
    private JButton confirmPasswordToggleBtn;
    private JLabel passwordErrorLabel;
    private JLabel phoneErrorLabel;
    private JCheckBox agreementCheckBox;

    public RegisterView() {
        setTitle("健康检查系统 - 用户注册");
        setSize(550, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("用户注册", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);

        // 姓名
        addLabelWithTipAndField(panel, gbc, 1, "姓名:", nameField = new JTextField(20), "请输入真实姓名");

        // 密码
        addPasswordFieldWithToggle(panel, gbc, 2, "密码:", passwordField = new JPasswordField(20),
                passwordToggleBtn = createPasswordToggleBtn(passwordField), "至少6位字符");

        // 确认密码
        addConfirmPasswordFieldWithToggle(panel, gbc, 3, "确认密码:", confirmPasswordField = new JPasswordField(20),
                confirmPasswordToggleBtn = createPasswordToggleBtn(confirmPasswordField),
                "请再次输入密码");

        // 密码错误提示
        passwordErrorLabel = new JLabel("两次密码输入不一致");
        passwordErrorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        passwordErrorLabel.setForeground(Color.RED);
        passwordErrorLabel.setVisible(false);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(passwordErrorLabel, gbc);

        // 电话
        addPhoneFieldWithValidation(panel, gbc, 5, "电话:", phoneField = new JTextField(20), "请输入11位手机号");

        // 电话错误提示
        phoneErrorLabel = new JLabel("请输入有效的11位手机号码");
        phoneErrorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        phoneErrorLabel.setForeground(Color.RED);
        phoneErrorLabel.setVisible(false);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(phoneErrorLabel, gbc);

        // 性别
        JLabel genderLabel = new JLabel("性别:");
        genderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        panel.add(genderLabel, gbc);

        genderComboBox = new JComboBox<>(new String[]{"男", "女"});
        genderComboBox.setSelectedIndex(0); // 默认选中第一个
        gbc.gridx = 1;
        panel.add(genderComboBox, gbc);

        JLabel genderTip = new JLabel("(请选择性别)");
        genderTip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        genderTip.setForeground(Color.GRAY);
        gbc.gridx = 2;
        panel.add(genderTip, gbc);

        // 出生日期
        JLabel birthLabel = new JLabel("出生日期:");
        birthLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 8;
        panel.add(birthLabel, gbc);

        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        // 设置默认日期为20年前的今天
        Date defaultDate = Date.from(LocalDate.now().minusYears(20).atStartOfDay(ZoneId.systemDefault()).toInstant());
        birthDateChooser.setDate(defaultDate);
        birthDateChooser.setMaxSelectableDate(new Date()); // 限制最大日期为今天
        gbc.gridx = 1;
        panel.add(birthDateChooser, gbc);

        JLabel birthTip = new JLabel("(请选择您的出生日期)");
        birthTip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        birthTip.setForeground(Color.GRAY);
        gbc.gridx = 2;
        panel.add(birthTip, gbc);

        // 用户协议复选框
        agreementCheckBox = new JCheckBox("我已阅读并同意《用户服务协议》和《隐私政策》");
        agreementCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        panel.add(agreementCheckBox, gbc);

        // 注册按钮
        registerButton = new JButton("立即注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setEnabled(false);
        registerButton.setBackground(Color.GRAY);
        registerButton.setForeground(Color.WHITE);
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(true);
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);

        // 添加输入验证监听器
        addValidationListeners();

        registerButton.addActionListener(e -> {
            if (!validateInput()) {
                return;
            }

            Users newUser = new Users();
            newUser.setName(nameField.getText().trim());
            newUser.setPassword(new String(passwordField.getPassword()));
            newUser.setStatus(1);
            newUser.setFirstLogin(false);

            newUser.setPhone(phoneField.getText().trim());
            newUser.setBirthDate(birthDateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            String genderStr = (String) genderComboBox.getSelectedItem();
            int genderVal = "男".equals(genderStr) ? 1 : 2;
            newUser.setGender(genderVal);

            UserDAO userDAO = new UserDAO();
            if (userDAO.addUser(newUser)) {
                JOptionPane.showMessageDialog(this, "注册成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "注册失败，可能是手机号已被注册", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }

    /**
     * 创建密码显示/隐藏切换按钮
     */
    private JButton createPasswordToggleBtn(JPasswordField passwordField) {
        JButton toggleBtn = new JButton("👁️");
        toggleBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        toggleBtn.setPreferredSize(new Dimension(30, 25));
        toggleBtn.setBorder(BorderFactory.createEmptyBorder());
        toggleBtn.setBackground(Color.WHITE);

        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\u2022') {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("🙈");
            } else {
                passwordField.setEchoChar('\u2022');
                toggleBtn.setText("👁️");
            }
        });
        return toggleBtn;
    }

    /**
     * 添加带提示词的标签和输入框
     */
    private void addLabelWithTipAndField(JPanel panel, GridBagConstraints gbc, int row,
                                         String labelText, JComponent field, String tipText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);

        JLabel tipLabel = new JLabel("(" + tipText + ")");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tipLabel.setForeground(Color.GRAY);
        gbc.gridx = 2;
        panel.add(tipLabel, gbc);
    }

    /**
     * 添加带切换按钮的密码框
     */
    private void addPasswordFieldWithToggle(JPanel panel, GridBagConstraints gbc, int row,
                                            String labelText, JPasswordField passwordField,
                                            JButton toggleBtn, String tipText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        JPanel passwordPanel = new JPanel(new BorderLayout(2, 0));
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleBtn, BorderLayout.EAST);
        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);

        JLabel tipLabel = new JLabel("(" + tipText + ")");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tipLabel.setForeground(Color.GRAY);
        gbc.gridx = 2;
        panel.add(tipLabel, gbc);
    }

    /**
     * 添加带切换按钮的确认密码框
     */
    private void addConfirmPasswordFieldWithToggle(JPanel panel, GridBagConstraints gbc, int row,
                                                   String labelText, JPasswordField passwordField,
                                                   JButton toggleBtn, String tipText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        JPanel passwordPanel = new JPanel(new BorderLayout(2, 0));
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleBtn, BorderLayout.EAST);
        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);

        JLabel tipLabel = new JLabel("(" + tipText + ")");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tipLabel.setForeground(Color.GRAY);
        gbc.gridx = 2;
        panel.add(tipLabel, gbc);
    }

    /**
     * 添加带验证的电话输入框
     */
    private void addPhoneFieldWithValidation(JPanel panel, GridBagConstraints gbc, int row,
                                             String labelText, JTextField phoneField, String tipText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        JLabel tipLabel = new JLabel("(" + tipText + ")");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tipLabel.setForeground(Color.GRAY);
        gbc.gridx = 2;
        panel.add(tipLabel, gbc);
    }

    /**
     * 添加输入验证监听器
     */
    private void addValidationListeners() {
        // 确认密码实时校验
        confirmPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validatePasswordMatch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validatePasswordMatch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validatePasswordMatch(); }
        });

        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validatePasswordMatch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validatePasswordMatch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validatePasswordMatch(); }
        });

        // 手机号失焦校验
        phoneField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String phone = phoneField.getText().trim();
                if (!phone.isEmpty() && !phone.matches("\\d{11}")) {
                    phoneErrorLabel.setVisible(true);
                } else {
                    phoneErrorLabel.setVisible(false);
                }
                updateRegisterButtonState();
            }
        });

        // 手机号输入时也触发更新
        phoneField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateRegisterButtonState(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateRegisterButtonState(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateRegisterButtonState(); }
        });

        // 协议复选框监听
        agreementCheckBox.addItemListener(e -> updateRegisterButtonState());

        // 姓名输入监听
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateRegisterButtonState(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateRegisterButtonState(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateRegisterButtonState(); }
        });

        // 性别选择监听
        genderComboBox.addItemListener(e -> updateRegisterButtonState());

        // 日期选择监听
        birthDateChooser.addPropertyChangeListener("date", e -> updateRegisterButtonState());
    }

    /**
     * 校验两次密码是否一致
     */
    private void validatePasswordMatch() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!confirmPassword.isEmpty()) {
            boolean match = password.equals(confirmPassword);
            passwordErrorLabel.setVisible(!match);
        } else {
            passwordErrorLabel.setVisible(false);
        }
        updateRegisterButtonState();
    }

    /**
     * 更新注册按钮状态
     */
    private void updateRegisterButtonState() {
        boolean allValid = true;

        // 检查姓名
        if (nameField.getText().trim().isEmpty()) {
            allValid = false;
        }

        // 检查密码
        String password = new String(passwordField.getPassword());
        if (password.isEmpty() || password.length() < 6) {
            allValid = false;
        }

        // 检查确认密码
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (confirmPassword.isEmpty()) {
            allValid = false;
        }
        if (!password.equals(confirmPassword)) {
            allValid = false;
        }

        // 检查手机号（11位数字）
        String phone = phoneField.getText().trim();
        if (phone.isEmpty() || !phone.matches("^\\d{11}$")) {
            allValid = false;
        }

        // 检查出生日期
        if (birthDateChooser.getDate() == null) {
            allValid = false;
        }

        // 检查协议勾选
        if (!agreementCheckBox.isSelected()) {
            allValid = false;
        }

        // 更新按钮状态
        registerButton.setEnabled(allValid);
        if (allValid) {
            registerButton.setBackground(new Color(65, 105, 225));
            registerButton.setForeground(Color.WHITE);
            registerButton.setOpaque(true);
            registerButton.setBorderPainted(true);
        } else {
            registerButton.setBackground(Color.GRAY);
            registerButton.setForeground(Color.WHITE);
            registerButton.setOpaque(true);
            registerButton.setBorderPainted(true);
        }
    }

    private boolean validateInput() {
        // 验证用户名
        if (nameField.getText().trim().isEmpty()) {
            showError("用户名不能为空");
            return false;
        }

        // 验证密码
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (password.isEmpty()) {
            showError("密码不能为空");
            return false;
        }
        if (password.length() < 6) {
            showError("密码长度不能少于6位");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            return false;
        }

        // 验证电话
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            showError("电话不能为空");
            return false;
        }
        if (!phone.matches("^\\d{11}$")) {
            showError("请输入有效的11位手机号码");
            return false;
        }

        // 验证出生日期
        if (birthDateChooser.getDate() == null) {
            showError("请选择出生日期");
            return false;
        }

        // 验证协议勾选
        if (!agreementCheckBox.isSelected()) {
            showError("请阅读并同意用户协议");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
}