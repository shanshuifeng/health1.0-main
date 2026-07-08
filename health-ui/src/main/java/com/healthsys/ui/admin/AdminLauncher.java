package com.healthsys.ui.admin;

import javax.swing.*;

public class AdminLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            JFrame frame = new JFrame("管理员控制台");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.add(new AdminMainView());
            frame.setVisible(true);
        });
    }
}
