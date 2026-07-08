package com.healthsys.ui.user;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Users;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MainView {
    private final JFrame mainFrame;
    private final JPanel mainPanel;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JPanel statusPanel;
    private final JLabel statusLabel;
    private final Users currentUser;
    private final AboutView aboutView;

    // 模块视图实例
    private final JPanel homePanel;
    private final AppointmentView appointmentView;

    public MainView(Users currentUser) {
        this.currentUser = currentUser;

        // 初始化所有模块
        this.homePanel = createHomePanel();
        this.appointmentView = new AppointmentView(currentUser);
        // this.examRecordView = new ExamRecordView(currentUser);
        this.aboutView = new AboutView();

        // 角色检查已移除 — 新架构下用户统一进入普通用户界面

        // 初始化UI组件
        this.mainFrame = new JFrame("健康管理系统");
        this.mainPanel = new JPanel(new BorderLayout());
        this.leftPanel = new JPanel();
        this.rightPanel = new JPanel(new CardLayout());
        this.statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.statusLabel = new JLabel();

        initializeUI();
    }

    private void initializeUI() {
        // 主窗口设置
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLocationRelativeTo(null);

        // 左侧导航栏
        initLeftPanel();

        // 右侧内容区（使用CardLayout）
        initRightPanel();

        // 状态栏
        initStatusBar();

        // 组合主界面
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private void initLeftPanel() {
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(70, 104, 197));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 导航按钮
        addNavButton("首页", "home.png", () -> showPanel("home"));
        addNavButton("预约", "message.png", () -> showPanel("messages")); // 新增消息按钮
        addNavButton("自定义检查组", "package_customize.png", () -> showPanel("customPackage"));
        addNavButton("消息", "calendar.png", () -> showPanel("appointment"));
        addNavButton("体检信息", "health.png", () -> showPanel("checkupInfo")); // 新增体检信息按钮
        addNavButton("关于", "info.png", () -> showPanel("about"));


        // 弹性空间
        leftPanel.add(Box.createVerticalGlue());

        // 系统功能按钮
        addNavButton("退出登录", "logout.png", this::logout);
        addNavButton("退出系统", "power.png", () -> System.exit(0));
    }

    private void addNavButton(String text, String iconName, Runnable action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 50));
        button.setPreferredSize(new Dimension(180, 50));
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(90, 124, 217));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);


        // 添加事件监听（正确处理ActionEvent参数）
        button.addActionListener((ActionEvent e) -> {
            action.run();
            updateStatusBar();
        });

        leftPanel.add(button);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void initRightPanel() {
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加所有模块面板
        homePanel.setName("home");
        rightPanel.add(homePanel, "home");

        JPanel appointmentPanel = appointmentView.getAppointmentPanel();
        appointmentPanel.setName("appointment");
        rightPanel.add(appointmentPanel, "appointment");

        // 创建自定义检查组面板
        CustomPackageView customPackageView = new CustomPackageView(currentUser);
        JPanel customPackagePanel = customPackageView.getCustomPackagePanel();
        customPackagePanel.setName("customPackage");
        rightPanel.add(customPackagePanel, "customPackage");


        // 添加about面板
        JPanel aboutPanel = aboutView.getAboutPanel();
        aboutPanel.setName("about");
        rightPanel.add(aboutPanel, "about");

        // 添加消息面板
        MessagesView messagesView = new MessagesView(currentUser);
        JPanel messagesPanel = messagesView.getMessagesPanel();
        messagesPanel.setName("messages");
        rightPanel.add(messagesPanel, "messages");


        JPanel checkupInfoPanel = new JPanel(new BorderLayout());
        checkupInfoPanel.setName("checkupInfo");

        if (currentUser != null && currentUser.getId() > 0) {

            Appointment dummyAppointment = new Appointment(currentUser.getId(), null, LocalDateTime.now());
            ExamRecordView examRecordView = new ExamRecordView(dummyAppointment);
            checkupInfoPanel.add(examRecordView.getHealthPanel(), BorderLayout.CENTER);
        } else {
            checkupInfoPanel.add(new JLabel("暂无体检记录", JLabel.CENTER));
        }
        rightPanel.add(checkupInfoPanel, "checkupInfo");


    }

    private void initStatusBar() {
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusPanel.setBackground(Color.WHITE);

        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusPanel.add(statusLabel);

        updateStatusBar();
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        JLabel welcomeLabel = new JLabel("欢迎使用健康管理系统", JLabel.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 32));
        welcomeLabel.setForeground(new Color(70, 104, 197));

        JLabel userLabel = new JLabel(currentUser.getName() + " 您好！", JLabel.CENTER);
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        userLabel.setForeground(new Color(100, 100, 100));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 30, 0);
        centerPanel.add(welcomeLabel, gbc);
        centerPanel.add(userLabel, gbc);
        centerPanel.setBackground(new Color(245, 245, 245));

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) rightPanel.getLayout();
        cl.show(rightPanel, panelName);
        updateWindowTitle(panelName);
        updateStatusBar();
    }

    private void updateWindowTitle(String panelName) {
        String title = switch (panelName) {
            case "home" -> "首页";
            case "messages" -> "消息";
            case "appointment" -> "自定义检查组";
            case "checkupInfo" -> "体检信息";
            case "userManagement" -> "体检信息";
            case "about" -> "关于";
            default -> "";
        };
        mainFrame.setTitle("健康管理系统 - " + title);
    }

    private void updateStatusBar() {
        String currentView = getCurrentViewName();
        String statusText = String.format(
                "当前用户: %s (%s) | %s | 登录时间: %s",
                currentUser.getName(),
                "用户",
                currentView,
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        statusLabel.setText(statusText);
    }

    private String getCurrentViewName() {
        for (Component comp : rightPanel.getComponents()) {
            if (comp.isVisible()) {
                String name = comp.getName();
                if (name != null) {
                    return switch (name) {
                        case "home" -> "首页";
                        case "messages" -> "预约";
                        case "appointment" -> "消息";
                        case "checkupInfo" -> "体检信息";
                        case "userManagement" -> "用户管理";
                        case "about" -> "关于";
                        default -> "";
                    };
                }
            }
        }
        return "";
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "确定要退出登录吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            mainFrame.dispose();
            new com.healthsys.ui.auth.LoginView().setVisible(true);
        }
    }

}

class AboutView {
    private JPanel aboutPanel;

    public AboutView() {
        initializeUI();
    }

    private void initializeUI() {
        aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        aboutPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("健康管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 104, 197));

        JLabel versionLabel = new JLabel("版本: 1.0.0", JLabel.CENTER);
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        JLabel dateLabel = new JLabel("开发日期: 2026年", JLabel.CENTER);
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        JLabel descLabel = new JLabel("本系统为小组开发作业，提供用户界面专属预约体检、查看消息等功能。", JLabel.CENTER);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        descLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(titleLabel);
        centerPanel.add(versionLabel);
        centerPanel.add(dateLabel);
        centerPanel.add(descLabel);

        aboutPanel.add(centerPanel, BorderLayout.CENTER);
    }

    public JPanel getAboutPanel() {
        return aboutPanel;
    }
}
