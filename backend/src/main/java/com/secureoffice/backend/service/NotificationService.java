package com.secureoffice.backend.service;

import com.secureoffice.backend.model.Notification;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.NotificationRepository;
import com.secureoffice.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification createNotification(User user, String title, String message,
            Notification.NotificationType type,
            String relatedEntityType, Long relatedEntityId) {
        Notification notification = new Notification(user, title, message, type, relatedEntityType, relatedEntityId);
        Notification savedNotification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        sendRealTimeNotification(user.getId(), savedNotification);

        return savedNotification;
    }

    public Notification createNotification(User user, String title, String message,
            Notification.NotificationType type) {
        return createNotification(user, title, message, type, null, null);
    }

    public Page<Notification> getUserNotifications(Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = new User();
        user.setId(userPrincipal.getId());

        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<Notification> getUnreadNotifications() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = new User();
        user.setId(userPrincipal.getId());

        return notificationRepository.findUnreadNotificationsByUser(user);
    }

    public long getUnreadNotificationCount() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = new User();
        user.setId(userPrincipal.getId());

        return notificationRepository.countUnreadNotificationsByUser(user);
    }

    public void markAsRead(Long notificationId) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = new User();
        user.setId(userPrincipal.getId());
        notificationRepository.markAsReadByIdAndUser(notificationId, user);
    }

    public void markAllAsRead() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = new User();
        user.setId(userPrincipal.getId());

        notificationRepository.markAllAsReadByUser(user);
    }

    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        // This would need to be implemented per user if needed
        // For now, we'll keep all notifications
    }

    private void sendRealTimeNotification(Long userId, Notification notification) {
        try {
            // Send to specific user via WebSocket
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    new NotificationMessage(notification));
        } catch (Exception e) {
            // Log error but don't fail the notification creation
            System.err.println("Failed to send real-time notification: " + e.getMessage());
        }
    }

    // DTO for WebSocket messages
    public static class NotificationMessage {
        private Long id;
        private String title;
        private String message;
        private String type;
        private LocalDateTime createdAt;
        private String relatedEntityType;
        private Long relatedEntityId;

        public NotificationMessage(Notification notification) {
            this.id = notification.getId();
            this.title = notification.getTitle();
            this.message = notification.getMessage();
            this.type = notification.getNotificationType().name();
            this.createdAt = notification.getCreatedAt();
            this.relatedEntityType = notification.getRelatedEntityType();
            this.relatedEntityId = notification.getRelatedEntityId();
        }

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public String getRelatedEntityType() {
            return relatedEntityType;
        }

        public void setRelatedEntityType(String relatedEntityType) {
            this.relatedEntityType = relatedEntityType;
        }

        public Long getRelatedEntityId() {
            return relatedEntityId;
        }

        public void setRelatedEntityId(Long relatedEntityId) {
            this.relatedEntityId = relatedEntityId;
        }
    }
}
