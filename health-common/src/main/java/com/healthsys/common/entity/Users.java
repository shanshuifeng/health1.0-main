package com.healthsys.common.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Users {
    private Long userId;
    private String phone;
    private String passwordHash;
    private String realName;
    private String idCard;
    private Integer gender; // 0-未知 1-男 2-女
    private LocalDate birthDate;
    private Integer status; // 1-正常 0-禁用
    private Boolean firstLogin = true;
    private String role; // 登录路由用：MEDICAL→医护端, 其他→用户端（非DB字段）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Users() {}

    public Users(Long userId, String phone, String passwordHash, String realName,
                 String idCard, Integer gender, LocalDate birthDate, Integer status, Boolean firstLogin) {
        this.userId = userId;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.realName = realName;
        this.idCard = idCard;
        this.gender = gender;
        this.birthDate = birthDate;
        this.status = status;
        this.firstLogin = firstLogin;
    }

    public String getGenderDisplay() {
        return switch (gender != null ? gender : 0) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(Boolean firstLogin) { this.firstLogin = firstLogin; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 兼容旧代码的别名方法
    public Long getId() { return userId; }
    public void setId(Long id) { this.userId = id; }
    public String getPassword() { return passwordHash; }
    public void setPassword(String password) { this.passwordHash = password; }
    public String getName() { return realName; }
    public void setName(String name) { this.realName = name; }
    public String getIdNumber() { return idCard; }
    public void setIdNumber(String idNumber) { this.idCard = idNumber; }
}
