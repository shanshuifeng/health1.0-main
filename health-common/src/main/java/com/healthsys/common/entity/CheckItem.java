package com.healthsys.common.entity;

import java.time.LocalDateTime;

public class CheckItem {
    private Long itemId;
    private String itemName;
    private String code;
    private String category;        // 所属科室/分类
    private String unit;            // 计量单位
    private String referenceRange;  // 正常参考值范围
    private Double price;
    private Integer status;         // 1-启用 0-停用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CheckItem() {}

    public CheckItem(Long itemId, String itemName, String code, String category,
                     String referenceRange, Double price, LocalDateTime createdAt) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.code = code;
        this.category = category;
        this.referenceRange = referenceRange;
        this.price = price;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 兼容旧代码的别名方法
    public Long getId() { return itemId; }
    public void setId(Long id) { this.itemId = id; }
    public String getName() { return itemName; }
    public void setName(String name) { this.itemName = name; }
    public String getDescription() { return category; }
    public void setDescription(String desc) { this.category = desc; }
    public String getNormalRange() { return referenceRange; }
    public void setNormalRange(String range) { this.referenceRange = range; }
}
