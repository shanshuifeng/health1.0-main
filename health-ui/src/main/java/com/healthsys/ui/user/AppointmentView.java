package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.service.ExamService;
import com.healthsys.service.MockPaymentService;
import com.healthsys.service.PaymentService;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.Report;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.dao.ReportDAO;
import com.healthsys.ui.HealthTheme;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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

    private final ExamService examService = new ExamService();
    private final ReportDAO reportDAO = new ReportDAO();

    public AppointmentView(Users currentUser) {
        this.currentUser = currentUser;
        this.controller = new AppointmentService();
        initializeUI();
    }

    private void initializeUI() {
        appointmentPanel = new JPanel(new BorderLayout(0, 0));
        appointmentPanel.setBackground(Color.WHITE);
        appointmentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== 标题栏 =====
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("我的预约", JLabel.CENTER);
        titleLabel.setFont(HealthTheme.FONT_TITLE);
        titleLabel.setForeground(HealthTheme.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("查看和管理您的体检预约记录", JLabel.CENTER);
        subtitleLabel.setFont(HealthTheme.FONT_BODY_SM);
        subtitleLabel.setForeground(HealthTheme.TEXT_HINT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // 右侧工具按钮
        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightToolbar.setBackground(Color.WHITE);

        JButton printBtn = createStyledButton("打印报告", HealthTheme.BTN_PRIMARY);
        printBtn.addActionListener(this::handlePrintReport);
        rightToolbar.add(printBtn);

        JButton viewReportBtn = createStyledButton("查看医生报告", HealthTheme.BTN_SECONDARY);
        viewReportBtn.addActionListener(this::handleViewDoctorReport);
        rightToolbar.add(viewReportBtn);

        headerPanel.add(rightToolbar, BorderLayout.EAST);

        // ===== 分类筛选按钮 =====
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton pendingBtn = createFilterButton("待检查", new Color(255, 193, 7));
        JButton completedBtn = createFilterButton("已完成", new Color(76, 175, 80));
        JButton cancelledBtn = createFilterButton("已取消", new Color(158, 158, 158));

        pendingBtn.addActionListener(e -> { currentFilter = "PENDING"; loadAppointmentData(); });
        completedBtn.addActionListener(e -> { currentFilter = "COMPLETED"; loadAppointmentData(); });
        cancelledBtn.addActionListener(e -> { currentFilter = "CANCELLED"; loadAppointmentData(); });

        filterPanel.add(pendingBtn);
        filterPanel.add(completedBtn);
        filterPanel.add(cancelledBtn);

        // 组装顶部区域
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // ===== 预约表格 =====
        String[] columnNames = { "序号", "检查组/项目", "类型", "预约时间", "详情", "支付状态", "操作" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5 || column == 6;
            }
        };

        appointmentTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5 || column == 6;
            }
        };
        
        appointmentTable.setRowHeight(36);
        appointmentTable.setFont(HealthTheme.FONT_BODY_SM);
        appointmentTable.getTableHeader().setFont(HealthTheme.FONT_BUTTON);
        appointmentTable.getTableHeader().setBackground(HealthTheme.TABLE_HEADER);
        appointmentTable.getTableHeader().setForeground(Color.WHITE);
        appointmentTable.getTableHeader().setReorderingAllowed(false);
        appointmentTable.getTableHeader().setResizingAllowed(false);
        appointmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        appointmentTable.setSelectionBackground(HealthTheme.TABLE_SELECTED);
        appointmentTable.setSelectionForeground(HealthTheme.TEXT_PRIMARY);
        appointmentTable.setGridColor(HealthTheme.BORDER);
        appointmentTable.setShowHorizontalLines(true);
        appointmentTable.setShowVerticalLines(false);
        appointmentTable.setIntercellSpacing(new Dimension(0, 0));
        appointmentTable.setBackground(Color.WHITE);
        
        // 自定义表头渲染器
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(HealthTheme.TABLE_HEADER);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(HealthTheme.FONT_BUTTON);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        appointmentTable.getTableHeader().setDefaultRenderer(headerRenderer);
        
        // 设置列宽
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        appointmentTable.getColumnModel().getColumn(0).setMaxWidth(70);
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        
        // 为数据列设置居中对齐渲染器
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < appointmentTable.getColumnCount(); i++) {
            appointmentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 自定义单元格渲染器和编辑器
        appointmentTable.getColumnModel().getColumn(4).setCellRenderer(new DetailButtonRenderer());
        appointmentTable.getColumnModel().getColumn(4).setCellEditor(new DetailButtonEditor(new JCheckBox(), this));
        appointmentTable.getColumnModel().getColumn(5).setCellRenderer(new PaymentStatusRenderer());
        appointmentTable.getColumnModel().getColumn(5).setCellEditor(new PaymentStatusEditor(new JCheckBox()));
        appointmentTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        appointmentTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        loadAppointmentData();

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HealthTheme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

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

        int index = 1;
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
                    index++,
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
                Long appointmentId = getAppointmentIdFromRow(row);
                boolean success = appointmentId != null && controller.cancelAppointment(appointmentId);
                fireEditingStopped();
                if (success) {
                    JOptionPane.showMessageDialog(table, "预约已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
                    refreshAppointmentData();
                } else {
                    JOptionPane.showMessageDialog(table, "取消失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    // ===== 支付状态按钮相关 =====

    /**
     * 支付状态渲染器：已支付显示绿色，未支付显示橙色
     */
    class PaymentStatusRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public PaymentStatusRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String text = (value == null) ? "" : value.toString();
            setText(text);
            setFont(new Font("微软雅黑", Font.BOLD, 13));

            if ("已支付".equals(text)) {
                setBackground(new Color(76, 175, 80));
                setForeground(Color.BLACK);
            } else {
                setBackground(new Color(255, 152, 0));
                setForeground(Color.BLACK);
            }
            setBorderPainted(true);
            setFocusPainted(false);
            return this;
        }
    }

    /**
     * 支付状态编辑器：点击后可进行支付操作或查看已支付状态
     */
    class PaymentStatusEditor extends DefaultCellEditor {
        private String label;

        public PaymentStatusEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.setFont(new Font("微软雅黑", Font.BOLD, 13));

            if ("已支付".equals(label)) {
                button.setBackground(new Color(76, 175, 80));
            } else {
                button.setBackground(new Color(255, 152, 0));
            }
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);

            button.addActionListener(e -> {
                Long appointmentId = getAppointmentIdFromRow(row);
                if (appointmentId == null) {
                    fireEditingStopped();
                    return;
                }
                String currentStatus = (String) table.getValueAt(row, 5);

                if ("已支付".equals(currentStatus)) {
                    // 已支付：弹出小窗显示"已支付"
                    JOptionPane.showMessageDialog(table,
                            "已支付",
                            "支付状态",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // 未支付：询问是否支付
                    int result = JOptionPane.showConfirmDialog(table,
                            "是否确认支付？",
                            "支付确认",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.YES_OPTION) {
                        // 执行支付
                        Double price = controller.getAppointmentPrice(appointmentId);
                        if (price == null) {
                            JOptionPane.showMessageDialog(table,
                                    "无法获取检查组价格", "错误", JOptionPane.ERROR_MESSAGE);
                            fireEditingStopped();
                            return;
                        }

                        int confirmPay = JOptionPane.showConfirmDialog(table,
                                "您需要支付: ¥" + price + "，是否继续？",
                                "支付确认",
                                JOptionPane.YES_NO_OPTION);

                        if (confirmPay == JOptionPane.YES_OPTION) {
                            PaymentService paymentService = new MockPaymentService();
                            boolean paid = paymentService.pay(appointmentId, price);

                            if (paid && controller.updatePaymentStatus(appointmentId, true)) {
                                JOptionPane.showMessageDialog(table,
                                        "支付成功！",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                refreshAppointmentData();
                            } else {
                                JOptionPane.showMessageDialog(table,
                                        "支付失败，请重试",
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
                fireEditingStopped();
            });
            return button;
        }

        @Override
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
                Long appointmentId = getAppointmentIdFromRow(row);
                if (appointmentId != null) {
                    parentView.showAppointmentDetail(appointmentId);
                }
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

        // 已处理预约：额外加载检查结果和报告
        List<ExamRecord> examRecords = new ArrayList<>();
        Report report = null;
        if ("COMPLETED".equals(appointment.getStatus())) {
            examRecords = examService.getExamRecordsByAppointment(appointmentId);
            report = reportDAO.getByAppointmentId(appointmentId);
        }

        AppointmentDetailDialog dialog = new AppointmentDetailDialog(appointment, group, items, examRecords, report);
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
            printAllAppointments();
        } else if (choice == 1 && selectedRow >= 0) {
            // 打印选中
            Long appointmentId = getAppointmentIdFromRow(selectedRow);
            if (appointmentId != null) {
                printSingleAppointment(appointmentId);
            }
        }
    }

    /**
     * 查看医生报告：选中预约后，展示医生撰写的报告内容，并提供打印选项
     */
    private void handleViewDoctorReport(ActionEvent e) {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(appointmentPanel,
                    "请先在表格中选择一条预约记录",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long appointmentId = getAppointmentIdFromRow(selectedRow);
        if (appointmentId == null) {
            JOptionPane.showMessageDialog(appointmentPanel, "未找到该预约", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Appointment appointment = controller.getAppointmentById(appointmentId);
        if (appointment == null) {
            JOptionPane.showMessageDialog(appointmentPanel, "未找到该预约", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!"COMPLETED".equals(appointment.getStatus())) {
            JOptionPane.showMessageDialog(appointmentPanel,
                    "该预约尚未完成检查，暂无医生报告",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Report report = reportDAO.getByAppointmentId(appointmentId);
        if (report == null) {
            JOptionPane.showMessageDialog(appointmentPanel,
                    "医生尚未撰写报告",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 弹出报告查看窗口
        showDoctorReportDialog(appointment, report);
    }

    /**
     * 显示医生报告弹窗，包含报告内容和打印按钮
     */
    private void showDoctorReportDialog(Appointment appointment, Report report) {
        JDialog reportDialog = new JDialog();
        reportDialog.setTitle("医生报告");
        reportDialog.setSize(550, 450);
        reportDialog.setLocationRelativeTo(appointmentPanel);
        reportDialog.setModal(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部信息
        CheckItemGroup group = controller.getCheckGroupByAppointmentId(appointment.getId());
        String groupName = group != null ? group.getGroupName() : "未知";

        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("检查组：").append(groupName).append("\n");
        headerInfo.append("检查日期：");
        if (appointment.getExamDate() != null) {
            headerInfo.append(appointment.getExamDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            headerInfo.append("未知");
        }
        headerInfo.append("\n");
        headerInfo.append("撰写医生：").append(report.getDoctorName() != null ? report.getDoctorName() : "未知");
        headerInfo.append("\n");
        headerInfo.append("报告时间：");
        if (report.getUploadTime() != null) {
            headerInfo.append(report.getUploadTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        headerInfo.append("\n");

        JLabel headerLabel = new JLabel("<html>" + headerInfo.toString().replace("\n", "<br>") + "</html>");
        headerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        headerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // 报告正文
        JTextArea reportArea = new JTextArea();
        reportArea.setText(report.getSummary() != null ? report.getSummary() : "（无内容）");
        reportArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton printBtn = new JButton("打印报告");
        printBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        printBtn.setBackground(new Color(70, 104, 197));
        printBtn.setForeground(Color.BLACK);
        printBtn.setFocusPainted(false);
        printBtn.setPreferredSize(new Dimension(120, 35));
        printBtn.addActionListener(ev -> {
            // 获取检查组和检查项用于导出
            CheckItemGroup g = controller.getCheckGroupByAppointmentId(appointment.getId());
            List<CheckItem> items = controller.getCheckItemsByAppointmentId(appointment.getId());
            List<ExamRecord> examRecords = examService.getExamRecordsByAppointment(appointment.getId());

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存医生报告");
            String defaultFileName = "医生报告_" +
                    (g != null ? g.getGroupName() : "未知") + "_" +
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".docx";
            fileChooser.setSelectedFile(new File(defaultFileName));

            if (fileChooser.showSaveDialog(reportDialog) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".docx")) {
                    filePath += ".docx";
                }
                try {
                    WordExportService exportService = new WordExportService();
                    exportService.exportAppointmentReport(filePath, appointment, g, items);
                    JOptionPane.showMessageDialog(reportDialog,
                            "报告已成功导出到：\n" + filePath,
                            "导出成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(reportDialog,
                            "导出失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        bottomPanel.add(printBtn);

        JButton closeBtn = new JButton("关闭");
        closeBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        closeBtn.setBackground(new Color(158, 158, 158));
        closeBtn.setForeground(Color.BLACK);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(120, 35));
        closeBtn.addActionListener(ev -> reportDialog.dispose());
        bottomPanel.add(closeBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        reportDialog.add(mainPanel);
        reportDialog.setVisible(true);
    }

    /**
     * 打印全部预约
     */
    private void printAllAppointments() {
        List<Appointment> appointments = controller.getUserAppointmentsByStatus(
                currentUser.getId(), currentFilter);
        List<CheckItemGroup> groups = new ArrayList<>();
        List<List<CheckItem>> allItems = new ArrayList<>();

        for (Appointment appointment : appointments) {
            Long appointmentId = appointment.getId();
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

    /**
     * 创建美化的按钮
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(HealthTheme.FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    /**
     * 创建筛选按钮
     */
    private JButton createFilterButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(90, 32));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    /**
     * 从表格行获取预约ID（需要从数据源中查找）
     */
    private Long getAppointmentIdFromRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= tableModel.getRowCount()) {
            return null;
        }
        // 重新加载数据，找到对应的预约
        List<Appointment> appointments = controller.getUserAppointmentsByStatus(
                currentUser.getId(), currentFilter);
        if (rowIndex < appointments.size()) {
            return appointments.get(rowIndex).getId();
        }
        return null;
    }

}


