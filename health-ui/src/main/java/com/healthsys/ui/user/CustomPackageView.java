package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.Users;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomPackageView {
    private JPanel customPackagePanel;
    private Users currentUser;
    private AppointmentService controller;
    private DefaultListModel<CheckItem> availableModel;
    private DefaultListModel<CheckItem> selectedModel;
    private JList<CheckItem> availableList;
    private JList<CheckItem> selectedList;
    private JLabel totalPriceLabel;
    private JTextField nameField;
    private JTextArea descArea;
    private JTextField priceField;

    public CustomPackageView(Users currentUser) {
        this.currentUser = currentUser;
        this.controller = new AppointmentService();
        initializeUI();
    }

    private void initializeUI() {
        customPackagePanel = new JPanel(new BorderLayout());
        customPackagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("自定义检查组");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 104, 197));
        titlePanel.add(titleLabel);

        // 说明面板
        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel descriptionLabel = new JLabel("<html><div style='text-align: center; width: 500px;'>" +
                "创建符合您个人需求的自定义检查组<br>" +
                "从可用的检查项目中选择，组合成专属于您的检查组</div></html>");
        descriptionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        descriptionPanel.add(descriptionLabel);

        // 主内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 左侧所有检查项目列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("可选检查项目"));

        // 右侧已选检查项目列表
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("已选检查项目"));

        // 初始化列表模型
        availableModel = new DefaultListModel<>();
        selectedModel = new DefaultListModel<>();

        availableList = new JList<>(availableModel);
        selectedList = new JList<>(selectedModel);

        // 设置列表单元格渲染器
        availableList.setCellRenderer(new TestCellRenderer());
        selectedList.setCellRenderer(new TestCellRenderer());

        // 加载所有检查项目
        List<CheckItem> allTests = controller.getAllTests();
        allTests.forEach(availableModel::addElement);

        // 添加按钮
        JButton addButton = new JButton(">>");
        JButton removeButton = new JButton("<<");

        // 价格计算标签
        totalPriceLabel = new JLabel("总价: ¥0.00");
        totalPriceLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        // 添加按钮点击事件
        addButton.addActionListener(this::addSelectedTest);

        // 移除按钮点击事件
        removeButton.addActionListener(this::removeSelectedTest);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createVerticalGlue());

        // 列表面板
        leftPanel.add(new JScrollPane(availableList), BorderLayout.CENTER);
        rightPanel.add(new JScrollPane(selectedList), BorderLayout.CENTER);

        // 价格面板
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pricePanel.add(totalPriceLabel);

        // 输入面板
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("检查组信息"));

        nameField = new JTextField();
        descArea = new JTextArea(3, 20);
        priceField = new JTextField();
        priceField.setEditable(false); // 价格自动计算，不可编辑

        inputPanel.add(new JLabel("检查组名称:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("描述:"));
        inputPanel.add(new JScrollPane(descArea));
        inputPanel.add(new JLabel("价格:"));
        inputPanel.add(priceField);

        // 提交按钮
        JButton submitBtn = new JButton("创建检查组");
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        submitBtn.addActionListener(this::createCustomGroup);

        // 组装列表部分
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listsPanel.add(leftPanel);
        listsPanel.add(rightPanel);

        // 组装中间部分
        JPanel middlePanel = new JPanel(new BorderLayout(10, 0));
        middlePanel.add(listsPanel, BorderLayout.CENTER);
        middlePanel.add(buttonPanel, BorderLayout.EAST);

        // 组装内容面板
        contentPanel.add(pricePanel, BorderLayout.NORTH);
        contentPanel.add(middlePanel, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        // 组装底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(submitBtn);

        // 组装主面板
        customPackagePanel.add(titlePanel, BorderLayout.NORTH);
        customPackagePanel.add(descriptionPanel, BorderLayout.PAGE_START);
        customPackagePanel.add(contentPanel, BorderLayout.CENTER);
        customPackagePanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addSelectedTest(ActionEvent e) {
        CheckItem selected = availableList.getSelectedValue();
        if (selected != null) {
            availableModel.removeElement(selected);
            selectedModel.addElement(selected);
            calculateTotalPrice();
        }
    }

    private void removeSelectedTest(ActionEvent e) {
        CheckItem selected = selectedList.getSelectedValue();
        if (selected != null) {
            selectedModel.removeElement(selected);
            availableModel.addElement(selected);
            calculateTotalPrice();
        }
    }

    private void calculateTotalPrice() {
        double totalPrice = 0.0;
        for (int i = 0; i < selectedModel.getSize(); i++) {
            CheckItem test = selectedModel.getElementAt(i);
            totalPrice += test.getPrice();
        }
        totalPriceLabel.setText(String.format("总价: ¥%.2f", totalPrice));
        priceField.setText(String.format("%.2f", totalPrice));
    }

    private void createCustomGroup(ActionEvent e) {
        // 验证输入
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(customPackagePanel, "请输入检查组名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedModel.getSize() == 0) {
            JOptionPane.showMessageDialog(customPackagePanel, "请至少选择一个检查项目", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 计算总价
        double totalPrice = 0.0;
        for (int i = 0; i < selectedModel.getSize(); i++) {
            CheckItem test = selectedModel.getElementAt(i);
            totalPrice += test.getPrice();
        }

        CheckItemGroup newGroup = new CheckItemGroup();
        newGroup.setName(nameField.getText());
        newGroup.setDescription(descArea.getText());
        newGroup.setPrice(totalPrice);

        List<Long> selectedTestIds = new ArrayList<>();
        for (int i = 0; i < selectedModel.getSize(); i++) {
            CheckItem test = selectedModel.getElementAt(i);
            selectedTestIds.add(test.getId());
        }

        if (controller.createCustomGroup(newGroup, selectedTestIds)) {
            JOptionPane.showMessageDialog(customPackagePanel, "检查组创建成功！");
            // 清空表单
            nameField.setText("");
            descArea.setText("");
            priceField.setText("");
            selectedModel.clear();

            // 重新加载可用测试项目
            availableModel.clear();
            List<CheckItem> allTests = controller.getAllTests();
            allTests.forEach(availableModel::addElement);

            calculateTotalPrice();
        } else {
            JOptionPane.showMessageDialog(customPackagePanel, "检查组创建失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getCustomPackagePanel() {
        return customPackagePanel;
    }

    // 自定义列表单元格渲染器
    private class TestCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CheckItem) {
                CheckItem test = (CheckItem) value;
                setText(String.format("%s - ¥%.2f", test.getName(), test.getPrice()));
            }
            return c;
        }
    }
}
