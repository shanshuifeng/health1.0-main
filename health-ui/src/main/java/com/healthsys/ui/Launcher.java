package com.healthsys.ui;

import com.healthsys.ui.auth.LoginView;

import javax.swing.*;

public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());


                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "系统初始化失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
