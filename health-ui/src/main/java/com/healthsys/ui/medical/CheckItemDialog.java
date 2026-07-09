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
    private JTextField categoryField;
    private JTextField unitField;
    private JTextField referenceRangeField;
    private JTextField priceField;
    private JComboBox<String> statusCombo;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);
    private final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private final Font FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    public CheckItemDialog(CheckItem checkItem) {
        this.checkItem = checkItem != null ? checkItem : new CheckItem();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(checkItem.getId() == null ? "新增检查项" : "编辑检查项 — " + checkItem.getName());

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(Color.WHITE);

        // 表单面板 — GridBagLayout 灵活控制列宽
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // 标签列固定，字段列自动拉伸
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.gridy = 0;

        // 名称
        addLabel(formPanel, gbc, "名称:");
        nameField = createField(checkItem.getName());
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(nameField, gbc);

        // 代码
        gbc.gridx = 0; gbc.weightx = 0; gbc.gridy = 1;
        addLabel(formPanel, gbc, "代码:");
        codeField = createField(checkItem.getCode());
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(codeField, gbc);

        // 分类
        gbc.gridx = 0; gbc.weightx = 0; gbc.gridy = 2;
        addLabel(formPanel, gbc, "分类:");
        categoryField = createField(checkItem.getCategory());
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(categoryField, gbc);

        // 单位
        gbc.gridx = 0; gbc.weightx = 0; gbc.gridy = 3;
        addLabel(formPanel, gbc, "单位:");
        unitField = createField(checkItem.getUnit());
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(unitField, gbc);

        // 参考范围
        gbc.gridx = 0; gbc.weightx = 0; gbc.gridy = 4;
        addLabel(formPanel, gbc, "参考范围:");
        referenceRangeField = createField(checkItem.getReferenceRange());
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(referenceRangeField, gbc);

        // 价格
        gbc.gridx = 0; gbc.weightx = 0; gbc.gridy = 5;
        addLabel(formPanel, gbc, "价格(¥):");
        priceField = createField(checkItem.getPrice() != null ? checkItem.getPrice().toString() : "");
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(priceField, gbc);

        // 状态
        gbc.gridx = 0; gbc.weightx = 0; gbc.gridy = 6;
        addLabel(formPanel, gbc, "状态:");
        statusCombo = new JComboBox<>(new String[]{"启用", "停用"});
        statusCombo.setFont(FIELD_FONT);
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        statusCombo.setSelectedIndex(checkItem.getStatus() != null && checkItem.getStatus() == 0 ? 1 : 0);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(statusCombo, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
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
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(label, gbc);
    }

    private JTextField createField(String text) {
        JTextField field = new JTextField(text != null ? text : "");
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入检查项名称", "提示", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的价格（非负数字）", "错误", JOptionPane.ERROR_MESSAGE);
            priceField.requestFocus();
            return false;
        }
        return true;
    }

    public int showDialog() {
        setVisible(true);
        return option;
    }

    public CheckItem getCheckItem() {
        checkItem.setName(nameField.getText().trim());
        checkItem.setCode(codeField.getText().trim());
        checkItem.setCategory(categoryField.getText().trim());
        checkItem.setUnit(unitField.getText().trim());
        checkItem.setReferenceRange(referenceRangeField.getText().trim());
        checkItem.setPrice(Double.parseDouble(priceField.getText().trim()));
        checkItem.setStatus(statusCombo.getSelectedIndex() == 0 ? 1 : 0);
        return checkItem;
    }
}
