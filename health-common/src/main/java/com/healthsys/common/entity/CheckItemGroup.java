package com.healthsys.common.entity;

import java.time.LocalDateTime;

public class CheckItemGroup {
    private Long groupId;
    private String groupName;
    private String description;
    private Double price;
    private Integer dailyLimit; // 每日可预约名额上限
    private Integer status; // 1-上架 0-下架
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CheckItemGroup() {}

    public CheckItemGroup(Long groupId, String groupName, String description, Double price, LocalDateTime createdAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.price = price;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(Integer dailyLimit) { this.dailyLimit = dailyLimit; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusDisplay() {
        return status != null && status == 1 ? "上架" : "下架";
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 兼容旧代码的别名方法
    public Long getId() { return groupId; }
    public void setId(Long id) { this.groupId = id; }
    public String getName() { return groupName; }
    public void setName(String name) { this.groupName = name; }
}
