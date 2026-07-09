package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.service.ExamRecordCellRenderer;
import com.healthsys.service.ExamService;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExamRecordView {
    private JPanel healthPanel;
    private final ExamService examRecordController;
    private final AppointmentService appointmentController;
    private final long userId;
    private DefaultTableModel model;

    public ExamRecordView(Appointment dummyAppointment) {
        this.examRecordController = new ExamService();
        this.appointmentController = new AppointmentService();
        this.userId = dummyAppointment.getUserId();
        initializeUI();
    }

    private void initializeUI() {
        healthPanel = new JPanel(new BorderLayout(0, 15));
        healthPanel.setBackground(new Color(245, 245, 245));
        healthPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("体检信息", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 104, 197));

        JLabel subtitleLabel = new JLabel("查看您已完成的体检记录和检查结果", JLabel.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(245, 245, 245));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);

        // 表格
        String[] columnNames = {"预约ID", "检查组", "检查项目", "结果", "单位", "参考范围", "体检时间"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setSelectionBackground(new Color(220, 230, 250));
        table.setGridColor(new Color(220, 220, 220));

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);

        table.getColumnModel().getColumn(3).setCellRenderer(new ExamRecordCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        loadExamRecords();

        healthPanel.add(headerPanel, BorderLayout.NORTH);
        healthPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadExamRecords() {
        model.setRowCount(0);
        List<Appointment> completedAppointments = appointmentController.getUserAppointmentsByStatus(userId, "COMPLETED");

        for (Appointment appointment : completedAppointments) {
            Long groupId = appointment.getGroupId();
            CheckItemGroup checkItemGroup = null;
            if (groupId != null) {
                checkItemGroup = appointmentController.getCheckItemGroupById(groupId);
            }
            String groupName = checkItemGroup != null ? checkItemGroup.getName() : "通用项目";

            List<ExamRecord> records = examRecordController.getExamRecordsByAppointment(appointment.getId());
            for (ExamRecord record : records) {
                CheckItem checkItem = appointmentController.getCheckItemById(record.getTestId());
                String testName = checkItem != null ? checkItem.getName() : "未知项目";
                String unit = "";
                if (checkItem != null && checkItem.getNormalRange() != null) {
                    String[] parts = checkItem.getNormalRange().split(":");
                    if (parts.length > 1) {
                        unit = parts[1].replaceAll("[\\d.-]+", "").trim();
                    }
                }
                String normalRange = checkItem != null ? checkItem.getNormalRange() : "";

                String examTime = "";
                if (record.getExamDate() != null) {
                    examTime = record.getExamDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }

                Object[] row = {
                        appointment.getId(),
                        groupName,
                        testName,
                        record.getResultValue(),
                        unit,
                        normalRange,
                        examTime
                };
                model.addRow(row);
            }
        }
    }

    public JPanel getHealthPanel() {
        return healthPanel;
    }
}
