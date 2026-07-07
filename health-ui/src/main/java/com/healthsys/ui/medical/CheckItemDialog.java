package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.ui.medical.CrudPanel;
import javax.swing.*;
import java.awt.*;

public class CheckItemDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private CheckItem checkItem;
    private int option = CANCEL_OPTION;

    // 对话框组件
    private JTextField nameField;
    private JTextField codeField;
    private JTextField descriptionField;
    private JTextField normalRangeField;
    private JTextField priceField;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);

    public CheckItemDialog(CheckItem checkItem) {
        this.checkItem = checkItem != null ? checkItem : new CheckItem();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(checkItem.getId() == null ? "新增检查项" : "编辑检查项");

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // 统一字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 添加表单字段
        addFormField(formPanel, "名称:", nameField = createStyledTextField(checkItem.getName(), fieldFont), labelFont);
        addFormField(formPanel, "代码:", codeField = createStyledTextField(checkItem.getCode(), fieldFont), labelFont);
        addFormField(formPanel, "描述:", descriptionField = createStyledTextField(checkItem.getDescription(), fieldFont), labelFont);
        addFormField(formPanel, "正常范围:", normalRangeField = createStyledTextField(checkItem.getNormalRange(), fieldFont), labelFont);
        addFormField(formPanel, "价格:", priceField = createStyledTextField(
                checkItem.getPrice() != null ? checkItem.getPrice().toString() : "", fieldFont), labelFont);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = CrudPanel.createStyledButton("确定", MAIN_COLOR);
        okButton.addActionListener(e -> {
            if (validateInput()) {
                option = OK_OPTION;
                dispose();
            }
        });

        JButton cancelButton = CrudPanel.createStyledButton("取消", new Color(204, 153, 153));
        cancelButton.addActionListener(e -> {
            option = CANCEL_OPTION;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addFormField(JPanel panel, String labelText, JTextField textField, Font font) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        panel.add(label);
        panel.add(textField);
    }

    private JTextField createStyledTextField(String text, Font font) {
        JTextField field = new JTextField(text);
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private boolean validateInput() {
        try {
            Double.parseDouble(priceField.getText());
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的价格", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public int showDialog() {
        setVisible(true);
        return option;
    }

    public CheckItem getCheckItem() {
        checkItem.setName(nameField.getText());
        checkItem.setCode(codeField.getText());
        checkItem.setDescription(descriptionField.getText());
        checkItem.setNormalRange(normalRangeField.getText());
        checkItem.setPrice(Double.parseDouble(priceField.getText()));
        return checkItem;
    }
}
