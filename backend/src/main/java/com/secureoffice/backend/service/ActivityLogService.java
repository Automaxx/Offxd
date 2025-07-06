package com.secureoffice.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureoffice.backend.model.ActivityLog;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.ActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logActivity(User user, String action, String entityType, Long entityId) {
        logActivity(user, action, entityType, entityId, null);
    }

    public void logActivity(User user, String action, String entityType, Long entityId, Map<String, Object> additionalDetails) {
        try {
            ActivityLog activityLog = new ActivityLog(user, action, entityType, entityId);
            
            // Get request details if available
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                activityLog.setIpAddress(getClientIpAddress(request));
                activityLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // Add additional details if provided
            if (additionalDetails != null && !additionalDetails.isEmpty()) {
                try {
                    String detailsJson = objectMapper.writeValueAsString(additionalDetails);
                    activityLog.setDetails(detailsJson);
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to serialize activity log details: " + e.getMessage());
                }
            }

            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            // Log the error but don't throw exception to avoid breaking the main flow
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    public void logFileActivity(User user, String action, Long fileId, String filename) {
        Map<String, Object> details = new HashMap<>();
        details.put("filename", filename);
        logActivity(user, action, "FILE", fileId, details);
    }

    public void logMessageActivity(User user, String action, Long messageId, String messageType) {
        Map<String, Object> details = new HashMap<>();
        details.put("messageType", messageType);
        logActivity(user, action, "MESSAGE", messageId, details);
    }

    public Page<ActivityLog> getUserActivityLogs(User user, Pageable pageable) {
        return activityLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public Page<ActivityLog> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<ActivityLog> getRecentActivityLogs(LocalDateTime since, Pageable pageable) {
        return activityLogRepository.findByCreatedAtAfter(since, pageable);
    }

    public List<Object[]> getActionStatistics(LocalDateTime since) {
        return activityLogRepository.getActionStatisticsSince(since);
    }

    public long getUserActivityCount(User user, LocalDateTime since) {
        return activityLogRepository.countByUserAndCreatedAtAfter(user, since);
    }

    public long getActionCount(String action, LocalDateTime since) {
        return activityLogRepository.countByActionAndCreatedAtAfter(action, since);
    }

    public void cleanupOldLogs(LocalDateTime cutoffDate) {
        activityLogRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
