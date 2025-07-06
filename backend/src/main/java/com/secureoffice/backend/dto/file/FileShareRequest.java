package com.secureoffice.backend.dto.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FileShareRequest {
    
    @NotNull(message = "File ID is required")
    private Long fileId;
    
    @NotEmpty(message = "At least one user ID is required")
    private List<Long> userIds;
    
    @NotNull(message = "Permission type is required")
    private String permissionType; // VIEW, EDIT, DOWNLOAD
    
    public FileShareRequest() {}
    
    public FileShareRequest(Long fileId, List<Long> userIds, String permissionType) {
        this.fileId = fileId;
        this.userIds = userIds;
        this.permissionType = permissionType;
    }
    
    public Long getFileId() {
        return fileId;
    }
    
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
    
    public List<Long> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
    
    public String getPermissionType() {
        return permissionType;
    }
    
    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }
}
