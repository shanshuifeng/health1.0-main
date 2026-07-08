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
    private JComboBox<String> loginTypeCombo;
    private AuthService authService;

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

        // Main container with proper padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
                COMPONENT_PADDING, COMPONENT_PADDING,
                COMPONENT_PADDING, COMPONENT_PADDING));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Application title
        mainPanel.add(createTitlePanel(), BorderLayout.NORTH);

        // Login form
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

        // Input fields
        usernameField = createInputField();
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');

        formPanel.add(createFieldPanel("用户名:", usernameField));
        formPanel.add(Box.createVerticalStrut(ROW_GAP));
        formPanel.add(createFieldPanel("密码:", passwordField));
        formPanel.add(Box.createVerticalStrut(ROW_GAP));

        // 登录类型选择
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        typePanel.setOpaque(false);
        loginTypeCombo = new JComboBox<>(new String[]{"普通用户", "管理员", "医生"});
        loginTypeCombo.setFont(LABEL_FONT);
        typePanel.add(new JLabel("登录身份:"));
        typePanel.add(loginTypeCombo);
        formPanel.add(typePanel);
        formPanel.add(Box.createVerticalStrut(ROW_GAP));

        // Options panel
        formPanel.add(createOptionsPanel());
        formPanel.add(Box.createVerticalStrut(ROW_GAP * 2));

        // Buttons panel
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

        rememberMeCheck = new JCheckBox("记住用户名");
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
        authService.setLoginListener(new AuthService.LoginListener() {
            @Override
            public void onLoginSuccess(Users user) {
                savePreferences();
                dispose();
                redirectBasedOnRole(user);
            }

            @Override
            public void onLoginFailed(String errorMessage) {
                EventQueue.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            LoginView.this,
                            errorMessage,
                            "登录失败",
                            JOptionPane.ERROR_MESSAGE
                    );
                    passwordField.setText("");
                    passwordField.requestFocus();
                });
            }

            @Override public void onFirstLogin(Users user) {
                savePreferences();
                dispose();
                redirectBasedOnRole(user);
            }
            @Override public void onPasswordChangeSuccess(Users user) {}
            @Override public void onPasswordChangeFailed(String errorMessage) {}
        });
    }

    private void redirectBasedOnRole(Users user) {
        if ("ADMIN".equals(user.getRole())) {
            openAdminInterface();
        } else if ("DOCTOR".equals(user.getRole())) {
            openMedicalInterface();
        } else {
            openUserInterface(user);
        }
    }

    private void openMedicalInterface() {
        try {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("医疗健康管理系统");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);
                frame.setLocationRelativeTo(null);

                com.healthsys.ui.medical.MainView panel =
                        new com.healthsys.ui.medical.MainView();
                frame.add(panel);
                frame.setVisible(true);
            });
        } catch (Exception e) {
            showError("医护界面加载失败", e.getMessage());
        }
    }

    private void openAdminInterface() {
        try {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("管理员管理控制台");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);
                frame.setLocationRelativeTo(null);

                AdminMainView panel = new AdminMainView();
                frame.add(panel);
                frame.setVisible(true);
            });
        } catch (Exception e) {
            showError("管理员界面加载失败", e.getMessage());
        }
    }

    private void openUserInterface(Users user) {
        try {
            SwingUtilities.invokeLater(() -> {
                new com.healthsys.ui.user.MainView(user);
            });
        } catch (Exception e) {
            showError("用户界面加载失败", e.getMessage());
        }
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
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showWarning("请输入用户名" + ("管理员".equals(loginTypeCombo.getSelectedItem()) ? "（管理员账号）" : "（手机号）"));
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showWarning("请输入密码");
            passwordField.requestFocus();
            return;
        }

        String type = (String) loginTypeCombo.getSelectedItem();
        switch (type) {
            case "管理员" -> authService.handleAdminLogin(username, password);
            case "医生"   -> authService.handleDoctorLogin(username, password);
            default       -> authService.handleLogin(username, password);
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