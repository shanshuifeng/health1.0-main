package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Report;
import com.healthsys.dao.AppointmentDAO;
import com.healthsys.dao.ReportDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReportEditDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private int result = CANCEL_OPTION;
    private final Long doctorId;
    private final Long fixedAppointmentId;
    private final Report existingReport;
    private final ReportDAO reportDAO = new ReportDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    private JComboBox<String> appointmentCombo;
    private JTextArea summaryArea;
    private List<Appointment> availableAppointments;

    /** 从报告管理页调用：用户自选预约 */
    public ReportEditDialog(Long doctorId, Report existingReport) {
        this(doctorId, null, existingReport);
    }

    /** 从预约页调用：预约已确定 */
    public ReportEditDialog(Long doctorId, Long appointmentId, Report existingReport) {
        this.doctorId = doctorId;
        this.fixedAppointmentId = appointmentId;
        this.existingReport = existingReport;
        setTitle(existingReport == null ? "撰写报告" : "编辑报告");
        setModal(true);
        setSize(600, 420);
        setLocationRelativeTo(null);
        initUI();
        if (existingReport != null || fixedAppointmentId != null) {
            appointmentCombo.setEnabled(false);
        }
        if (existingReport != null) {
            loadExistingData();
        }
        // 撰写模式下：只显示尚未写过报告的预约
        if (existingReport == null && fixedAppointmentId == null) {
            filterOutReported();
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

        // 选择预约
        JPanel apptPanel = new JPanel(new BorderLayout(10, 0));
        apptPanel.setBackground(Color.WHITE);
        apptPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel apptLabel = new JLabel("关联预约：");
        apptLabel.setFont(labelFont);
        appointmentCombo = new JComboBox<>();
        appointmentCombo.setFont(fieldFont);
        loadAppointments();
        apptPanel.add(apptLabel, BorderLayout.WEST);
        apptPanel.add(appointmentCombo, BorderLayout.CENTER);

        // 诊断总结
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 5));
        summaryPanel.setBackground(Color.WHITE);
        JLabel summaryLabel = new JLabel("诊断报告：");
        summaryLabel.setFont(labelFont);
        summaryArea = new JTextArea(8, 40);
        summaryArea.setFont(fieldFont);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setPreferredSize(new Dimension(500, 200));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        panel.add(apptPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(summaryPanel);

        return panel;
    }

    private void loadAppointments() {
        if (fixedAppointmentId != null) {
            Appointment a = appointmentDAO.getById(fixedAppointmentId);
            availableAppointments = a != null ? List.of(a) : List.of();
        } else {
            availableAppointments = appointmentDAO.searchByFilters(doctorId, null, null, "COMPLETED");
        }
        appointmentCombo.removeAllItems();
        for (Appointment a : availableAppointments) {
            String label = a.getUserName() + " — " + a.getExamDate() + " — " + a.getGroupName();
            appointmentCombo.addItem(label);
        }
    }

    private void filterOutReported() {
        appointmentCombo.removeAllItems();
        availableAppointments.removeIf(a -> reportDAO.getByAppointmentId(a.getId()) != null);
        for (Appointment a : availableAppointments) {
            String label = a.getUserName() + " — " + a.getExamDate() + " — " + a.getGroupName();
            appointmentCombo.addItem(label);
        }
    }

    private void loadExistingData() {
        // 在 combo 中找到对应预约
        for (int i = 0; i < availableAppointments.size(); i++) {
            if (availableAppointments.get(i).getId().equals(existingReport.getAppointmentId())) {
                appointmentCombo.setSelectedIndex(i);
                break;
            }
        }
        if (existingReport.getSummary() != null) {
            summaryArea.setText(existingReport.getSummary());
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

        JButton cancelBtn = CrudPanel.createStyledButton("取消", new Color(200, 200, 200));
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(cancelBtn);
        return panel;
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

        if (existingReport == null) {
            // 检查是否已有报告
            Report existing = reportDAO.getByAppointmentId(appointmentId);
            if (existing != null) {
                JOptionPane.showMessageDialog(this, "该预约已有报告，不能重复创建", "提示", JOptionPane.WARNING_MESSAGE);
                return false;
            }
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
        } else {
            existingReport.setSummary(summary);
            if (reportDAO.update(existingReport)) {
                JOptionPane.showMessageDialog(this, "报告更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
