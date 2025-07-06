package com.secureoffice.backend.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {
    
    private Long recipientId;
    private Long departmentId;
    
    @NotNull(message = "Message type is required")
    private String messageType; // DIRECT, DEPARTMENT, ANNOUNCEMENT
    
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message content must not exceed 5000 characters")
    private String content;
    
    public CreateMessageRequest() {}
    
    public CreateMessageRequest(Long recipientId, String messageType, String subject, String content) {
        this.recipientId = recipientId;
        this.messageType = messageType;
        this.subject = subject;
        this.content = content;
    }
    
    public CreateMessageRequest(Long departmentId, String messageType, String subject, String content, boolean isDepartment) {
        this.departmentId = departmentId;
        this.messageType = messageType;
        this.subject = subject;
        this.content = content;
    }
    
    public Long getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
