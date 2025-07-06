package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.Department;
import com.secureoffice.backend.model.Message;
import com.secureoffice.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySender(User sender);

    List<Message> findByRecipient(User recipient);

    List<Message> findByDepartment(Department department);

    List<Message> findByMessageType(Message.MessageType messageType);

    @Query("SELECT m FROM Message m WHERE m.recipient = :user AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesByRecipient(@Param("user") User user);

    @Query("SELECT m FROM Message m WHERE (m.sender = :user OR m.recipient = :user) AND m.messageType = 'DIRECT' ORDER BY m.createdAt DESC")
    Page<Message> findDirectMessagesByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.department IN :departments AND m.messageType IN ('DEPARTMENT', 'ANNOUNCEMENT') ORDER BY m.createdAt DESC")
    Page<Message> findDepartmentMessagesByDepartments(@Param("departments") List<Department> departments,
            Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.messageType = 'ANNOUNCEMENT' ORDER BY m.createdAt DESC")
    Page<Message> findAnnouncements(Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient = :user AND m.isRead = false")
    long countUnreadMessagesByRecipient(@Param("user") User user);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :startDate")
    long countMessagesSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT m FROM Message m WHERE (m.sender = :user OR m.recipient = :user OR " +
            "m.department IN (SELECT d FROM Department d JOIN d.users u WHERE u = :user)) AND " +
            "(LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY m.createdAt DESC")
    Page<Message> findMessagesByUserAndSearch(@Param("user") User user, @Param("search") String search,
            Pageable pageable);

    // Count messages by type
    @Query("SELECT COUNT(m) FROM Message m WHERE m.messageType = :messageType")
    long countByMessageType(@Param("messageType") String messageType);

    // Count all unread messages
    @Query("SELECT COUNT(m) FROM Message m WHERE m.isRead = false")
    long countUnreadMessages();

    // Get message statistics by type
    @Query("SELECT m.messageType, COUNT(m) FROM Message m GROUP BY m.messageType")
    List<Object[]> getMessageTypeStatistics();

    // Get most active senders
    @Query("SELECT m.sender, COUNT(m) FROM Message m GROUP BY m.sender ORDER BY COUNT(m) DESC")
    Page<Object[]> getMostActiveSenders(Pageable pageable);
}
