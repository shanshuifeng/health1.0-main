package com.healthsys.ui.auth;

import com.healthsys.dao.UserDAO;
import com.healthsys.common.entity.Users;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
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

    public RegisterView() {
        setTitle("健康检查系统 - 用户注册");
        setSize(500, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("用户注册", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 姓名
        addLabelAndField(panel, gbc, 4, "姓名:", nameField = new JTextField(20));
        // 密码
        addLabelAndField(panel, gbc, 2, "密码:", passwordField = new JPasswordField(20));

        // 确认密码
        addLabelAndField(panel, gbc, 3, "确认密码:", confirmPasswordField = new JPasswordField(20));



        // 电话
        addLabelAndField(panel, gbc, 5, "电话:", phoneField = new JTextField(20));

        // 性别
        JLabel genderLabel = new JLabel("性别:");
        genderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(genderLabel, gbc);

        genderComboBox = new JComboBox<>(new String[]{"男", "女"});
        gbc.gridx = 1;
        panel.add(genderComboBox, gbc);

        // 出生日期
        JLabel birthLabel = new JLabel("出生日期:");
        birthLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(birthLabel, gbc);

        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        birthDateChooser.setDate(Date.from(LocalDate.now().minusYears(20).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        gbc.gridx = 1;
        panel.add(birthDateChooser, gbc);

        // 注册按钮
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);

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
            int genderVal = "MALE".equals(genderStr) ? 1 : "FEMALE".equals(genderStr) ? 2 : 0;
            newUser.setGender(genderVal);

            UserDAO userDAO = new UserDAO();
            if (userDAO.addUser(newUser)) {
                JOptionPane.showMessageDialog(this, "注册成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "注册失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private boolean validateInput() {
        // 验证用户名
        if (nameField.getText().isEmpty()) {
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
        String phone = phoneField.getText();
        if (phone.isEmpty()) {
            showError("电话不能为空");
            return false;
        }
        if (!phone.matches("\\d{11}")) {
            showError("请输入有效的11位手机号码");
            return false;
        }

        // 验证出生日期
        if (birthDateChooser.getDate() == null) {
            showError("请选择出生日期");
            return false;
            }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
}
