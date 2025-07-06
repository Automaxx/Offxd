package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.ActivityLog;
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
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUser(User user);
    
    List<ActivityLog> findByAction(String action);
    
    List<ActivityLog> findByEntityType(String entityType);
    
    Page<ActivityLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT al FROM ActivityLog al WHERE al.user = :user AND al.createdAt >= :startDate ORDER BY al.createdAt DESC")
    List<ActivityLog> findByUserAndCreatedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT al FROM ActivityLog al WHERE al.createdAt >= :startDate ORDER BY al.createdAt DESC")
    Page<ActivityLog> findByCreatedAtAfter(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    @Query("SELECT al FROM ActivityLog al WHERE al.action = :action AND al.createdAt >= :startDate")
    List<ActivityLog> findByActionAndCreatedAtAfter(@Param("action") String action, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(al) FROM ActivityLog al WHERE al.user = :user AND al.createdAt >= :startDate")
    long countByUserAndCreatedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(al) FROM ActivityLog al WHERE al.action = :action AND al.createdAt >= :startDate")
    long countByActionAndCreatedAtAfter(@Param("action") String action, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT al.action, COUNT(al) FROM ActivityLog al WHERE al.createdAt >= :startDate GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> getActionStatisticsSince(@Param("startDate") LocalDateTime startDate);
    
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
