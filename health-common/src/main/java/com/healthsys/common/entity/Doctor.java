package com.healthsys.common.entity;

import java.time.LocalDateTime;

/**
 * 医生表 (doctors)
 */
public class Doctor {
    private Long doctorId;
    private String username;
    private String passwordHash;
    private String name;
    private String department;
    private String title;
    private Integer status; // 1-在职 0-离职
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Doctor() {}

    // Getters and Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusDisplay() {
        return status != null && status == 1 ? "在职" : "离职";
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
