
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
import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentView {
    private JPanel appointmentPanel;
    private Users currentUser;
    private AppointmentService controller;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private String currentFilter = "PENDING"; // 默认显示未处理

    public AppointmentView(Users currentUser) {
        this.currentUser = currentUser;
        this.controller = new AppointmentService();
        initializeUI();
    }

    private void initializeUI() {
        appointmentPanel = new JPanel(new BorderLayout());
        appointmentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 左侧：刷新按钮
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> refreshAppointmentData());
        leftToolbar.add(refreshBtn);
        toolbarPanel.add(leftToolbar, BorderLayout.WEST);

        // 右侧：打印报告按钮
        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton printBtn = new JButton("打印报告");
        printBtn.setFont(new Font("微软雅黑", Font.BOLD, 13));
        printBtn.setBackground(new Color(70, 104, 197));
        printBtn.setForeground(Color.BLACK);
        printBtn.setFocusPainted(false);
        printBtn.addActionListener(this::handlePrintReport);
        rightToolbar.add(printBtn);
        toolbarPanel.add(rightToolbar, BorderLayout.EAST);

        // 分类按钮
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton pendingBtn = new JButton("未处理");
        JButton completedBtn = new JButton("已处理");
        JButton cancelledBtn = new JButton("已取消");
        Font filterFont = new Font("微软雅黑", Font.BOLD, 12);
        for (JButton btn : new JButton[]{pendingBtn, completedBtn, cancelledBtn}) {
            btn.setFont(filterFont);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(80, 28));
        }
        pendingBtn.setBackground(new Color(255, 193, 7));
        pendingBtn.setForeground(Color.BLACK);
        completedBtn.setBackground(new Color(76, 175, 80));
        completedBtn.setForeground(Color.BLACK);
        cancelledBtn.setBackground(new Color(158, 158, 158));
        cancelledBtn.setForeground(Color.BLACK);

        pendingBtn.addActionListener(e -> { currentFilter = "PENDING"; loadAppointmentData(); });
        completedBtn.addActionListener(e -> { currentFilter = "COMPLETED"; loadAppointmentData(); });
        cancelledBtn.addActionListener(e -> { currentFilter = "CANCELLED"; loadAppointmentData(); });

        filterPanel.add(pendingBtn);
        filterPanel.add(completedBtn);
        filterPanel.add(cancelledBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbarPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // 预约表格
        String[] columnNames = { "ID", "检查组/项目", "类型", "预约时间", "详情", "支付状态", "操作" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 6; // "详情"列和"操作"列可编辑
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setRowHeight(30);
        appointmentTable.getColumnModel().getColumn(4).setCellRenderer(new DetailButtonRenderer());
        appointmentTable.getColumnModel().getColumn(4).setCellEditor(new DetailButtonEditor(new JCheckBox(), this));
        appointmentTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        appointmentTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        loadAppointmentData();

        JScrollPane scrollPane = new JScrollPane(appointmentTable);

        appointmentPanel.add(topPanel, BorderLayout.NORTH);
        appointmentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshAppointmentData() {
        loadAppointmentData();
    }

    private void loadAppointmentData() {
        tableModel.setRowCount(0);
        List<Appointment> appointments = controller.getUserAppointmentsByStatus(
                currentUser.getId(), currentFilter);

        for (Appointment appointment : appointments) {
            String itemName = "";
            String type = "";

            if (appointment.getGroupId() != null) {
                CheckItemGroup pkg = controller.getCheckItemGroupById(appointment.getGroupId());
                itemName = pkg != null ? pkg.getName() : "未知套餐";
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
                    "详情",
                    appointment.getPaymentStatusDisplay(),
                    "取消"
            };
            tableModel.addRow(rowData);
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
                refreshAppointmentData();
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
        timeDialog.setSize(420, 330);
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

        // 医生选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("选择医生:"), gbc);

        java.util.List<com.healthsys.common.entity.Doctor> doctors = new com.healthsys.dao.DoctorDAO().getAll();
        JComboBox<String> doctorCombo = new JComboBox<>();
        doctorCombo.addItem("不指定");
        for (com.healthsys.common.entity.Doctor d : doctors) {
            doctorCombo.addItem(d.getName() + " - " + d.getDepartment() + " - " + d.getTitle());
        }
        gbc.gridx = 1;
        panel.add(doctorCombo, gbc);

        JButton submitBtn = new JButton("确认预约");
        submitBtn.setBackground(new Color(41, 75, 166));
        submitBtn.setForeground(Color.BLACK);
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        submitBtn.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
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

            int doctorIndex = doctorCombo.getSelectedIndex();
            Long doctorId = doctorIndex > 0 ? doctors.get(doctorIndex - 1).getDoctorId() : null;

            Appointment newAppointment = controller.createAppointment(currentUser, groupId, appointmentTime, doctorId);
            if (newAppointment != null) {
                JOptionPane.showMessageDialog(timeDialog, "预约成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

                int option = JOptionPane.showConfirmDialog(
                        timeDialog,
                        "是否立即支付？",
                        "支付确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    showPaymentDialog(newAppointment.getId());
                } else {
                    JOptionPane.showMessageDialog(timeDialog, "您可稍后支付，请注意支付截止时间", "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                timeDialog.dispose();
                parentDialog.dispose();
                refreshAppointmentData();
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
                    refreshAppointmentData();
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

    // ===== 详情按钮相关 =====

    /**
     * 详情按钮渲染器：蓝字、无边框、超链风格
     */
    class DetailButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public DetailButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "详情" : value.toString());
            setFont(new Font("微软雅黑", Font.BOLD, 13));
            setForeground(new Color(41, 75, 166));
            setBackground(isSelected ? new Color(232, 240, 254) : Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
            return this;
        }
    }

    /**
     * 详情按钮编辑器：点击后弹出 AppointmentDetailDialog
     */
    class DetailButtonEditor extends DefaultCellEditor {
        private String label;
        private AppointmentView parentView;

        public DetailButtonEditor(JCheckBox checkBox, AppointmentView parentView) {
            super(checkBox);
            this.parentView = parentView;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "详情" : value.toString();
            JButton button = new JButton(label);
            button.setFont(new Font("微软雅黑", Font.BOLD, 13));
            button.setForeground(new Color(41, 75, 166));
            button.addActionListener(e -> {
                Long appointmentId = (Long) table.getValueAt(row, 0);
                parentView.showAppointmentDetail(appointmentId);
                fireEditingStopped();
            });
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    /**
     * 显示预约详情弹窗
     */
    void showAppointmentDetail(Long appointmentId) {
        Appointment appointment = controller.getAppointmentById(appointmentId);
        if (appointment == null) {
            JOptionPane.showMessageDialog(appointmentPanel, "未找到该预约", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Long groupId = appointment.getGroupId();
        CheckItemGroup group = groupId != null ? controller.getCheckItemGroupById(groupId) : null;
        List<CheckItem> items = groupId != null ? controller.getCheckItemsByGroupId(groupId) : new ArrayList<>();

        AppointmentDetailDialog dialog = new AppointmentDetailDialog(appointment, group, items);
        dialog.setLocationRelativeTo(appointmentPanel);
        dialog.setVisible(true);
    }

    /**
     * 处理打印报告按钮：弹出选择对话框（全部打印 / 单条打印）
     */
    private void handlePrintReport(ActionEvent e) {
        int rowCount = appointmentTable.getRowCount();
        int selectedRow = appointmentTable.getSelectedRow();

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(appointmentPanel, "没有可打印的预约记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 弹出选择对话框
        String[] options;
        if (selectedRow >= 0) {
            options = new String[]{"打印全部预约", "打印选中预约", "取消"};
        } else {
            options = new String[]{"打印全部预约", "取消"};
        }

        int choice = JOptionPane.showOptionDialog(appointmentPanel,
                "请选择打印方式：\n\n" +
                "全部预约：按顺序导出所有预约记录\n" +
                "选中预约：仅导出当前选中的一条记录",
                "打印预约报告",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            // 打印全部
            printAllAppointments(appointmentTable);
        } else if (choice == 1 && selectedRow >= 0) {
            // 打印选中
            Long appointmentId = (Long) appointmentTable.getValueAt(selectedRow, 0);
            printSingleAppointment(appointmentId);
        }
    }

    /**
     * 打印全部预约
     */
    private void printAllAppointments(JTable table) {
        int rowCount = appointmentTable.getRowCount();
        List<Appointment> appointments = new ArrayList<>();
        List<CheckItemGroup> groups = new ArrayList<>();
        List<List<CheckItem>> allItems = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Long appointmentId = (Long) appointmentTable.getValueAt(i, 0);
            Appointment appointment = controller.getAppointmentById(appointmentId);
            if (appointment == null) continue;
            appointments.add(appointment);
            groups.add(controller.getCheckGroupByAppointmentId(appointmentId));
            allItems.add(controller.getCheckItemsByAppointmentId(appointmentId));
        }

        if (appointments.isEmpty()) {
            JOptionPane.showMessageDialog(appointmentPanel, "没有可导出的预约", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存全部预约报告");
        fileChooser.setSelectedFile(new java.io.File("预约报告_全部_" +
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".docx"));

        if (fileChooser.showSaveDialog(appointmentPanel) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".docx")) {
                filePath += ".docx";
            }
            try {
                WordExportService exportService = new WordExportService();
                exportService.exportBatchReport(filePath, appointments, groups, allItems);
                JOptionPane.showMessageDialog(appointmentPanel,
                        "全部报告已成功导出到：\n" + filePath,
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(appointmentPanel,
                        "导出失败：" + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 打印单条预约
     */
    private void printSingleAppointment(Long appointmentId) {
        Appointment appointment = controller.getAppointmentById(appointmentId);
        if (appointment == null) {
            JOptionPane.showMessageDialog(appointmentPanel, "未找到该预约", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CheckItemGroup group = controller.getCheckGroupByAppointmentId(appointmentId);
        List<CheckItem> items = controller.getCheckItemsByAppointmentId(appointmentId);

        String defaultFileName = "预约报告_" +
                (group != null ? group.getGroupName() : "未知") + "_" +
                (appointment.getExamDate() != null
                        ? appointment.getExamDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                        : "nodate") +
                ".docx";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存预约报告");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));

        if (fileChooser.showSaveDialog(appointmentPanel) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".docx")) {
                filePath += ".docx";
            }
            try {
                WordExportService exportService = new WordExportService();
                exportService.exportAppointmentReport(filePath, appointment, group, items);
                JOptionPane.showMessageDialog(appointmentPanel,
                        "报告已成功导出到：\n" + filePath,
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(appointmentPanel,
                        "导出失败：" + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
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
                refreshAppointmentData();
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
                return;
            }

            // 3. 使用支付服务进行支付
            PaymentService paymentService = new MockPaymentService();
            boolean paid = paymentService.pay(appointmentId, price);

            // 4. 更新支付状态
            if (paid && controller.updatePaymentStatus(appointmentId, true)) {
                JOptionPane.showMessageDialog(paymentDialog, "支付成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                paymentDialog.dispose();
                refreshAppointmentData();
            } else {
                JOptionPane.showMessageDialog(paymentDialog, "支付失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(confirmBtn, BorderLayout.CENTER);
        paymentDialog.add(panel);
        paymentDialog.setVisible(true);
    }

}


