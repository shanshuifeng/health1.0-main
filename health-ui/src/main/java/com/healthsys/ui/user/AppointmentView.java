package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.service.MockPaymentService;
import com.healthsys.service.PaymentService;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.CheckItem;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentView {
    private JPanel appointmentPanel;
    private Users currentUser;
    private AppointmentService controller;

    public AppointmentView(Users currentUser) {
        this.currentUser = currentUser;
        this.controller = new AppointmentService();
        initializeUI();
    }

    private void initializeUI() {
        appointmentPanel = new JPanel(new BorderLayout());
        appointmentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> refreshAppointmentData());
        toolbarPanel.add(refreshBtn);

        // 预约表格
        String[] columnNames = { "ID", "检查组/项目", "类型", "预约时间", "状态", "支付状态", "操作" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有操作列可编辑
            }
        };

        JTable appointmentTable = new JTable(model);
        appointmentTable.setRowHeight(30);
        appointmentTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        appointmentTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        loadAppointmentData(model);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);

        appointmentPanel.add(toolbarPanel, BorderLayout.NORTH);
        appointmentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshAppointmentData() {
        JTable table = (JTable) ((JScrollPane) appointmentPanel.getComponent(1))
                .getViewport().getView();
        loadAppointmentData((DefaultTableModel) table.getModel());
    }

    private void loadAppointmentData(DefaultTableModel model) {
        model.setRowCount(0);
        List<Appointment> appointments = controller.getUserAppointments(currentUser);

        for (Appointment appointment : appointments) {
            String itemName = "";
            String type = "";

            if (appointment.getGroupId() != null) {
                CheckItemGroup pkg = controller.getAllGroups().stream()
                        .filter(p -> p.getId().equals(appointment.getGroupId()))
                        .findFirst()
                        .orElse(new CheckItemGroup());
                itemName = pkg.getName();
                type = "检查组";
            } else {
                itemName = "未知项目";
                type = "未定义";
            }

            Object[] rowData = {
                    appointment.getId(),
                    itemName,
                    type,
                    appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    appointment.getStatusDisplay(),
                    appointment.getPaymentStatusDisplay(),
                    "取消"
            };
            model.addRow(rowData);
        }
    }

    private void showNewAppointmentDialog(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("新建体检预约");
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(appointmentPanel);
        dialog.setModal(true);

        // 自定义检查组面板
        JPanel customGroupPanel = createCustomGroupPanel(dialog);
        dialog.add(customGroupPanel);
        dialog.setVisible(true);
    }

    private JPanel createCustomGroupPanel(JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("自定义检查组", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 左侧所有检查项目列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("可选检查项目"));

        // 右侧已选检查项目列表
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("已选检查项目"));

        // 示例：双列表拖拽或按钮添加/移除
        DefaultListModel<CheckItem> availableModel = new DefaultListModel<>();
        DefaultListModel<CheckItem> selectedModel = new DefaultListModel<>();

        JList<CheckItem> availableList = new JList<>(availableModel);
        JList<CheckItem> selectedList = new JList<>(selectedModel);

        // 加载所有可用的检查项目
        List<CheckItem> allTests = controller.getAllTests();
        allTests.forEach(availableModel::addElement);

        // 添加按钮
        JButton addButton = new JButton(">>");
        JButton removeButton = new JButton("<<");

        // 价格计算标签
        JLabel totalPriceLabel = new JLabel("总价: ¥0.00");
        totalPriceLabel.setFont(new Font("宋体", Font.BOLD, 16));

        // 计算总价格的方法
        Runnable calculateTotalPrice = () -> {
            double totalPrice = 0.0;
            for (int i = 0; i < selectedModel.getSize(); i++) {
                CheckItem test = selectedModel.getElementAt(i);
                totalPrice += test.getPrice();
            }
            totalPriceLabel.setText(String.format("总价: ¥%.2f", totalPrice));
        };

        // 添加按钮点击事件
        addButton.addActionListener(e -> {
            CheckItem selected = availableList.getSelectedValue();
            if (selected != null) {
                availableModel.removeElement(selected);
                selectedModel.addElement(selected);
                calculateTotalPrice.run();
            }
        });

        // 移除按钮点击事件
        removeButton.addActionListener(e -> {
            CheckItem selected = selectedList.getSelectedValue();
            if (selected != null) {
                selectedModel.removeElement(selected);
                availableModel.addElement(selected);
                calculateTotalPrice.run();
            }
        });

        // 下方输入框：检查组名称、描述、价格等
        JPanel inputPanel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField();
        JTextArea descArea = new JTextArea();
        JTextField priceField = new JTextField();

        // 价格自动计算
        priceField.setEditable(false); // 使价格字段不可编辑，由系统自动计算

        inputPanel.add(new JLabel("检查组名称:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("描述:"));
        inputPanel.add(new JScrollPane(descArea));
        inputPanel.add(new JLabel("价格:"));
        inputPanel.add(priceField);

        // 提交按钮
        JButton submitBtn = new JButton("提交检查组");
        submitBtn.addActionListener(e -> {
            // 验证输入
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(parentDialog, "请输入检查组名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedModel.getSize() == 0) {
                JOptionPane.showMessageDialog(parentDialog, "请至少选择一个检查项目", "错误", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(parentDialog, "检查组创建成功！");

                // 显示预约时间选择对话框
                showTimeSelectionDialog(newGroup.getId(), null, parentDialog);

                parentDialog.dispose();
                // 刷新预约表格
                loadAppointmentData((DefaultTableModel) ((JTable) ((JScrollPane) appointmentPanel.getComponent(1))
                        .getViewport().getView()).getModel());
            } else {
                JOptionPane.showMessageDialog(parentDialog, "检查组创建失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 组装面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        // 添加价格显示面板
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pricePanel.add(totalPriceLabel);

        leftPanel.add(new JScrollPane(availableList), BorderLayout.CENTER);
        rightPanel.add(new JScrollPane(selectedList), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(pricePanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void showTimeSelectionDialog(Long groupId, Long testId, JDialog parentDialog) {
        JDialog timeDialog = new JDialog(parentDialog, "选择预约时间", true);
        timeDialog.setSize(400, 250);
        timeDialog.setLocationRelativeTo(parentDialog);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("预约日期:"), gbc);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        gbc.gridx = 1;
        panel.add(dateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("预约时间:"), gbc);

        String[] timeSlots = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"};
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        gbc.gridx = 1;
        panel.add(timeCombo, gbc);

        JButton submitBtn = new JButton("确认预约");
        submitBtn.setBackground(new Color(41, 75, 166));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        submitBtn.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.ipady = 8;
        panel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(timeDialog, "请选择预约日期", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String timeStr = (String) timeCombo.getSelectedItem();
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            java.time.LocalDate localDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDateTime ldt = localDate.atTime(hour, minute);

            if (ldt.isBefore(java.time.LocalDateTime.now())) {
                JOptionPane.showMessageDialog(timeDialog, "不能预约过去的时间", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date appointmentTime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

            if (controller.createAppointment(currentUser, groupId, appointmentTime)) {
                JOptionPane.showMessageDialog(timeDialog, "预约成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

                int option = JOptionPane.showConfirmDialog(
                        timeDialog,
                        "是否立即支付？",
                        "支付确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    showPaymentDialog(groupId);
                } else {
                    JOptionPane.showMessageDialog(timeDialog, "您可稍后支付，请注意支付截止时间", "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                timeDialog.dispose();
                parentDialog.dispose();
                loadAppointmentData((DefaultTableModel) ((JTable) ((JScrollPane) appointmentPanel.getComponent(1))
                        .getViewport().getView()).getModel());
            } else {
                JOptionPane.showMessageDialog(timeDialog, "预约失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        timeDialog.add(panel);
        timeDialog.setVisible(true);
    }

    // 表格按钮渲染器
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // 预约表格按钮编辑器
    class ButtonEditor extends DefaultCellEditor {
        private String label;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                Long appointmentId = (Long) table.getValueAt(row, 0);
                if (controller.cancelAppointment(appointmentId)) {
                    JOptionPane.showMessageDialog(table, "预约已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointmentData((DefaultTableModel) table.getModel());
                } else {
                    JOptionPane.showMessageDialog(table, "取消失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
                fireEditingStopped();
            });
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    // 检查组表格按钮编辑器
    class GroupButtonEditor extends DefaultCellEditor {
        private String label;
        private JDialog parentDialog;

        public GroupButtonEditor(JCheckBox checkBox, JDialog parentDialog) {
            super(checkBox);
            this.parentDialog = parentDialog;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                Long groupId = (Long) table.getValueAt(row, 0);
                showTimeSelectionDialog(groupId, null, parentDialog);
                fireEditingStopped();
            });
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    // 项目表格按钮编辑器
    class TestButtonEditor extends DefaultCellEditor {
        private String label;
        private JDialog parentDialog;

        public TestButtonEditor(JCheckBox checkBox, JDialog parentDialog) {
            super(checkBox);
            this.parentDialog = parentDialog;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                Long testId = (Long) table.getValueAt(row, 0);
                showTimeSelectionDialog(null, testId, parentDialog);
                fireEditingStopped();
            });
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    public JPanel getAppointmentPanel() {
        return appointmentPanel;
    }

    private void showCustomPackageDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("自定义检查组");
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(appointmentPanel);
        dialog.setModal(true);

        JPanel panel = new JPanel(new BorderLayout());

        // 左侧所有检查项目列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("可选检查项目"));

        // 右侧已选检查项目列表
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("已选检查项目"));

        // 示例：双列表拖拽或按钮添加/移除
        DefaultListModel<CheckItem> availableModel = new DefaultListModel<>();
        DefaultListModel<CheckItem> selectedModel = new DefaultListModel<>();

        JList<CheckItem> availableList = new JList<>(availableModel);
        JList<CheckItem> selectedList = new JList<>(selectedModel);

        // 加载所有可用的检查项目
        List<CheckItem> allTests = controller.getAllTests();
        allTests.forEach(availableModel::addElement);

        // 添加按钮
        JButton addButton = new JButton(">>");
        JButton removeButton = new JButton("<<");

        // 价格计算标签
        JLabel totalPriceLabel = new JLabel("总价: ¥0.00");
        totalPriceLabel.setFont(new Font("宋体", Font.BOLD, 16));

        // 计算总价格的方法
        Runnable calculateTotalPrice = () -> {
            double totalPrice = 0.0;
            for (int i = 0; i < selectedModel.getSize(); i++) {
                CheckItem test = selectedModel.getElementAt(i);
                totalPrice += test.getPrice();
            }
            totalPriceLabel.setText(String.format("总价: ¥%.2f", totalPrice));
        };

        // 添加按钮点击事件
        addButton.addActionListener(e -> {
            CheckItem selected = availableList.getSelectedValue();
            if (selected != null) {
                availableModel.removeElement(selected);
                selectedModel.addElement(selected);
                calculateTotalPrice.run();
            }
        });

        // 移除按钮点击事件
        removeButton.addActionListener(e -> {
            CheckItem selected = selectedList.getSelectedValue();
            if (selected != null) {
                selectedModel.removeElement(selected);
                availableModel.addElement(selected);
                calculateTotalPrice.run();
            }
        });

        // 下方输入框：检查组名称、描述、价格等
        JPanel inputPanel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField();
        JTextArea descArea = new JTextArea();
        JTextField priceField = new JTextField();

        // 价格自动计算
        priceField.setEditable(false); // 使价格字段不可编辑，由系统自动计算

        inputPanel.add(new JLabel("检查组名称:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("描述:"));
        inputPanel.add(new JScrollPane(descArea));
        inputPanel.add(new JLabel("价格:"));
        inputPanel.add(priceField);

        // 提交按钮
        JButton submitBtn = new JButton("提交检查组");
        submitBtn.addActionListener(e -> {
            // 验证输入
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入检查组名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedModel.getSize() == 0) {
                JOptionPane.showMessageDialog(dialog, "请至少选择一个检查项目", "错误", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(dialog, "检查组创建成功！");
                dialog.dispose();
                // 刷新预约表格
                loadAppointmentData((DefaultTableModel) ((JTable) ((JScrollPane) appointmentPanel.getComponent(1))
                        .getViewport().getView()).getModel());
            } else {
                JOptionPane.showMessageDialog(dialog, "检查组创建失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 组装面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        // 添加价格显示面板
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pricePanel.add(totalPriceLabel);

        leftPanel.add(new JScrollPane(availableList), BorderLayout.CENTER);
        rightPanel.add(new JScrollPane(selectedList), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(pricePanel, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.EAST);
        panel.add(submitBtn, BorderLayout.PAGE_END);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showPaymentDialog(Long appointmentId) {
        JDialog paymentDialog = new JDialog();
        paymentDialog.setTitle("支付确认");
        paymentDialog.setSize(300, 150);
        paymentDialog.setLocationRelativeTo(appointmentPanel);
        paymentDialog.setModal(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton confirmBtn = new JButton("确认缴费");
        confirmBtn.setFont(new Font("宋体", Font.BOLD, 16));
        confirmBtn.setPreferredSize(new Dimension(100, 50));

        confirmBtn.addActionListener(e -> {
            // 模拟支付成功
            if (true) {
                JOptionPane.showMessageDialog(paymentDialog, "支付成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                paymentDialog.dispose();
                loadAppointmentData((DefaultTableModel) ((JTable) ((JScrollPane) appointmentPanel.getComponent(1))
                        .getViewport().getView()).getModel());
            }
            // 1. 获取预约价格
            Double price = controller.getAppointmentPrice(appointmentId);
            if (price == null) {
                JOptionPane.showMessageDialog(paymentDialog, "无法获取检查组价格", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. 用户确认支付
            int option = JOptionPane.showConfirmDialog(paymentDialog,
                    "您需要支付: ¥" + price + "，是否继续？",
                    "支付确认",
                    JOptionPane.YES_NO_OPTION);

            if (option != JOptionPane.YES_OPTION) {
                return; // 用户取消支付
            }

            // 3. 使用支付服务进行支付
            PaymentService paymentService = new MockPaymentService();
            boolean paid = paymentService.pay(appointmentId, price);

            // 4. 更新支付状态
            if (paid && controller.updatePaymentStatus(appointmentId, true)) {
                JOptionPane.showMessageDialog(paymentDialog, "支付成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                paymentDialog.dispose();

                // 5. 刷新预约表格数据
                loadAppointmentData((DefaultTableModel) ((JTable) ((JScrollPane) appointmentPanel.getComponent(1))
                        .getViewport().getView()).getModel());
            } else {
                JOptionPane.showMessageDialog(paymentDialog, "支付失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(confirmBtn, BorderLayout.CENTER);
        paymentDialog.add(panel);
        paymentDialog.setVisible(true);
    }

}

class DetailButtonEditor extends DefaultCellEditor {
    private String label;
    private JDialog parentDialog;
    private AppointmentService controller;
    private JPanel appointmentPanel;

    public DetailButtonEditor(JCheckBox checkBox, JDialog parentDialog, AppointmentService controller,
            JPanel appointmentPanel) {
        super(checkBox);
        this.parentDialog = parentDialog;
        this.controller = controller;
        this.appointmentPanel = appointmentPanel;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            Long groupId = (Long) table.getValueAt(row, 0);
            CheckItemGroup selectedGroup = controller.getAllGroups().stream()
                    .filter(p -> p.getId().equals(groupId))
                    .findFirst()
                    .orElse(null);

            if (selectedGroup != null) {
                showGroupDetail(selectedGroup, parentDialog);
            }
            fireEditingStopped();
        });
        return button;
    }

    private void showGroupDetail(CheckItemGroup checkItemGroup, JDialog parentDialog) {
        JDialog detailDialog = new JDialog(parentDialog, "检查组详情 - " + checkItemGroup.getName(), true);
        detailDialog.setSize(800, 600);
        detailDialog.setLocationRelativeTo(parentDialog);

        PackageDetailView detailView = new PackageDetailView(checkItemGroup);
        detailDialog.add(detailView.getDetailPanel());
        detailDialog.setVisible(true);
    }

    public Object getCellEditorValue() {
        return label;
    }
}
