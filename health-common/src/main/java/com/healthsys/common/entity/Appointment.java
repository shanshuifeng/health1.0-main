package com.healthsys.common.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Appointment {
    private Long appointmentId;
    private Long userId;
    private Long groupId;
    private LocalDateTime appointmentTime;
    private LocalDate examDate;          // 体检预约日期
    private String examTimeSlot;         // 时段（上午/下午）
    private String status;               // PENDING/CONFIRMED/COMPLETED/CANCELLED
    private Boolean paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 显示用字段（JOIN查询填充）
    private String userName;
    private String groupName;

    public Appointment() {}

    public Appointment(Long userId, Long groupId, LocalDateTime appointmentTime) {
        this.userId = userId;
        this.groupId = groupId;
        this.appointmentTime = appointmentTime;
        this.status = "PENDING";
        this.paymentStatus = false;
    }

    public String getStatusDisplay() {
        return switch (status != null ? status : "PENDING") {
            case "PENDING" -> "待检查";
            case "CONFIRMED" -> "已确认";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }

    public String getPaymentStatusDisplay() {
        return Boolean.TRUE.equals(paymentStatus) ? "已支付" : "未支付";
    }

    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public String getExamTimeSlot() { return examTimeSlot; }
    public void setExamTimeSlot(String examTimeSlot) { this.examTimeSlot = examTimeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Boolean paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    // 兼容旧代码的别名方法
    public Long getId() { return appointmentId; }
    public void setId(Long id) { this.appointmentId = id; }
}
