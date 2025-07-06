package com.secureoffice.backend.dto.file;

import com.secureoffice.backend.model.File;

import java.time.LocalDateTime;

public class FileResponse {
    
    private Long id;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private String folderPath;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UploaderInfo uploadedBy;
    private String formattedFileSize;
    
    public FileResponse() {}
    
    public FileResponse(File file) {
        this.id = file.getId();
        this.filename = file.getFilename();
        this.originalFilename = file.getOriginalFilename();
        this.fileSize = file.getFileSize();
        this.mimeType = file.getMimeType();
        this.folderPath = file.getFolderPath();
        this.isPublic = file.getIsPublic();
        this.createdAt = file.getCreatedAt();
        this.updatedAt = file.getUpdatedAt();
        this.formattedFileSize = file.getFormattedFileSize();
        
        if (file.getUploadedBy() != null) {
            this.uploadedBy = new UploaderInfo(
                file.getUploadedBy().getId(),
                file.getUploadedBy().getUsername(),
                file.getUploadedBy().getFullName()
            );
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public String getFolderPath() { return folderPath; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public UploaderInfo getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UploaderInfo uploadedBy) { this.uploadedBy = uploadedBy; }
    
    public String getFormattedFileSize() { return formattedFileSize; }
    public void setFormattedFileSize(String formattedFileSize) { this.formattedFileSize = formattedFileSize; }
    
    public static class UploaderInfo {
        private Long id;
        private String username;
        private String fullName;
        
        public UploaderInfo(Long id, String username, String fullName) {
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
