package com.healthsys.common.entity;

import java.time.LocalDateTime;

/**
 * 检查结果明细表 (check_results)
 */
public class ExamRecord {
    private Long resultId;
    private Long appointmentId;   // 预约ID
    private Long itemId;           // 检查项ID
    private Long doctorId;         // 录入医生ID
    private String resultValue;    // 检查结果值
    private Boolean isAbnormal;    // 是否异常
    private String doctorNote;     // 医生备注
    private LocalDateTime examDate; // 体检执行时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 显示用字段（JOIN查询填充）
    private String userName;
    private String groupName;
    private String itemName;

    public ExamRecord() {}

    public ExamRecord(Long resultId, Long appointmentId, Long itemId, String resultValue, LocalDateTime examDate) {
        this.resultId = resultId;
        this.appointmentId = appointmentId;
        this.itemId = itemId;
        this.resultValue = resultValue;
        this.examDate = examDate;
    }

    // Getters and Setters
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }

    public Boolean getIsAbnormal() { return isAbnormal; }
    public void setIsAbnormal(Boolean isAbnormal) { this.isAbnormal = isAbnormal; }

    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }

    public LocalDateTime getExamDate() { return examDate; }
    public void setExamDate(LocalDateTime examDate) { this.examDate = examDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    // 兼容旧代码的别名
    public Long getId() { return resultId; }
    public void setId(Long id) { this.resultId = id; }
    public Long getTestId() { return itemId; }
    public void setTestId(Long testId) { this.itemId = testId; }
}
