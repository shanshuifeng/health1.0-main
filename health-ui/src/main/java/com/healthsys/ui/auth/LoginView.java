package com.healthsys.ui.auth;

import com.healthsys.service.AuthService;
import com.healthsys.common.entity.Users;
import com.healthsys.ui.admin.AdminMainView;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

public class LoginView extends JFrame {
    private static final String APP_NAME = "健康检查系统";
    private static final String PREFS_KEY = "health_system_prefs";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 400;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Microsoft YaHei", Font.BOLD, 14);
    private static final int COMPONENT_PADDING = 30;
    private static final int FIELD_GAP = 10;
    private static final int ROW_GAP = 15;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JCheckBox rememberMeCheck;
    private JCheckBox showPasswordCheck;
    private AuthService authService;
    private volatile boolean isLoggingIn = false; // 防止重复点击

    public LoginView() {
        initUI();
        initController();
        loadPreferences();
    }

    private void initUI() {
        setTitle(APP_NAME + " - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
                COMPONENT_PADDING, COMPONENT_PADDING,
                COMPONENT_PADDING, COMPONENT_PADDING));
        mainPanel.setBackground(BACKGROUND_COLOR);

        mainPanel.add(createTitlePanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, COMPONENT_PADDING, 0));

        JLabel titleLabel = new JLabel(APP_NAME, JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titlePanel.add(titleLabel);

        return titlePanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        usernameField = createInputField();
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');

        formPanel.add(createFieldPanel("账号:", usernameField));
        formPanel.add(Box.createVerticalStrut(ROW_GAP));
        formPanel.add(createFieldPanel("密码:", passwordField));
        formPanel.add(Box.createVerticalStrut(ROW_GAP));

        JLabel hintLabel = new JLabel("支持手机号或用户名登录", JLabel.CENTER);
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        hintLabel.setForeground(Color.GRAY);
        formPanel.add(hintLabel);
        formPanel.add(Box.createVerticalStrut(ROW_GAP));

        formPanel.add(createOptionsPanel());
        formPanel.add(Box.createVerticalStrut(ROW_GAP * 2));

        formPanel.add(createButtonPanel());

        return formPanel;
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, FIELD_GAP, 0));
        panel.setOpaque(false);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(LABEL_FONT);
        jLabel.setPreferredSize(new Dimension(60, 25));

        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 30));

        panel.add(jLabel);
        panel.add(field);

        return panel;
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(250, 30));
        return field;
    }

    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panel.setOpaque(false);

        rememberMeCheck = new JCheckBox("记住账号");
        rememberMeCheck.setFont(LABEL_FONT);

        showPasswordCheck = new JCheckBox("显示密码");
        showPasswordCheck.setFont(LABEL_FONT);
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? '\0' : '•');
        });

        panel.add(rememberMeCheck);
        panel.add(showPasswordCheck);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);

        loginButton = createButton("登录", Color.BLACK, e -> handleLogin());
        loginButton.setMnemonic(KeyEvent.VK_ENTER);
        getRootPane().setDefaultButton(loginButton);

        registerButton = createButton("注册", Color.BLACK, e -> showRegistrationDialog());

        panel.add(loginButton);
        panel.add(registerButton);

        return panel;
    }

    private JButton createButton(String text, Color bgColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.addActionListener(listener);
        return button;
    }

    private void initController() {
        authService = new AuthService();
    }

    private void redirectBasedOnRole(Users user) {
        if ("ADMIN".equals(user.getRole())) {
            openAdminInterface();
        } else if ("DOCTOR".equals(user.getRole())) {
            openMedicalInterface(user);
        } else {
            openUserInterface(user);
        }
    }

    private void openMedicalInterface(Users user) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("医疗健康管理系统");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);
                frame.setLocationRelativeTo(null);
                com.healthsys.ui.medical.MainView panel = new com.healthsys.ui.medical.MainView(user);
                frame.add(panel);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "医护界面加载失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void openAdminInterface() {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("管理员管理控制台");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);
                frame.setLocationRelativeTo(null);
                AdminMainView panel = new AdminMainView();
                frame.add(panel);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "管理员界面加载失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void openUserInterface(Users user) {
        SwingUtilities.invokeLater(() -> {
            try {
                new com.healthsys.ui.user.MainView(user);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "界面加载失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void handleLogin() {
        // 防止重复点击
        if (isLoggingIn) {
            return;
        }
        isLoggingIn = true;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showWarning("请输入账号（手机号或用户名）");
            usernameField.requestFocus();
            isLoggingIn = false;
            return;
        }

        if (password.isEmpty()) {
            showWarning("请输入密码");
            passwordField.requestFocus();
            isLoggingIn = false;
            return;
        }

        // 设置登录监听器（在 UI 线程中执行）
        authService.setLoginListener(new AuthService.LoginListener() {
            private int attempt = 0;

            @Override
            public void onLoginSuccess(Users user) {
                SwingUtilities.invokeLater(() -> {
                    savePreferences();
                    dispose();
                    redirectBasedOnRole(user);
                    isLoggingIn = false;
                });
            }

            @Override
            public void onLoginFailed(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    attempt++;
                    if (attempt == 1) {
                        authService.handleDoctorLogin(username, password);
                    } else if (attempt == 2) {
                        authService.handleAdminLogin(username, password);
                    } else {
                        JOptionPane.showMessageDialog(
                                LoginView.this,
                                "账号或密码错误，请重试",
                                "登录失败",
                                JOptionPane.ERROR_MESSAGE
                        );
                        passwordField.setText("");
                        passwordField.requestFocus();
                        attempt = 0;
                        isLoggingIn = false;
                    }
                });
            }

            @Override
            public void onFirstLogin(Users user) {
                SwingUtilities.invokeLater(() -> {
                    savePreferences();
                    dispose();
                    redirectBasedOnRole(user);
                    isLoggingIn = false;
                });
            }

            @Override public void onPasswordChangeSuccess(Users user) {}
            @Override public void onPasswordChangeFailed(String errorMessage) {}
        });

        // 开始登录尝试
        try {
            authService.handleLogin(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            showError("登录异常", "登录过程中发生错误: " + e.getMessage());
            isLoggingIn = false;
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "输入验证",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void loadPreferences() {
        Preferences prefs = Preferences.userRoot().node(PREFS_KEY);
        String savedUsername = prefs.get("username", "");
        if (!savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberMeCheck.setSelected(true);
            passwordField.requestFocus();
        }
    }

    private void savePreferences() {
        Preferences prefs = Preferences.userRoot().node(PREFS_KEY);
        if (rememberMeCheck.isSelected()) {
            prefs.put("username", usernameField.getText().trim());
        } else {
            prefs.remove("username");
        }
    }

    private void showRegistrationDialog() {
        EventQueue.invokeLater(() -> {
            RegisterView registerView = new RegisterView();
            registerView.setVisible(true);
        });
    }
}