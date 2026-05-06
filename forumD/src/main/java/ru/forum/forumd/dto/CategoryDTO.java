package ru.forum.forumd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String slug;

    @JsonProperty("sortOrder")
    private Integer sortOrder;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("createdAt")
    private String createdAt;  // Изменили на String

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}