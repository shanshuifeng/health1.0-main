package com.healthsys.ui.medical;

import com.healthsys.common.entity.Users;
import com.healthsys.ui.HealthTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainView extends JPanel {
    private final Long doctorId;
    private final String doctorName;
    private JPanel contentPanel;
    private AppointmentPanel appointmentPanel;
    private ReportPanel reportPanel;
    private AboutView aboutView;

    private Users currentUser;
    private String role;

    public MainView() {
        this(null, null);
    }

    public MainView(Users user) {
        this(user != null ? user.getUserId() : null,
             user != null ? user.getRealName() : null);
        this.currentUser = user;
        this.role = user != null ? user.getRole() : null;
    }

    public MainView(Long doctorId, String doctorName) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.role = null;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        createSidebar();
        createContentArea();
        showHome();
    }

    private JFrame getParentFrame() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof JFrame)) {
            parent = parent.getParent();
        }
        return (JFrame) parent;
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(70, 104, 197)); // #4668C5
        sidebar.setPreferredSize(new Dimension(220, Integer.MAX_VALUE));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        String[] navItems = {"首页", "预约管理", "报告管理", "关于"};
        for (String item : navItems) {
            JButton button = createNavButton(item);
            button.addActionListener(getNavActionListener(item));
            sidebar.add(button);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createNavButton("退出登录");
        logoutBtn.addActionListener(e -> {
            JFrame frame = getParentFrame();
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "确定要退出登录吗？",
                    "确认退出",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                frame.dispose();
                new com.healthsys.ui.auth.LoginView().setVisible(true);
            }
        });
        sidebar.add(logoutBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

        JButton exitBtn = createNavButton("退出系统");
        exitBtn.addActionListener(e -> System.exit(0));
        sidebar.add(exitBtn);

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 50));
        button.setPreferredSize(new Dimension(180, 50));
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(new Color(90, 124, 217)); // #5A7CD9
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(120, 150, 240)); // 悬停色
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(90, 124, 217)); // 恢复原色
            }
        });

        return button;
    }

    private ActionListener getNavActionListener(String itemName) {
        return e -> {
            switch (itemName) {
                case "首页":
                    showHome();
                    break;
                case "预约管理":
                    showAppointmentManagement();
                    break;
                case "报告管理":
                    showReportManagement();
                    break;
                case "关于":
                    showAbout();
                    break;
            }
        };
    }

    private void createContentArea() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showHome() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("欢迎使用健康管理系统", JLabel.CENTER);
        welcomeLabel.setFont(HealthTheme.FONT_PAGE_TITLE);
        welcomeLabel.setForeground(HealthTheme.PRIMARY);

        JLabel userLabel = new JLabel((doctorName != null ? doctorName : "") + " 医生，您好！", JLabel.CENTER);
        userLabel.setFont(HealthTheme.FONT_SUBTITLE);
        userLabel.setForeground(HealthTheme.TEXT_SECONDARY);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 30, 0);
        centerPanel.add(welcomeLabel, gbc);
        centerPanel.add(userLabel, gbc);
        centerPanel.setBackground(Color.WHITE);

        homePanel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(homePanel, "home");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "home");
    }

    private void showAbout() {
        if (aboutView == null) {
            aboutView = new AboutView();
            contentPanel.add(aboutView.getAboutPanel(), "about");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "about");
    }

    private void showAppointmentManagement() {
        if (appointmentPanel == null) {
            appointmentPanel = new AppointmentPanel(doctorId);
            contentPanel.add(appointmentPanel, "appointments");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "appointments");
    }

    private void showReportManagement() {
        try {
            if (reportPanel == null) {
                reportPanel = new ReportPanel(doctorId);
                contentPanel.add(reportPanel, "reports");
            } else {
                reportPanel.refreshData();
            }
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "reports");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "报告管理加载失败：" + ex.toString(),
                    "错误", JOptionPane.ERROR_MESSAGE);
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

    public JPanel getAboutPanel() {
        return aboutPanel;
    }

    private void initializeUI() {
        aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBackground(BG_COLOR);
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        aboutPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        content.add(createHeaderSection());
        content.add(Box.createRigidArea(new Dimension(0, 25)));
        content.add(createIntroCard());
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(createFeatureSection());
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(createTechCard());
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(createTeamSection());
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(createInfoAndContactSection());
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(createFooter());
        content.add(Box.createVerticalGlue());

        return content;
    }

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

    private JPanel createInfoAndContactSection() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 20, 0));
        wrapper.setBackground(BG_COLOR);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

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

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel("  ▎" + text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        label.setForeground(PRIMARY_COLOR);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        return label;
    }

    private JTable createStyledTable(String[] columns, Object[][] data, int rowCount) {
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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

        if (columns.length == 2) {
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(400);
        }

        return table;
    }
}
