package com.healthsys.ui.medical;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainView extends JPanel {
    private JPanel contentPanel;
    private CheckItemPanel checkItemPanel;
    private CheckGroupPanel checkGroupPanel;
    private UserPanel userPanel;
    private AppointmentPanel appointmentPanel;
    private AboutView aboutView;

    public MainView() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // 创建侧边栏 - 风格与第一次代码一致
        createSidebar();

        // 创建内容区域
        createContentArea();

        // 显示首页
        showHome();
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(70, 104, 197)); // 使用第一次代码的蓝色
        sidebar.setPreferredSize(new Dimension(220, Integer.MAX_VALUE));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] navItems = {"首页", "检查项", "检查组", "用户管理", "预约管理", "关于", "退出系统"};
        for (String item : navItems) {
            JButton button = new JButton(item);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(180, 50));
            button.setPreferredSize(new Dimension(180, 50));
            button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            button.setBackground(new Color(90, 124, 217)); // 稍亮的蓝色
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 悬停效果
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    button.setBackground(new Color(120, 150, 240)); // 悬停时更亮的蓝色
                }
                public void mouseExited(MouseEvent evt) {
                    button.setBackground(new Color(90, 124, 217));
                }
            });

            button.addActionListener(getNavActionListener(item));
            sidebar.add(button);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5))); // 间距与第一次代码一致
        }

        // 底部弹性空间
        sidebar.add(Box.createVerticalGlue());
        add(sidebar, BorderLayout.WEST);
    }

    private ActionListener getNavActionListener(String itemName) {
        return e -> {
            switch (itemName) {
                case "首页":
                    showHome();
                    break;
                case "检查项":
                    showCheckItems();
                    break;
                case "检查组":
                    showCheckGroups();
                    break;
                case "用户管理":
                    showUserManagement();
                    break;
                case "预约管理":
                    showAppointmentManagement();
                    break;
                case "关于":
                    showAbout();
                    break;
                case "退出系统":
                    System.exit(0);
                    break;
            }
        };
    }

    private void createContentArea() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        contentPanel.setBackground(new Color(245, 245, 245)); // 与第一次代码一致的背景色
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showHome() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(new Color(245, 245, 245));

        JLabel welcomeLabel = new JLabel("欢迎使用医疗健康管理系统", JLabel.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 104, 197)); // 标题颜色与导航栏一致

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(welcomeLabel);

        homePanel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(homePanel, "home");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "home");
    }

    private void showAbout() { // 新增显示关于面板的方法
        if (aboutView == null) {
            aboutView = new AboutView();
            contentPanel.add(aboutView.getAboutPanel(), "about");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "about");
    }

    private void showCheckItems() {
        if (checkItemPanel == null) {
            checkItemPanel = new CheckItemPanel();
            contentPanel.add(checkItemPanel, "checkItems");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "checkItems");
    }

    private void showCheckGroups() {
        if (checkGroupPanel == null) {
            checkGroupPanel = new CheckGroupPanel();
            contentPanel.add(checkGroupPanel, "checkGroups");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "checkGroups");
    }

    private void showUserManagement() {
        if (userPanel == null) {
            userPanel = new UserPanel();
            contentPanel.add(userPanel, "users");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "users");
    }

    private void showAppointmentManagement() {
        if (appointmentPanel == null) {
            appointmentPanel = new AppointmentPanel();
            contentPanel.add(appointmentPanel, "appointments");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "appointments");
    }
}

// AboutView 类
class AboutView {
    private JPanel aboutPanel;

    public AboutView() {
        initializeUI();
    }

    public JPanel getAboutPanel() { // 提供获取 aboutPanel 的方法
        return aboutPanel;
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

        JLabel dateLabel = new JLabel("开发日期: 2025年", JLabel.CENTER);
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
}
