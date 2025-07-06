package com.secureoffice.backend.dto.file;

import com.secureoffice.backend.model.FileShare;

import java.time.LocalDateTime;

public class FileShareResponse {
    
    private Long id;
    private Long fileId;
    private String filename;
    private UserInfo user;
    private String permissionType;
    private UserInfo sharedBy;
    private LocalDateTime createdAt;
    
    public FileShareResponse() {}
    
    public FileShareResponse(FileShare fileShare) {
        this.id = fileShare.getId();
        this.fileId = fileShare.getFile().getId();
        this.filename = fileShare.getFile().getOriginalFilename();
        this.permissionType = fileShare.getPermissionType().name();
        this.createdAt = fileShare.getCreatedAt();
        
        if (fileShare.getUser() != null) {
            this.user = new UserInfo(
                fileShare.getUser().getId(),
                fileShare.getUser().getUsername(),
                fileShare.getUser().getFullName()
            );
        }
        
        if (fileShare.getSharedBy() != null) {
            this.sharedBy = new UserInfo(
                fileShare.getSharedBy().getId(),
                fileShare.getSharedBy().getUsername(),
                fileShare.getSharedBy().getFullName()
            );
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    
    public UserInfo getSharedBy() { return sharedBy; }
    public void setSharedBy(UserInfo sharedBy) { this.sharedBy = sharedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        
        public UserInfo(Long id, String username, String fullName) {
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
