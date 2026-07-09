package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.Report;
import com.healthsys.dao.AppointmentDAO;
import com.healthsys.dao.ExamRecordDAO;
import com.healthsys.dao.ReportDAO;
import com.healthsys.service.AppointmentService;
import com.healthsys.ui.user.WordExportService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class ReportEditDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private int result = CANCEL_OPTION;
    private final Long doctorId;
    private final Long fixedAppointmentId;
    private Report existingReport;
    private final ReportDAO reportDAO = new ReportDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ExamRecordDAO examRecordDAO = new ExamRecordDAO();
    private final AppointmentService appointmentService = new AppointmentService();

    private JComboBox<String> appointmentCombo;
    private JTextArea examInfoArea;
    private JTextArea summaryArea;
    private List<Appointment> availableAppointments;

    public ReportEditDialog(Long doctorId, Report existingReport) {
        this(doctorId, null, existingReport);
    }

    public ReportEditDialog(Long doctorId, Long appointmentId, Report existingReport) {
        this.doctorId = doctorId;
        this.fixedAppointmentId = appointmentId;
        this.existingReport = existingReport;
        setTitle(existingReport == null ? "撰写报告" : "编辑报告");
        setModal(true);
        setSize(650, 600);
        setLocationRelativeTo(null);
        initUI();
        if (existingReport != null || fixedAppointmentId != null) {
            appointmentCombo.setEnabled(false);
        }
        if (existingReport != null) {
            selectAppointmentInCombo(existingReport.getAppointmentId());
            loadSummaryFromReport(existingReport);
            loadExamInfo(existingReport.getAppointmentId());
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        Font labelFont = new Font("微软雅黑", Font.BOLD, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 关联预约
        JPanel apptPanel = new JPanel(new BorderLayout(10, 0));
        apptPanel.setBackground(Color.WHITE);
        apptPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel apptLabel = new JLabel("关联预约：");
        apptLabel.setFont(labelFont);
        appointmentCombo = new JComboBox<>();
        appointmentCombo.setFont(fieldFont);
        loadAppointments();
        appointmentCombo.addActionListener(e -> onAppointmentSelected());
        apptPanel.add(apptLabel, BorderLayout.WEST);
        apptPanel.add(appointmentCombo, BorderLayout.CENTER);

        // 体检信息
        JPanel examPanel = new JPanel(new BorderLayout(10, 5));
        examPanel.setBackground(Color.WHITE);
        JLabel examLabel = new JLabel("体检信息：");
        examLabel.setFont(labelFont);
        examInfoArea = new JTextArea(6, 40);
        examInfoArea.setFont(fieldFont);
        examInfoArea.setEditable(false);
        examInfoArea.setBackground(new Color(250, 250, 250));
        examInfoArea.setLineWrap(true);
        examInfoArea.setWrapStyleWord(true);
        JScrollPane examScroll = new JScrollPane(examInfoArea);
        examScroll.setPreferredSize(new Dimension(500, 120));
        examPanel.add(examLabel, BorderLayout.NORTH);
        examPanel.add(examScroll, BorderLayout.CENTER);

        // 诊断报告
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 5));
        summaryPanel.setBackground(Color.WHITE);
        JLabel summaryLabel = new JLabel("诊断报告：");
        summaryLabel.setFont(labelFont);
        summaryArea = new JTextArea(6, 40);
        summaryArea.setFont(fieldFont);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setPreferredSize(new Dimension(500, 150));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        panel.add(apptPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(examPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(summaryPanel);

        return panel;
    }

    private void loadAppointments() {
        if (fixedAppointmentId != null) {
            Appointment a = appointmentDAO.getById(fixedAppointmentId);
            availableAppointments = a != null ? List.of(a) : List.of();
        } else if (existingReport != null) {
            // 编辑模式：显示全部已完成预约
            availableAppointments = appointmentDAO.searchByFilters(doctorId, null, null, "COMPLETED");
        } else {
            // 撰写模式：只显示未撰写报告的已完成预约
            availableAppointments = appointmentDAO.searchByFilters(doctorId, null, null, "COMPLETED")
                    .stream()
                    .filter(a -> !Boolean.TRUE.equals(a.getHasReport()))
                    .toList();
        }
        appointmentCombo.removeAllItems();
        for (Appointment a : availableAppointments) {
            boolean hasReport = reportDAO.getByAppointmentId(a.getId()) != null;
            String label = a.getUserName() + " — " + a.getExamDate() + " — " + a.getGroupName()
                    + (hasReport ? "  [已报告]" : "");
            appointmentCombo.addItem(label);
        }
    }

    private void onAppointmentSelected() {
        if (fixedAppointmentId != null || existingReport != null) return;
        int idx = appointmentCombo.getSelectedIndex();
        if (idx < 0 || idx >= availableAppointments.size()) return;
        Appointment selected = availableAppointments.get(idx);
        Report r = reportDAO.getByAppointmentId(selected.getId());
        if (r != null) {
            loadSummaryFromReport(r);
        } else {
            summaryArea.setText("");
        }
        loadExamInfo(selected.getId());
    }

    private void loadExamInfo(Long appointmentId) {
        List<ExamRecord> records = examRecordDAO.getExamRecordsByAppointment(appointmentId);
        if (records.isEmpty()) {
            examInfoArea.setText("暂无检查结果");
            return;
        }

        // 获取检查组项目以取得参考范围和单位
        Appointment appt = appointmentDAO.getAppointmentById(appointmentId);
        List<CheckItem> items = appt != null && appt.getGroupId() != null
                ? appointmentService.getCheckItemsByGroupId(appt.getGroupId()) : List.of();
        java.util.Map<Long, CheckItem> itemMap = new java.util.HashMap<>();
        for (CheckItem item : items) {
            itemMap.put(item.getId(), item);
        }

        int abnormalCount = 0;
        StringBuilder sb = new StringBuilder();
        for (ExamRecord r : records) {
            CheckItem item = itemMap.get(r.getItemId());
            sb.append("● ").append(r.getItemName() != null ? r.getItemName() : "项目#" + r.getItemId())
              .append("：").append(r.getResultValue() != null ? r.getResultValue() : "-");

            // 单位和参考范围
            if (item != null) {
                if (item.getUnit() != null && !item.getUnit().isEmpty() && !"-".equals(item.getUnit())) {
                    sb.append(" ").append(item.getUnit());
                }
                if (item.getReferenceRange() != null && !item.getReferenceRange().isEmpty()) {
                    sb.append(" （参考：").append(item.getReferenceRange()).append("）");
                }
            }

            // 异常标记
            if (r.getIsAbnormal() != null && r.getIsAbnormal()) {
                sb.append(" 【异常】");
                abnormalCount++;
            }
            // 医生备注
            if (r.getDoctorNote() != null && !r.getDoctorNote().isEmpty()) {
                sb.append("\n    备注：").append(r.getDoctorNote());
            }
            sb.append("\n");
        }

        // 顶部摘要
        String header = "共 " + records.size() + " 项检查，异常 " + abnormalCount + " 项\n\n";
        examInfoArea.setText(header + sb.toString());
        examInfoArea.setCaretPosition(0);
    }

    private void selectAppointmentInCombo(Long appointmentId) {
        for (int i = 0; i < availableAppointments.size(); i++) {
            if (availableAppointments.get(i).getId().equals(appointmentId)) {
                appointmentCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadSummaryFromReport(Report r) {
        if (r.getSummary() != null) {
            summaryArea.setText(r.getSummary());
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(245, 245, 245));

        JButton saveBtn = CrudPanel.createStyledButton("保存报告", new Color(102, 204, 153));
        saveBtn.addActionListener(e -> {
            if (saveReport()) {
                result = OK_OPTION;
                dispose();
            }
        });

        JButton exportBtn = CrudPanel.createStyledButton("导出Word", new Color(153, 204, 255));
        exportBtn.addActionListener(e -> exportToWord());

        JButton cancelBtn = CrudPanel.createStyledButton("取消", new Color(200, 200, 200));
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(exportBtn);
        panel.add(cancelBtn);
        return panel;
    }

    private void exportToWord() {
        Appointment selected = getSelectedAppointment();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择关联预约", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出Word报告");
        String defaultName = "报告_" + selected.getUserName() + "_" + selected.getGroupName() + ".docx";
        chooser.setSelectedFile(new File(defaultName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String filePath = chooser.getSelectedFile().getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".docx")) filePath += ".docx";

        try {
            CheckItemGroup group = appointmentService.getCheckGroupByAppointmentId(selected.getId());
            List<CheckItem> items = appointmentService.getCheckItemsByAppointmentId(selected.getId());
            List<ExamRecord> records = examRecordDAO.getExamRecordsByAppointment(selected.getId());

            WordExportService exporter = new WordExportService();
            exporter.exportReportWithResults(filePath, selected, group, items, records, summaryArea.getText().trim());

            JOptionPane.showMessageDialog(this, "报告导出成功！\n" + filePath, "导出成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "导出失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Appointment getSelectedAppointment() {
        int idx = appointmentCombo.getSelectedIndex();
        if (idx >= 0 && idx < availableAppointments.size()) {
            return availableAppointments.get(idx);
        }
        return null;
    }

    private boolean saveReport() {
        Long appointmentId;
        if (fixedAppointmentId != null) {
            appointmentId = fixedAppointmentId;
        } else {
            int idx = appointmentCombo.getSelectedIndex();
            if (idx < 0) {
                JOptionPane.showMessageDialog(this, "请选择关联预约", "提示", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            appointmentId = availableAppointments.get(idx).getId();
        }
        String summary = summaryArea.getText().trim();
        if (summary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写诊断报告", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Report existing = reportDAO.getByAppointmentId(appointmentId);

        if (existingReport != null) {
            existingReport.setSummary(summary);
            if (reportDAO.update(existingReport)) {
                JOptionPane.showMessageDialog(this, "报告更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else if (existing != null) {
            existing.setSummary(summary);
            if (reportDAO.update(existing)) {
                JOptionPane.showMessageDialog(this, "报告更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            Report report = new Report();
            report.setAppointmentId(appointmentId);
            report.setDoctorId(doctorId);
            report.setSummary(summary);
            report.setUploadTime(LocalDateTime.now());
            if (reportDAO.create(report)) {
                JOptionPane.showMessageDialog(this, "报告保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告保存失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    public int showDialog() {
        if (fixedAppointmentId == null && availableAppointments.isEmpty()) {
            JOptionPane.showMessageDialog(getOwner(),
                existingReport != null
                    ? "当前没有已完成的预约。\n请先在「预约管理」中完成体检并录入检查结果。"
                    : "当前没有待撰写报告的已完成预约。\n已全部撰写完毕或请先在「预约管理」中完成体检。",
                "无可选预约", JOptionPane.INFORMATION_MESSAGE);
            return CANCEL_OPTION;
        }
        setVisible(true);
        return result;
    }
}
