package com.healthsys.ui.admin;

import com.healthsys.ui.medical.UserPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminMainView extends JPanel {
    private JPanel contentPanel;
    private UserPanel userPanel;
    private DoctorPanel doctorPanel;
    private AboutView aboutView;

    public AdminMainView() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        createSidebar();
        createContentArea();
        showHome();
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(70, 104, 197));
        sidebar.setPreferredSize(new Dimension(220, Integer.MAX_VALUE));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] navItems = {"首页", "用户管理", "医生管理", "关于"};
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

    private JFrame getParentFrame() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof JFrame)) {
            parent = parent.getParent();
        }
        return (JFrame) parent;
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
                case "首页" -> showHome();
                case "用户管理" -> showUserManagement();
                case "医生管理" -> showDoctorManagement();
                case "关于" -> showAbout();
            }
        };
    }

    private void createContentArea() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showHome() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(new Color(245, 245, 245));

        JLabel welcomeLabel = new JLabel("欢迎使用管理员控制台", JLabel.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 104, 197));

        JLabel tipLabel = new JLabel("您可以在此管理用户和医生账号。", JLabel.CENTER);
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        tipLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(welcomeLabel);
        centerPanel.add(tipLabel);

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

    private void showUserManagement() {
        if (userPanel == null) {
            userPanel = new UserPanel();
            contentPanel.add(userPanel, "users");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "users");
        userPanel.refreshData();
    }

    private void showDoctorManagement() {
        if (doctorPanel == null) {
            doctorPanel = new DoctorPanel();
            contentPanel.add(doctorPanel, "doctors");
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "doctors");
        doctorPanel.refreshData();
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

        JLabel titleLabel = new JLabel("管理员控制台", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 104, 197));

        JLabel versionLabel = new JLabel("版本: 1.0.0", JLabel.CENTER);
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        JLabel descLabel = new JLabel("该页面用于管理员统一管理用户与医生账号。", JLabel.CENTER);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        descLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(titleLabel);
        centerPanel.add(versionLabel);
        centerPanel.add(descLabel);

        aboutPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
