package com.healthsys.common.entity;

import java.time.LocalDateTime;

/**
 * 管理员表 (admins)
 */
public class Admin {
    private Long adminId;
    private String username;
    private String passwordHash;
    private String realName;
    private String role;     // SUPER_ADMIN / MANAGER
    private String phone;
    private Integer status;  // 1-启用 0-禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Admin() {}

    // Getters and Setters
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusDisplay() {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
