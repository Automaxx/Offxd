package com.secureoffice.backend.service;

import com.secureoffice.backend.dto.message.CreateMessageRequest;
import com.secureoffice.backend.dto.message.MessageResponse;
import com.secureoffice.backend.exception.ResourceNotFoundException;
import com.secureoffice.backend.model.Department;
import com.secureoffice.backend.model.Message;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.DepartmentRepository;
import com.secureoffice.backend.repository.MessageRepository;
import com.secureoffice.backend.repository.UserRepository;
import com.secureoffice.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageResponse sendMessage(CreateMessageRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User sender = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            Message.MessageType messageType = Message.MessageType.valueOf(request.getMessageType().toUpperCase());
            Message message = new Message(sender, request.getContent(), messageType);
            message.setSubject(request.getSubject());

            switch (messageType) {
                case DIRECT:
                    if (request.getRecipientId() == null) {
                        throw new RuntimeException("Recipient ID is required for direct messages");
                    }
                    User recipient = userRepository.findById(request.getRecipientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with id: " + request.getRecipientId()));
                    message.setRecipient(recipient);
                    
                    // Send notification to recipient
                    notificationService.createNotification(
                        recipient,
                        "New Message",
                        "You have a new message from " + sender.getFullName(),
                        com.secureoffice.backend.model.Notification.NotificationType.INFO,
                        "MESSAGE",
                        null
                    );
                    break;

                case DEPARTMENT:
                    if (request.getDepartmentId() == null) {
                        throw new RuntimeException("Department ID is required for department messages");
                    }
                    Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
                    
                    // Check if sender is part of the department
                    if (!department.getUsers().contains(sender)) {
                        throw new RuntimeException("You are not a member of this department");
                    }
                    
                    message.setDepartment(department);
                    
                    // Send notifications to all department members except sender
                    department.getUsers().stream()
                        .filter(user -> !user.getId().equals(sender.getId()))
                        .forEach(user -> {
                            notificationService.createNotification(
                                user,
                                "Department Message",
                                "New message in " + department.getName() + " from " + sender.getFullName(),
                                com.secureoffice.backend.model.Notification.NotificationType.INFO,
                                "MESSAGE",
                                null
                            );
                        });
                    break;

                case ANNOUNCEMENT:
                    // Only admins and managers can send announcements
                    if (sender.getRole() != User.Role.ADMIN && sender.getRole() != User.Role.MANAGER) {
                        throw new RuntimeException("Only administrators and managers can send announcements");
                    }
                    
                    // Send notifications to all active users except sender
                    List<User> allUsers = userRepository.findByIsActiveTrue();
                    allUsers.stream()
                        .filter(user -> !user.getId().equals(sender.getId()))
                        .forEach(user -> {
                            notificationService.createNotification(
                                user,
                                "New Announcement",
                                "New announcement from " + sender.getFullName(),
                                com.secureoffice.backend.model.Notification.NotificationType.INFO,
                                "MESSAGE",
                                null
                            );
                        });
                    break;
            }

            Message savedMessage = messageRepository.save(message);

            // Log activity
            activityLogService.logMessageActivity(sender, "MESSAGE_SEND", savedMessage.getId(), messageType.name());

            // Send real-time message via WebSocket
            sendRealTimeMessage(savedMessage);

            return new MessageResponse(savedMessage);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid message type: " + request.getMessageType());
        }
    }

    public Page<MessageResponse> getDirectMessages(Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Message> messages = messageRepository.findDirectMessagesByUser(user, pageable);
        return messages.map(MessageResponse::new);
    }

    public Page<MessageResponse> getDepartmentMessages(Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Department> userDepartments = departmentRepository.findByUserId(userPrincipal.getId());

        Page<Message> messages = messageRepository.findDepartmentMessagesByDepartments(userDepartments, pageable);
        return messages.map(MessageResponse::new);
    }

    public Page<MessageResponse> getAnnouncements(Pageable pageable) {
        Page<Message> messages = messageRepository.findAnnouncements(pageable);
        return messages.map(MessageResponse::new);
    }

    public Page<MessageResponse> searchMessages(String search, Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Message> messages = messageRepository.findMessagesByUserAndSearch(user, search, pageable);
        return messages.map(MessageResponse::new);
    }

    public List<MessageResponse> getUnreadMessages() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Message> messages = messageRepository.findUnreadMessagesByRecipient(user);
        return messages.stream().map(MessageResponse::new).collect(Collectors.toList());
    }

    public void markMessageAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Check if current user is the recipient
        if (message.getRecipient() != null && message.getRecipient().getId().equals(userPrincipal.getId())) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    public long getUnreadMessageCount() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return messageRepository.countUnreadMessagesByRecipient(user);
    }

    private void sendRealTimeMessage(Message message) {
        try {
            MessageResponse messageResponse = new MessageResponse(message);
            
            // Send to specific recipient for direct messages
            if (message.getRecipient() != null) {
                messagingTemplate.convertAndSendToUser(
                    message.getRecipient().getId().toString(),
                    "/queue/messages",
                    messageResponse
                );
            }
            
            // Send to department members for department messages
            if (message.getDepartment() != null) {
                message.getDepartment().getUsers().forEach(user -> {
                    if (!user.getId().equals(message.getSender().getId())) {
                        messagingTemplate.convertAndSendToUser(
                            user.getId().toString(),
                            "/queue/messages",
                            messageResponse
                        );
                    }
                });
            }
            
            // Send to all users for announcements
            if (message.getMessageType() == Message.MessageType.ANNOUNCEMENT) {
                messagingTemplate.convertAndSend("/topic/announcements", messageResponse);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to send real-time message: " + e.getMessage());
        }
    }
}
