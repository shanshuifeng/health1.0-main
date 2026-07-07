package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.ui.medical.CrudPanel;
import javax.swing.*;
import java.awt.*;

public class CheckGroupDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private CheckItemGroup checkItemGroup;
    private int option = CANCEL_OPTION;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);
    private final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private final Font FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    // 对话框组件
    private JTextField idField;
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField priceField;

    public CheckGroupDialog(CheckItemGroup checkItemGroup) {
        this.checkItemGroup = checkItemGroup != null ? checkItemGroup : new CheckItemGroup();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(500, 300);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(checkItemGroup.getId() == null ? "新增检查组" : "编辑检查组");

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // ID字段（仅显示，不可编辑）
        idField = createStyledTextField(checkItemGroup.getId() != null ? checkItemGroup.getId().toString() : "自动生成");
        idField.setEditable(false);
        addFormField(formPanel, "检查组ID:", idField);

        // 名称字段
        addFormField(formPanel, "检查组名称:",
                nameField = createStyledTextField(checkItemGroup.getName()));

        // 描述字段
        addFormField(formPanel, "检查组描述:",
                descriptionField = createStyledTextField(checkItemGroup.getDescription()));

        // 价格字段
        addFormField(formPanel, "检查组价格:",
                priceField = createStyledTextField(
                        checkItemGroup.getPrice() != null ? checkItemGroup.getPrice().toString() : ""));

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

    private void addFormField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        panel.add(label);
        panel.add(field);
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(FIELD_FONT);
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

    public CheckItemGroup getCheckItemGroup() {
        if (checkItemGroup.getId() != null) {
            checkItemGroup.setId(Long.parseLong(idField.getText()));
        }
        checkItemGroup.setName(nameField.getText());
        checkItemGroup.setDescription(descriptionField.getText());
        checkItemGroup.setPrice(Double.parseDouble(priceField.getText()));
        return checkItemGroup;
    }
}

