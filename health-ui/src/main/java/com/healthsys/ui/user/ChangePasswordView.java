package com.healthsys.ui.user;

import com.healthsys.service.AuthService;
import com.healthsys.common.entity.Users;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordView implements AuthService.LoginListener {
    private JFrame loginFrame;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private AuthService loginController;

    public ChangePasswordView() {
        this.loginController = new AuthService();
        this.loginController.setLoginListener(this);
    }

    public void showLogin() {
        loginFrame = new JFrame("健康管理系统登录");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 350);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel titleLabel = new JLabel("健康管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 10, 0);
        gbc.fill = GridBagConstraints.NONE;

        JLabel phoneLabel = new JLabel("手机号:");
        phoneLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(phoneLabel, gbc);

        phoneField = new JTextField();
        phoneField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        phoneField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(phoneField, gbc);

        JLabel passwordLabel = new JLabel("密  码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginButton.setPreferredSize(new Dimension(60, 35));
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> performLogin());
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(loginButton, gbc);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);
        loginFrame.add(mainPanel);

        passwordField.addActionListener(e -> performLogin());

        loginFrame.setVisible(true);
    }

    private void performLogin() {
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        loginController.handleLogin(phone, password);
    }

    public void showChangePasswordDialog(Users user) {
        JDialog dialog = new JDialog(loginFrame, "修改密码", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(loginFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("首次登录，请修改密码", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel newPasswordLabel = new JLabel("新密码:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(newPasswordLabel, gbc);

        JPasswordField newPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(newPasswordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(confirmPasswordLabel, gbc);

        JPasswordField confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(confirmPasswordField, gbc);

        JButton confirmButton = new JButton("确认");
        confirmButton.setBackground(new Color(0, 102, 204));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            loginController.handleChangePassword(user, newPassword, confirmPassword);
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(confirmButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    @Override
    public void onLoginSuccess(Users user) {
        loginFrame.dispose();
        new MainView(user);
    }

    @Override
    public void onFirstLogin(Users user) {
        showChangePasswordDialog(user);
    }

    @Override
    public void onLoginFailed(String errorMessage) {
        JOptionPane.showMessageDialog(loginFrame, errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onPasswordChangeSuccess(Users user) {
        JOptionPane.showMessageDialog(loginFrame, "密码修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.dispose();
        new MainView(user);
    }

    @Override
    public void onPasswordChangeFailed(String errorMessage) {
        JOptionPane.showMessageDialog(loginFrame, errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
    }

}
