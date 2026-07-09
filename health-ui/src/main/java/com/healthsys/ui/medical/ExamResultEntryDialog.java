package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.dao.AppointmentDAO;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.service.ExamService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamResultEntryDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private int result = CANCEL_OPTION;
    private final Long doctorId;
    private final Appointment appointment;
    private final Map<Long, JTextField> resultFields = new LinkedHashMap<>();
    private final Map<Long, JCheckBox> abnormalChecks = new LinkedHashMap<>();
    private final Map<Long, JTextField> noteFields = new LinkedHashMap<>();
    private final Map<Long, String> itemNames = new LinkedHashMap<>();
    private final ExamService examService = new ExamService();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public ExamResultEntryDialog(Long doctorId, Appointment appointment) {
        this.doctorId = doctorId;
        this.appointment = appointment;
        setTitle("检查结果录入 — " + appointment.getUserName());
        setModal(true);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 顶部：预约信息
        mainPanel.add(createInfoPanel(), BorderLayout.NORTH);

        // 中部：检查项表单（可滚动）
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);

        // 底部：按钮
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        Font infoFont = new Font("微软雅黑", Font.PLAIN, 14);
        JLabel patientLabel = new JLabel("患者：" + appointment.getUserName());
        patientLabel.setFont(infoFont);
        JLabel groupLabel = new JLabel("检查组：" + appointment.getGroupName());
        groupLabel.setFont(infoFont);
        JLabel dateLabel = new JLabel("检查日期：" + appointment.getExamDate());
        dateLabel.setFont(infoFont);

        panel.add(patientLabel);
        panel.add(groupLabel);
        panel.add(dateLabel);
        return panel;
    }

    private JScrollPane createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        List<CheckItem> items = CheckItemDAO.getItemsByGroupId(appointment.getGroupId());
        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("该检查组暂无检查项", JLabel.CENTER);
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            formPanel.add(emptyLabel);
        }

        for (CheckItem item : items) {
            formPanel.add(createItemRow(item));
            formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createItemRow(CheckItem item) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        // 左侧：检查项名称 + 参考范围
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(item.getItemName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        JLabel refLabel = new JLabel("参考范围：" + (item.getReferenceRange() != null ? item.getReferenceRange() : "—"));
        refLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        refLabel.setForeground(Color.GRAY);
        leftPanel.add(nameLabel);
        leftPanel.add(refLabel);
        row.add(leftPanel, BorderLayout.WEST);

        // 中间：结果值输入
        JPanel midPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        midPanel.setBackground(Color.WHITE);
        JTextField resultField = new JTextField(10);
        resultField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        midPanel.add(new JLabel("结果："));
        midPanel.add(resultField);
        midPanel.add(new JLabel(item.getUnit() != null ? item.getUnit() : ""));
        row.add(midPanel, BorderLayout.CENTER);

        // 右侧：异常标记 + 备注
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rightPanel.setBackground(Color.WHITE);
        JCheckBox abnormalCheck = new JCheckBox("异常");
        abnormalCheck.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        abnormalCheck.setBackground(Color.WHITE);
        JTextField noteField = new JTextField(15);
        noteField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(abnormalCheck);
        rightPanel.add(new JLabel("备注："));
        rightPanel.add(noteField);
        row.add(rightPanel, BorderLayout.EAST);

        resultFields.put(item.getItemId(), resultField);
        abnormalChecks.put(item.getItemId(), abnormalCheck);
        noteFields.put(item.getItemId(), noteField);
        itemNames.put(item.getItemId(), item.getItemName());

        return row;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(245, 245, 245));

        JButton saveBtn = CrudPanel.createStyledButton("保存结果", new Color(102, 204, 153));
        saveBtn.addActionListener(e -> {
            if (saveResults()) {
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

    private boolean saveResults() {
        boolean hasAny = false;
        for (Map.Entry<Long, JTextField> entry : resultFields.entrySet()) {
            Long itemId = entry.getKey();
            String value = entry.getValue().getText().trim();
            if (value.isEmpty()) continue;

            hasAny = true;
            ExamRecord record = new ExamRecord();
            record.setAppointmentId(appointment.getId());
            record.setItemId(itemId);
            record.setDoctorId(doctorId);
            record.setResultValue(value);
            record.setIsAbnormal(abnormalChecks.get(itemId).isSelected());
            String note = noteFields.get(itemId).getText().trim();
            record.setDoctorNote(note.isEmpty() ? null : note);
            record.setExamDate(LocalDateTime.now());

            if (examService.existsRecord(appointment.getId(), itemId)) {
                String itemName = itemNames.getOrDefault(itemId, String.valueOf(itemId));
                JOptionPane.showMessageDialog(this,
                        "检查项「" + itemName + "」已有记录，请勿重复录入。",
                        "录入失败", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            examService.addExamRecord(record);
        }

        if (!hasAny) {
            JOptionPane.showMessageDialog(this, "请至少录入一项检查结果", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 认领预约并标记完成
        appointmentDAO.assignDoctor(appointment.getId(), doctorId, "COMPLETED");

        JOptionPane.showMessageDialog(this, "检查结果保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
