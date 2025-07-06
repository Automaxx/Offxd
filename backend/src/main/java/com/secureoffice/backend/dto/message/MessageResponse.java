package com.secureoffice.backend.dto.message;

import com.secureoffice.backend.model.Message;

import java.time.LocalDateTime;

public class MessageResponse {
    
    private Long id;
    private UserInfo sender;
    private UserInfo recipient;
    private DepartmentInfo department;
    private String messageType;
    private String subject;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    public MessageResponse() {}
    
    public MessageResponse(Message message) {
        this.id = message.getId();
        this.messageType = message.getMessageType().name();
        this.subject = message.getSubject();
        this.content = message.getContent();
        this.isRead = message.getIsRead();
        this.createdAt = message.getCreatedAt();
        
        if (message.getSender() != null) {
            this.sender = new UserInfo(
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getSender().getFullName()
            );
        }
        
        if (message.getRecipient() != null) {
            this.recipient = new UserInfo(
                message.getRecipient().getId(),
                message.getRecipient().getUsername(),
                message.getRecipient().getFullName()
            );
        }
        
        if (message.getDepartment() != null) {
            this.department = new DepartmentInfo(
                message.getDepartment().getId(),
                message.getDepartment().getName()
            );
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserInfo getSender() { return sender; }
    public void setSender(UserInfo sender) { this.sender = sender; }
    
    public UserInfo getRecipient() { return recipient; }
    public void setRecipient(UserInfo recipient) { this.recipient = recipient; }
    
    public DepartmentInfo getDepartment() { return department; }
    public void setDepartment(DepartmentInfo department) { this.department = department; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
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
    
    public static class DepartmentInfo {
        private Long id;
        private String name;
        
        public DepartmentInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
