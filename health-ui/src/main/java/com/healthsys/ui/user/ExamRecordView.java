package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.service.ExamRecordCellRenderer;
import com.healthsys.service.ExamService;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Doctor;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.Report;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.Users;
import com.healthsys.dao.DoctorDAO;
import com.healthsys.dao.ReportDAO;
import com.healthsys.ui.HealthTheme;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体检信息面板 — 展示用户最近一次体检结果
 */
public class ExamRecordView {
    private JPanel healthPanel;
    private final ExamService examService;
    private final AppointmentService appointmentService;
    private final ReportDAO reportDAO;
    private final DoctorDAO doctorDAO;
    private final Users currentUser;
    private JTable resultsTable;
    private ExamResultTableModel resultsModel;
    private JLabel infoLabel;
    private JTextArea reportArea;
    private JPanel reportPanel;
    private JButton printButton;
    private Appointment latestAppointment;
    private CheckItemGroup latestGroup;
    private List<ExamRecord> latestRecords;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ExamRecordView(Users currentUser) {
        this.currentUser = currentUser;
        this.examService = new ExamService();
        this.appointmentService = new AppointmentService();
        this.reportDAO = new ReportDAO();
        this.doctorDAO = new DoctorDAO();
        initializeUI();
    }

    private void initializeUI() {
        healthPanel = new JPanel(new BorderLayout(0, 0));
        healthPanel.setBackground(Color.WHITE);
        healthPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== 标题栏 =====
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("体检信息", JLabel.CENTER);
        titleLabel.setFont(HealthTheme.FONT_TITLE);
        titleLabel.setForeground(HealthTheme.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("查看您最近一次的体检结果", JLabel.CENTER);
        subtitleLabel.setFont(HealthTheme.FONT_BODY_SM);
        subtitleLabel.setForeground(HealthTheme.TEXT_HINT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // 打印按钮 - 使用系统主色
        printButton = new JButton("打印报告");
        printButton.setFont(HealthTheme.FONT_BUTTON);
        printButton.setBackground(HealthTheme.BTN_PRIMARY);
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.setBorderPainted(false);
        printButton.setContentAreaFilled(true);
        printButton.setOpaque(true);
        printButton.setPreferredSize(new Dimension(120, 40));
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printButton.addActionListener(e -> handlePrint());
        printButton.setEnabled(false);

        // 添加圆角边框效果
        printButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createLineBorder(HealthTheme.BTN_PRIMARY, 1)
        ));

        // 添加鼠标悬停效果
        printButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (printButton.isEnabled()) {
                    printButton.setBackground(HealthTheme.PRIMARY_DARK);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (printButton.isEnabled()) {
                    printButton.setBackground(HealthTheme.BTN_PRIMARY);
                }
            }
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightHeader.setBackground(Color.WHITE);
        rightHeader.add(printButton);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // ===== 概要信息 =====
        infoLabel = new JLabel(" ", JLabel.LEFT);
        infoLabel.setFont(HealthTheme.FONT_BODY_SM);
        infoLabel.setForeground(HealthTheme.TEXT_SECONDARY);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // ===== 报告摘要 =====
        reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "医生综合报告"
        ));
        reportPanel.setBackground(Color.WHITE);

        reportArea = new JTextArea();
        reportArea.setFont(HealthTheme.FONT_BODY_SM);
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setBackground(HealthTheme.BG_SECONDARY);
        reportArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setPreferredSize(new Dimension(-1, 120));
        reportPanel.add(reportScroll, BorderLayout.CENTER);
        reportPanel.setVisible(false);

        // ===== 结果表格 =====
        resultsModel = new ExamResultTableModel();
        resultsTable = new JTable(resultsModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable.setRowHeight(36);
        resultsTable.setFont(HealthTheme.FONT_BODY_SM);
        resultsTable.getTableHeader().setFont(HealthTheme.FONT_BUTTON);
        resultsTable.getTableHeader().setBackground(HealthTheme.TABLE_HEADER);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        resultsTable.getTableHeader().setResizingAllowed(false);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsTable.setSelectionBackground(HealthTheme.TABLE_SELECTED);
        resultsTable.setSelectionForeground(HealthTheme.TEXT_PRIMARY);
        resultsTable.setGridColor(HealthTheme.BORDER);
        resultsTable.setShowHorizontalLines(true);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 0));
        resultsTable.setBackground(Color.WHITE);

        // 自定义表头渲染器，确保文字清晰可见且居中
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(HealthTheme.TABLE_HEADER);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(HealthTheme.FONT_BUTTON);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getTableHeader().setDefaultRenderer(headerRenderer);

        // 创建居中对齐的渲染器（用于所有数据列）
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); // 左右留白

        // 为所有列设置居中渲染器
        for (int i = 0; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 设置列宽 - 优化分配
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(70);

        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        resultsTable.getColumnModel().getColumn(5).setMaxWidth(130);

        // 状态列使用特殊渲染器（继承居中）
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(new AbnormalRenderer());

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(HealthTheme.BORDER, 1));
        tableScroll.getViewport().setBackground(Color.WHITE);

        // ===== 使用 GridBagLayout 精确控制布局 =====
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // 1. 添加信息面板（顶部）
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        gbc.gridy = 0;
        gbc.weighty = 0; // 不占用额外空间
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(infoPanel, gbc);

        // 2. 添加报告面板（中部，如果有报告才添加）
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // 先不添加到面板，等加载数据时再动态添加

        // 3. 添加表格（占据所有剩余空间）
        gbc.gridy = 2;
        gbc.weighty = 1.0; // 关键：让表格占据所有剩余垂直空间
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(tableScroll, gbc);

        // 将内容面板放入滚动面板
        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getViewport().setBackground(Color.WHITE);

        healthPanel.add(headerPanel, BorderLayout.NORTH);
        healthPanel.add(mainScroll, BorderLayout.CENTER);

        // 初次加载
        loadLatestExamRecords();
    }

    /**
     * 加载最近一次体检记录
     */
    private void loadLatestExamRecords() {
        resultsModel.clear();
        reportArea.setText("");
        reportPanel.setVisible(false);
        printButton.setEnabled(false);

        List<Appointment> completedAppointments = appointmentService.getUserAppointmentsByStatus(
                currentUser.getId(), "COMPLETED");

        if (completedAppointments.isEmpty()) {
            infoLabel.setText("暂无已完成体检记录");
            return;
        }

        // 按 examDate 降序取最近一次
        completedAppointments.sort((a, b) -> {
            if (a.getExamDate() == null) return 1;
            if (b.getExamDate() == null) return -1;
            return b.getExamDate().compareTo(a.getExamDate());
        });

        latestAppointment = completedAppointments.get(0);
        Long groupId = latestAppointment.getGroupId();
        latestGroup = groupId != null ? appointmentService.getCheckItemGroupById(groupId) : null;

        // 医生信息
        String doctorInfo = "";
        Long doctorId = latestAppointment.getDoctorId();
        if (doctorId != null && doctorId > 0) {
            Doctor doctor = doctorDAO.getById(doctorId);
            if (doctor != null) {
                doctorInfo = " | 医生：" + doctor.getName()
                        + (doctor.getTitle() != null ? " " + doctor.getTitle() : "");
            }
        }

        String examDateStr = latestAppointment.getExamDate() != null
                ? latestAppointment.getExamDate().format(DATE_FMT) : "未知";

        infoLabel.setText(String.format("检查组：%s  |  体检日期：%s%s",
                latestGroup != null ? latestGroup.getGroupName() : "未知套餐",
                examDateStr, doctorInfo));

        // 加载检查结果
        latestRecords = examService.getExamRecordsByAppointment(latestAppointment.getId());

        // 加载报告
        Report report = reportDAO.getByAppointmentId(latestAppointment.getId());
        if (report != null && report.getSummary() != null && !report.getSummary().isEmpty()) {
            reportArea.setText(report.getSummary());
            reportPanel.setVisible(true);

            // 动态添加报告面板到布局中
            addReportPanelToLayout();
        }

        // 填充表格
        if (latestRecords != null) {
            // 构建 item 映射表
            Map<Long, CheckItem> itemMap = new HashMap<>();
            if (latestGroup != null) {
                List<CheckItem> items = appointmentService.getCheckItemsByGroupId(latestGroup.getId());
                for (CheckItem item : items) {
                    itemMap.put(item.getId(), item);
                }
            }
            resultsModel.setData(latestRecords, itemMap);
        }

        printButton.setEnabled(true);
    }

    /**
     * 动态添加报告面板到布局中
     */
    private void addReportPanelToLayout() {
        // 获取 contentPanel
        Component[] components = healthPanel.getComponents();
        JPanel contentPanel = null;
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                if (scrollPane.getViewport().getView() instanceof JPanel) {
                    contentPanel = (JPanel) scrollPane.getViewport().getView();
                    break;
                }
            }
        }

        if (contentPanel == null) return;

        // 检查是否已经添加了报告面板
        Component[] contentComponents = contentPanel.getComponents();
        boolean hasReportPanel = false;
        for (Component comp : contentComponents) {
            if (comp == reportPanel) {
                hasReportPanel = true;
                break;
            }
        }

        if (!hasReportPanel) {
            // 重新构建布局，在信息面板和表格之间插入报告面板
            contentPanel.removeAll();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);

            // 1. 信息面板
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBackground(Color.WHITE);
            infoPanel.add(infoLabel, BorderLayout.NORTH);
            gbc.gridy = 0;
            gbc.weighty = 0;
            contentPanel.add(infoPanel, gbc);

            // 2. 报告面板
            gbc.gridy = 1;
            gbc.weighty = 0;
            contentPanel.add(reportPanel, gbc);

            // 3. 表格
            Component tableComp = null;
            for (Component comp : contentComponents) {
                if (comp instanceof JScrollPane) {
                    tableComp = comp;
                    break;
                }
            }
            if (tableComp == null) {
                // 如果没有找到表格，重新创建
                JScrollPane tableScroll = new JScrollPane(resultsTable);
                tableScroll.setBorder(BorderFactory.createLineBorder(HealthTheme.BORDER, 1));
                tableScroll.getViewport().setBackground(Color.WHITE);
                gbc.gridy = 2;
                gbc.weighty = 1.0;
                gbc.fill = GridBagConstraints.BOTH;
                contentPanel.add(tableScroll, gbc);
            } else {
                // 重新添加表格
                gbc.gridy = 2;
                gbc.weighty = 1.0;
                gbc.fill = GridBagConstraints.BOTH;
                contentPanel.add(tableComp, gbc);
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    public void refreshData() {
        loadLatestExamRecords();
    }

    private void handlePrint() {
        if (latestAppointment == null) {
            JOptionPane.showMessageDialog(healthPanel, "没有可打印的体检记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String defaultFileName = "体检报告_"
                + (latestGroup != null ? sanitizeFileName(latestGroup.getGroupName()) : "未知")
                + "_" + (latestAppointment.getExamDate() != null
                ? latestAppointment.getExamDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : "nodate")
                + ".docx";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存体检报告");
        fileChooser.setSelectedFile(new File(defaultFileName));

        if (fileChooser.showSaveDialog(healthPanel) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".docx")) {
                filePath += ".docx";
            }
            try {
                // 构建 item 映射表
                Map<Long, CheckItem> itemMap = new HashMap<>();
                if (latestGroup != null) {
                    List<CheckItem> items = appointmentService.getCheckItemsByGroupId(latestGroup.getId());
                    for (CheckItem item : items) {
                        itemMap.put(item.getId(), item);
                    }
                }

                Report report = reportDAO.getByAppointmentId(latestAppointment.getId());

                WordExportService exportService = new WordExportService();
                exportService.exportExamRecordReport(filePath, currentUser, latestAppointment,
                        latestGroup, latestRecords, itemMap, report);
                JOptionPane.showMessageDialog(healthPanel,
                        "报告已成功导出到：\n" + filePath,
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(healthPanel,
                        "导出失败：" + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public JPanel getHealthPanel() {
        return healthPanel;
    }

    /**
     * 检查结果表格模型
     */
    private static class ExamResultTableModel extends AbstractTableModel {
        private final String[] columns = {"序号", "检查项目", "结果值", "单位", "参考范围", "状态"};
        private List<ExamRecord> data = new ArrayList<>();
        private Map<Long, CheckItem> itemMap = new HashMap<>();

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Integer.class;
            return String.class;
        }

        void setData(List<ExamRecord> data, Map<Long, CheckItem> itemMap) {
            this.data = data != null ? data : new ArrayList<>();
            this.itemMap = itemMap != null ? itemMap : new HashMap<>();
            fireTableDataChanged();
        }

        void clear() {
            this.data = new ArrayList<>();
            this.itemMap = new HashMap<>();
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ExamRecord record = data.get(row);
            CheckItem item = itemMap.get(record.getItemId());
            switch (col) {
                case 0: return row + 1;
                case 1:
                    // 优先使用record中的itemName，如果没有则从itemMap中获取
                    String itemName = record.getItemName();
                    if (itemName == null || itemName.trim().isEmpty()) {
                        itemName = item != null ? item.getItemName() : "未知";
                    }
                    return itemName;
                case 2: return record.getResultValue() != null ? record.getResultValue() : "";
                case 3: return item != null && item.getUnit() != null ? item.getUnit() : "";
                case 4: return item != null && item.getReferenceRange() != null ? item.getReferenceRange() : "";
                case 5: return Boolean.TRUE.equals(record.getIsAbnormal()) ? "异常" : "正常";
                default: return "";
            }
        }
    }

    /**
     * 状态列渲染器 - 居中显示，无颜色区分
     */
    private static class AbnormalRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            // 设置为居中
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); // 添加左右内边距
            lbl.setFont(HealthTheme.FONT_BODY_SM); // 使用系统字体

            // 选中时使用系统默认选中色
            if (isSelected) {
                lbl.setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                // 未选中时使用默认颜色
                lbl.setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            lbl.setOpaque(true);
            return lbl;
        }
    }
}