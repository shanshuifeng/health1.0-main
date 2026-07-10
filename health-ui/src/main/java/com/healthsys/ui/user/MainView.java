package com.healthsys.ui.user;

import com.healthsys.common.entity.Users;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
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
    private ExamRecordView examRecordView;

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
        addNavButton("预约检查", "message.png", () -> showPanel("groups"));
        addNavButton("我的预约", "calendar.png", () -> showPanel("appointments"));
        addNavButton("体检信息", "health.png", () -> showPanel("checkupInfo"));
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
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
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
        appointmentPanel.setName("appointments");
        rightPanel.add(appointmentPanel, "appointments");

        // 添加about面板
        JPanel aboutPanel = aboutView.getAboutPanel();
        aboutPanel.setName("about");
        rightPanel.add(aboutPanel, "about");

        // 检查组列表（预约入口）
        MessagesView messagesView = new MessagesView(currentUser);
        JPanel messagesPanel = messagesView.getMessagesPanel();
        messagesPanel.setName("groups");
        rightPanel.add(messagesPanel, "groups");


        JPanel checkupInfoPanel = new JPanel(new BorderLayout());
        checkupInfoPanel.setName("checkupInfo");

        examRecordView = new ExamRecordView(currentUser);
        checkupInfoPanel.add(examRecordView.getHealthPanel(), BorderLayout.CENTER);

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

        // 切换面板时自动刷新数据
        if ("appointments".equals(panelName)) {
            appointmentView.refreshAppointmentData();
        }
        if ("checkupInfo".equals(panelName) && examRecordView != null) {
            examRecordView.refreshData();
        }
    }

    private void updateWindowTitle(String panelName) {
        String title = switch (panelName) {
            case "home" -> "首页";
            case "groups" -> "预约检查";
            case "appointments" -> "我的预约";
            case "checkupInfo" -> "体检信息";
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
                        case "groups" -> "预约检查";
                        case "appointments" -> "我的预约";
                        case "checkupInfo" -> "体检信息";
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

    // 主题色常量
    private static final Color PRIMARY_COLOR = new Color(70, 104, 197);
    private static final Color ACCENT_COLOR = new Color(90, 124, 217);
    private static final Color BG_COLOR = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(50, 50, 60);
    private static final Color TEXT_SECONDARY = new Color(120, 120, 130);
    private static final Color TABLE_HEADER_BG = new Color(70, 104, 197);
    private static final Color TABLE_ALT_ROW = new Color(240, 244, 255);
    private static final Color BORDER_COLOR = new Color(220, 225, 240);

    public AboutView() {
        initializeUI();
    }

    private void initializeUI() {
        aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBackground(BG_COLOR);
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 使用 JScrollPane 包裹以支持内容超出时滚动
        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // 让鼠标滚轮可以流畅滚动
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        aboutPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        // ===== 标题区域 =====
        content.add(createHeaderSection());
        content.add(Box.createRigidArea(new Dimension(0, 25)));

        // ===== 项目简介卡片 =====
        content.add(createIntroCard());
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== 核心功能表格 =====
        content.add(createFeatureSection());
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== 技术架构卡片 =====
        content.add(createTechCard());
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== 开发团队表格 =====
        content.add(createTeamSection());
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== 项目信息 + 联系方式 =====
        content.add(createInfoAndContactSection());
        content.add(Box.createRigidArea(new Dimension(0, 15)));

        // ===== 版权信息 =====
        content.add(createFooter());
        content.add(Box.createVerticalGlue());

        return content;
    }

    // ==================== 标题区域 ====================
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel titleLabel = new JLabel("Health 1.0 健康体检管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        JLabel subtitleLabel = new JLabel("轻量化桌面管理系统 · 覆盖预约、检查、报告全生命周期", JLabel.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        panel.add(subtitleLabel, gbc);

        return panel;
    }

    // ==================== 项目简介卡片 ====================
    private JPanel createIntroCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = createSectionTitle("项目简介");
        card.add(title, BorderLayout.NORTH);

        JTextArea desc = new JTextArea(
                "Health 1.0 是为中小型体检机构打造的轻量化桌面管理系统，\n" +
                "覆盖预约、检查、报告全生命周期。系统采用 Java Swing 桌面应用 + MySQL 数据库，\n" +
                "五人团队三天完成迭代开发。"
        );
        desc.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        desc.setForeground(TEXT_PRIMARY);
        desc.setBackground(CARD_BG);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        card.add(desc, BorderLayout.CENTER);

        return card;
    }

    // ==================== 核心功能表格 ====================
    private JPanel createFeatureSection() {
        JPanel section = createCard();
        section.setLayout(new BorderLayout(0, 5));

        JLabel title = createSectionTitle("核心功能");
        section.add(title, BorderLayout.NORTH);

        String[] columns = {"角色", "功能"};
        Object[][] data = {
                {"普通用户", "注册登录、浏览套餐、预约体检、查看/下载报告"},
                {"医生", "查看今日预约、录入检查结果、标记异常、导出Word报告"},
                {"管理员", "用户管理、医生管理、检查组管理、检查项管理"}
        };

        JTable table = createStyledTable(columns, data, data.length);
        section.add(table.getTableHeader(), BorderLayout.PAGE_START);
        section.add(table, BorderLayout.CENTER);

        return section;
    }

    // ==================== 技术架构卡片 ====================
    private JPanel createTechCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = createSectionTitle("技术架构");
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 12));
        grid.setBackground(CARD_BG);
        grid.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        grid.add(createTechItem("运行环境", "Java 21 + MySQL 8.0"));
        grid.add(createTechItem("UI 框架", "Java Swing（24个面板）"));
        grid.add(createTechItem("构建工具", "Maven 3.x（多模块）"));
        grid.add(createTechItem("安全方案", "AES-128 加密 + 三表认证隔离"));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTechItem(String label, String value) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setBackground(CARD_BG);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("微软雅黑", Font.BOLD, 12));
        lbl.setForeground(TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        val.setForeground(TEXT_PRIMARY);

        item.add(lbl, BorderLayout.NORTH);
        item.add(val, BorderLayout.CENTER);
        return item;
    }

    // ==================== 开发团队表格 ====================
    private JPanel createTeamSection() {
        JPanel section = createCard();
        section.setLayout(new BorderLayout(0, 5));

        JLabel title = createSectionTitle("开发团队");
        section.add(title, BorderLayout.NORTH);

        String[] columns = {"成员", "职责"};
        Object[][] data = {
                {"蒋永康", "全栈架构 (19次提交 +8,800行)"},
                {"陈欣妍", "UI视觉统一 (4次提交 +1,703行)"},
                {"丁紫岚", "医生端+Bug修复 (25次提交 +1,211行)"},
                {"慈曾华", "认证安全+支付 (7次提交 +1,573行)"},
                {"苏贝贝", "注册+登录体验 (5次提交 +321行)"}
        };

        JTable table = createStyledTable(columns, data, data.length);
        section.add(table.getTableHeader(), BorderLayout.PAGE_START);
        section.add(table, BorderLayout.CENTER);

        return section;
    }

    // ==================== 项目信息 + 联系方式 ====================
    private JPanel createInfoAndContactSection() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 20, 0));
        wrapper.setBackground(BG_COLOR);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // 左：项目信息
        JPanel infoCard = createCard();
        infoCard.setLayout(new BorderLayout(0, 8));
        infoCard.add(createSectionTitle("项目信息"), BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(4, 1, 0, 6));
        infoGrid.setBackground(CARD_BG);
        infoGrid.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        infoGrid.add(createInfoRow("版本", "V2.0（最终版）"));
        infoGrid.add(createInfoRow("发布日期", "2026-07-07"));
        infoGrid.add(createInfoRow("开发模式", "五人并行，三天迭代"));
        infoGrid.add(createInfoRow("仓库地址", "github.com/shanshuifeng/health1.0-main"));
        infoCard.add(infoGrid, BorderLayout.CENTER);

        // 右：联系我们
        JPanel contactCard = createCard();
        contactCard.setLayout(new BorderLayout(0, 8));
        contactCard.add(createSectionTitle("联系我们"), BorderLayout.NORTH);

        JPanel contactGrid = new JPanel(new GridLayout(2, 1, 0, 8));
        contactGrid.setBackground(CARD_BG);
        contactGrid.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        contactGrid.add(createInfoRow("邮箱", "support@healthsys.com"));
        contactGrid.add(createInfoRow("项目地址", "github.com/shanshuifeng/health1.0-main"));
        contactCard.add(contactGrid, BorderLayout.CENTER);

        wrapper.add(infoCard);
        wrapper.add(contactCard);
        return wrapper;
    }

    private JLabel createInfoRow(String key, String value) {
        JLabel label = new JLabel(key + "：" + value);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    // ==================== 版权页脚 ====================
    private JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BG_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel copyright = new JLabel("© 2026 Health 1.0. All Rights Reserved.");
        copyright.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        copyright.setForeground(TEXT_SECONDARY);
        panel.add(copyright);
        return panel;
    }

    // ==================== 通用组件工厂方法 ====================

    /** 创建白色圆角卡片容器 */
    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        return card;
    }

    /** 创建带左侧色条的分区标题 */
    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel("  ▎" + text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        label.setForeground(PRIMARY_COLOR);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        return label;
    }

    /** 创建统一风格的表格（不使用JScrollPane，直接嵌入卡片） */
    private JTable createStyledTable(String[] columns, Object[][] data, int rowCount) {
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 根据行数精确计算高度，消除多余空白
        int headerHeight = 34;
        int rowHeight = 32;
        int totalHeight = headerHeight + rowCount * rowHeight;
        table.setPreferredScrollableViewportSize(new Dimension(0, totalHeight));
        table.setRowHeight(rowHeight);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(220, 230, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // ===== 表头样式：深色背景 + 白色粗体字 =====
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, headerHeight));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBackground(TABLE_HEADER_BG);
                setForeground(Color.WHITE);
                setFont(new Font("微软雅黑", Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });

        // ===== 数据行：交替底色 + 居中 =====
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT_ROW);
                    setForeground(TEXT_PRIMARY);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });

        // 列宽分配
        if (columns.length == 2) {
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(400);
        }

        return table;
    }

    public JPanel getAboutPanel() {
        return aboutPanel;
    }
}
