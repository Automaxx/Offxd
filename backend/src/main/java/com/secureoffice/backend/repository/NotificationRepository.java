package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.Notification;
import com.secureoffice.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser(User user);
    
    List<Notification> findByUserAndIsRead(User user, Boolean isRead);
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadNotificationsByUser(@Param("user") User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.notificationType = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByUserAndType(@Param("user") User user, @Param("type") Notification.NotificationType type, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadByUser(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.user = :user")
    int markAsReadByIdAndUser(@Param("notificationId") Long notificationId, @Param("user") User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :startDate")
    long countNotificationsSince(@Param("startDate") LocalDateTime startDate);
    
    void deleteByUserAndCreatedAtBefore(User user, LocalDateTime cutoffDate);
}
