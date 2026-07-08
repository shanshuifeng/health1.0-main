package com.healthsys.ui.medical;

import com.healthsys.common.entity.Users;

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
        setBackground(new Color(245, 245, 245));

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
        sidebar.setBackground(new Color(70, 104, 197));
        sidebar.setPreferredSize(new Dimension(220, Integer.MAX_VALUE));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(90, 124, 217));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(120, 150, 240));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(90, 124, 217));
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
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showHome() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(new Color(245, 245, 245));

        JLabel welcomeLabel = new JLabel("欢迎使用医疗健康管理系统", JLabel.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 104, 197));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(welcomeLabel);

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
        if (reportPanel == null) {
            reportPanel = new ReportPanel(doctorId);
            contentPanel.add(reportPanel, "reports");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "reports");
    }
}

class AboutView {
    private JPanel aboutPanel;

    public AboutView() {
        initializeUI();
    }

    public JPanel getAboutPanel() {
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
}
