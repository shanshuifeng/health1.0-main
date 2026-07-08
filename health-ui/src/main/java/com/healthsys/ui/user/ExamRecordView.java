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
    private ExamService examRecordController;
    private AppointmentService appointmentController;

    public ExamRecordView(Appointment dummyAppointment) {
        this.examRecordController = new ExamService();
        this.appointmentController = new AppointmentService();
        initializeUI(dummyAppointment);
    }

    private void initializeUI(Appointment dummyAppointment) {
        healthPanel = new JPanel(new BorderLayout());
        healthPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 表格模型定义
        String[] columnNames = {"预约ID", "检查组名称", "项目名称", "结果", "单位", "正常范围", "体检时间"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        // 设置自动调整模式
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 设置自定义渲染器（仅对"结果"列）
        table.getColumnModel().getColumn(3).setCellRenderer(new ExamRecordCellRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // 预约ID
        table.getColumnModel().getColumn(1).setPreferredWidth(60); // 检查组名称
        table.getColumnModel().getColumn(2).setPreferredWidth(60); // 项目名称
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // 结果
        table.getColumnModel().getColumn(4).setPreferredWidth(50);  // 单位
        table.getColumnModel().getColumn(5).setPreferredWidth(180); // 正常范围
        table.getColumnModel().getColumn(6).setPreferredWidth(140); // 体检时间

        JScrollPane scrollPane = new JScrollPane(table);
        loadExamRecords(model, dummyAppointment.getUserId());
        healthPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadExamRecords(DefaultTableModel model, long userId) {
        model.setRowCount(0); // 清空旧数据
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

                // 【优化点】增加时间为空的默认值处理，防止空指针
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
                        examTime // 使用处理后的字符串
                };
                model.addRow(row);
            }
        }
    }

    public JPanel getHealthPanel() {
        return healthPanel;
    }
}