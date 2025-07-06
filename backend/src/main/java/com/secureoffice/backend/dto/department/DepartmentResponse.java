package com.secureoffice.backend.dto.department;

import com.secureoffice.backend.model.Department;

import java.time.LocalDateTime;

public class DepartmentResponse {
    
    private Long id;
    private String name;
    private String description;
    private ManagerInfo manager;
    private Long userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public DepartmentResponse() {}
    
    public DepartmentResponse(Department department) {
        this.id = department.getId();
        this.name = department.getName();
        this.description = department.getDescription();
        this.createdAt = department.getCreatedAt();
        this.updatedAt = department.getUpdatedAt();
        this.userCount = (long) department.getUsers().size();
        
        if (department.getManager() != null) {
            this.manager = new ManagerInfo(
                department.getManager().getId(),
                department.getManager().getUsername(),
                department.getManager().getFullName()
            );
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ManagerInfo getManager() { return manager; }
    public void setManager(ManagerInfo manager) { this.manager = manager; }
    
    public Long getUserCount() { return userCount; }
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public static class ManagerInfo {
        private Long id;
        private String username;
        private String fullName;
        
        public ManagerInfo(Long id, String username, String fullName) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}
