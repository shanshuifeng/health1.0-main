package com.healthsys.ui.medical;

import com.healthsys.ui.medical.CrudPanel;

import javax.swing.*;
import java.awt.*;

public class MessageDialog extends JDialog {
    private final JTextArea messageArea;
    private final Color MAIN_COLOR = new Color(70, 104, 197);

    public MessageDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(MAIN_COLOR);
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 消息内容
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        messageArea.setBackground(new Color(248, 248, 248));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 10));

        JButton closeButton = CrudPanel.createStyledButton("关闭", MAIN_COLOR);
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void setMessage(String message) {
        messageArea.setText(message);
        messageArea.setCaretPosition(0); // 滚动到顶部
    }

    public static void showMessage(Component parent, String title, String message) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        MessageDialog dialog = new MessageDialog(owner, title, true);
        dialog.setMessage(message);
        dialog.setVisible(true);
    }
}

