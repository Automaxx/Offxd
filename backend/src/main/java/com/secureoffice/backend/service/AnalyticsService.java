package com.secureoffice.backend.service;

import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countActiveUsers());
        stats.put("adminCount", userRepository.countActiveUsersByRole(User.Role.ADMIN));
        stats.put("managerCount", userRepository.countActiveUsersByRole(User.Role.MANAGER));
        stats.put("employeeCount", userRepository.countActiveUsersByRole(User.Role.EMPLOYEE));

        // File statistics
        stats.put("totalFiles", fileRepository.count());
        Long totalFileSize = fileRepository.getTotalFileSize();
        stats.put("totalFileSize", totalFileSize != null ? totalFileSize : 0L);
        stats.put("totalFileSizeFormatted", formatFileSize(totalFileSize != null ? totalFileSize : 0L));

        // Message statistics
        stats.put("totalMessages", messageRepository.count());

        // Department statistics
        stats.put("totalDepartments", departmentRepository.count());

        // Recent activity (last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        stats.put("recentFileUploads", fileRepository.countFilesUploadedSince(last24Hours));
        stats.put("recentMessages", messageRepository.countMessagesSince(last24Hours));
        stats.put("recentNotifications", notificationRepository.countNotificationsSince(last24Hours));

        return stats;
    }

    public Map<String, Object> getUserActivityStatistics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        Map<String, Object> stats = new HashMap<>();

        // Activity by action type
        List<Object[]> actionStats = activityLogRepository.getActionStatisticsSince(startDate);
        Map<String, Long> actionCounts = actionStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
        stats.put("actionStatistics", actionCounts);

        // Total activities in period
        stats.put("totalActivities", actionStats.stream().mapToLong(row -> (Long) row[1]).sum());

        // Login statistics
        stats.put("totalLogins", activityLogRepository.countByActionAndCreatedAtAfter("LOGIN", startDate));
        stats.put("uniqueActiveUsers", userRepository.countActiveUsers());

        // File activity
        stats.put("fileUploads", activityLogRepository.countByActionAndCreatedAtAfter("FILE_UPLOAD", startDate));
        stats.put("fileDownloads", activityLogRepository.countByActionAndCreatedAtAfter("FILE_DOWNLOAD", startDate));

        // Message activity
        stats.put("messagesSent", activityLogRepository.countByActionAndCreatedAtAfter("MESSAGE_SEND", startDate));

        // Department activity
        stats.put("departmentCreated",
                activityLogRepository.countByActionAndCreatedAtAfter("DEPARTMENT_CREATE", startDate));
        stats.put("departmentUpdated",
                activityLogRepository.countByActionAndCreatedAtAfter("DEPARTMENT_UPDATE", startDate));

        return stats;
    }

    public Map<String, Object> getFileStatistics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        Map<String, Object> stats = new HashMap<>();

        // File upload trends
        stats.put("filesUploadedInPeriod", fileRepository.countFilesUploadedSince(startDate));
        stats.put("totalFiles", fileRepository.count());

        // Storage usage
        Long totalSize = fileRepository.getTotalFileSize();
        stats.put("totalStorageUsed", totalSize != null ? totalSize : 0L);
        stats.put("totalStorageUsedFormatted", formatFileSize(totalSize != null ? totalSize : 0L));

        // File activity statistics
        stats.put("fileDownloads", activityLogRepository.countByActionAndCreatedAtAfter("FILE_DOWNLOAD", startDate));
        stats.put("fileShares", activityLogRepository.countByActionAndCreatedAtAfter("FILE_SHARE", startDate));
        stats.put("fileDeletes", activityLogRepository.countByActionAndCreatedAtAfter("FILE_DELETE", startDate));

        // Public vs Private files
        stats.put("publicFiles", fileRepository.countByIsPublic(true));
        stats.put("privateFiles", fileRepository.countByIsPublic(false));

        // Average file size
        Long avgSize = fileRepository.getAverageFileSize();
        stats.put("averageFileSize", avgSize != null ? avgSize : 0L);
        stats.put("averageFileSizeFormatted", formatFileSize(avgSize != null ? avgSize : 0L));

        return stats;
    }

    public Map<String, Object> getMessageStatistics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        Map<String, Object> stats = new HashMap<>();

        // Message trends
        stats.put("messagesInPeriod", messageRepository.countMessagesSince(startDate));
        stats.put("totalMessages", messageRepository.count());

        // Message type breakdown
        stats.put("directMessages", messageRepository.countByMessageType("DIRECT"));
        stats.put("departmentMessages", messageRepository.countByMessageType("DEPARTMENT"));
        stats.put("announcements", messageRepository.countByMessageType("ANNOUNCEMENT"));

        // Recent message activity
        stats.put("unreadMessages", messageRepository.countUnreadMessages());
        stats.put("messagesLastWeek", messageRepository.countMessagesSince(LocalDateTime.now().minusWeeks(1)));
        stats.put("messagesLastMonth", messageRepository.countMessagesSince(LocalDateTime.now().minusMonths(1)));

        return stats;
    }

    public Map<String, Object> getSystemHealthStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Database health indicators
        stats.put("totalUsers", userRepository.count());
        stats.put("totalFiles", fileRepository.count());
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalNotifications", notificationRepository.count());
        stats.put("totalActivityLogs", activityLogRepository.count());

        // Recent activity as health indicator
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        stats.put("recentActivity", activityLogRepository.countByActionAndCreatedAtAfter("LOGIN", lastHour));

        // Storage health
        Long totalFileSize = fileRepository.getTotalFileSize();
        stats.put("storageUsed", totalFileSize != null ? totalFileSize : 0L);

        return stats;
    }

    public List<Map<String, Object>> getActivityTrends(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        // This is a simplified version - in a real application, you'd want to group by
        // date
        List<Object[]> actionStats = activityLogRepository.getActionStatisticsSince(startDate);

        return actionStats.stream()
                .map(row -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("action", row[0]);
                    trend.put("count", row[1]);
                    return trend;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getUserEngagementMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);

        // Active users in different time periods
        metrics.put("totalUsers", userRepository.countActiveUsers());

        // Login activity
        metrics.put("weeklyLogins", activityLogRepository.countByActionAndCreatedAtAfter("LOGIN", lastWeek));
        metrics.put("monthlyLogins", activityLogRepository.countByActionAndCreatedAtAfter("LOGIN", lastMonth));

        // File activity
        metrics.put("weeklyFileUploads", fileRepository.countFilesUploadedSince(lastWeek));
        metrics.put("monthlyFileUploads", fileRepository.countFilesUploadedSince(lastMonth));

        // Message activity
        metrics.put("weeklyMessages", messageRepository.countMessagesSince(lastWeek));
        metrics.put("monthlyMessages", messageRepository.countMessagesSince(lastMonth));

        return metrics;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
