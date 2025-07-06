package com.secureoffice.backend.service;

import com.secureoffice.backend.config.FileUploadConfig;
import com.secureoffice.backend.dto.file.FileResponse;
import com.secureoffice.backend.dto.file.FileShareRequest;
import com.secureoffice.backend.dto.file.FileShareResponse;
import com.secureoffice.backend.exception.ResourceNotFoundException;
import com.secureoffice.backend.model.File;
import com.secureoffice.backend.model.FileShare;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.FileRepository;
import com.secureoffice.backend.repository.FileShareRepository;
import com.secureoffice.backend.repository.UserRepository;
import com.secureoffice.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileShareRepository fileShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    private final Path fileStorageLocation;

    public FileService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
        this.fileStorageLocation = Paths.get(fileUploadConfig.getDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public FileResponse uploadFile(MultipartFile file, String folderPath) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file name: " + fileName);
            }

            if (file.getSize() > fileUploadConfig.getMaxSize()) {
                throw new RuntimeException("File size exceeds maximum allowed size");
            }

            // Generate unique filename
            String fileExtension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = fileName.substring(dotIndex);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Ensure folder path starts with /
            if (folderPath == null || folderPath.trim().isEmpty()) {
                folderPath = "/";
            } else if (!folderPath.startsWith("/")) {
                folderPath = "/" + folderPath;
            }

            // Create folder structure
            Path folderLocation = this.fileStorageLocation.resolve(folderPath.substring(1));
            Files.createDirectories(folderLocation);

            // Copy file to the target location
            Path targetLocation = folderLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Get current user
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Save file metadata
            File fileEntity = new File(
                    uniqueFileName,
                    fileName,
                    targetLocation.toString(),
                    file.getSize(),
                    file.getContentType(),
                    folderPath,
                    user);

            File savedFile = fileRepository.save(fileEntity);

            // Log activity
            activityLogService.logFileActivity(user, "FILE_UPLOAD", savedFile.getId(), fileName);

            return new FileResponse(savedFile);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource downloadFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        // Check if user has access to this file
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!hasFileAccess(file, user, FileShare.PermissionType.DOWNLOAD)) {
            throw new RuntimeException("Access denied to download this file");
        }

        try {
            Path filePath = Paths.get(file.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                // Log activity
                activityLogService.logFileActivity(user, "FILE_DOWNLOAD", file.getId(), file.getOriginalFilename());
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + file.getOriginalFilename());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + file.getOriginalFilename());
        }
    }

    public Page<FileResponse> getAccessibleFiles(Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<File> files = fileRepository.findAccessibleFiles(user, pageable);
        return files.map(FileResponse::new);
    }

    public Page<FileResponse> searchFiles(String search, Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<File> files = fileRepository.findAccessibleFilesBySearch(user, search, pageable);
        return files.map(FileResponse::new);
    }

    public Page<FileResponse> getFilesByFolder(String folderPath, Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (folderPath == null || folderPath.trim().isEmpty()) {
            folderPath = "/";
        }

        Page<File> files = fileRepository.findAccessibleFilesByFolder(user, folderPath, pageable);
        return files.map(FileResponse::new);
    }

    public List<String> getUserFolders() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return fileRepository.findDistinctFolderPathsByUser(user);
    }

    public List<FileShareResponse> shareFile(FileShareRequest request) {
        File file = fileRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + request.getFileId()));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if current user owns the file or has edit permission
        if (!file.getUploadedBy().getId().equals(currentUser.getId()) &&
                !hasFileAccess(file, currentUser, FileShare.PermissionType.EDIT)) {
            throw new RuntimeException("Access denied to share this file");
        }

        try {
            FileShare.PermissionType permissionType = FileShare.PermissionType
                    .valueOf(request.getPermissionType().toUpperCase());

            List<FileShare> shares = request.getUserIds().stream().map(userId -> {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                // Remove existing share if any
                fileShareRepository.deleteByFileAndUser(file, user);

                // Create new share
                FileShare share = new FileShare(file, user, permissionType, currentUser);
                FileShare savedShare = fileShareRepository.save(share);

                // Send notification
                notificationService.createNotification(
                        user,
                        "File Shared",
                        currentUser.getFullName() + " shared a file '" + file.getOriginalFilename() + "' with you",
                        com.secureoffice.backend.model.Notification.NotificationType.INFO,
                        "FILE",
                        file.getId());

                return savedShare;
            }).collect(Collectors.toList());

            // Log activity
            activityLogService.logFileActivity(currentUser, "FILE_SHARE", file.getId(), file.getOriginalFilename());

            return shares.stream().map(FileShareResponse::new).collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid permission type: " + request.getPermissionType());
        }
    }

    public List<FileShareResponse> getFileShares(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user owns the file
        if (!file.getUploadedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to view file shares");
        }

        List<FileShare> shares = fileShareRepository.findByFile(file);
        return shares.stream().map(FileShareResponse::new).collect(Collectors.toList());
    }

    public void deleteFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user owns the file
        if (!file.getUploadedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to delete this file");
        }

        try {
            // Delete physical file
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete file shares
            fileShareRepository.deleteByFile(file);

            // Delete file record
            fileRepository.delete(file);

            // Log activity
            activityLogService.logFileActivity(user, "FILE_DELETE", fileId, file.getOriginalFilename());

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    public FileResponse toggleFileVisibility(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user owns the file
        if (!file.getUploadedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to modify this file");
        }

        file.setIsPublic(!file.getIsPublic());
        File updatedFile = fileRepository.save(file);

        // Log activity
        String action = updatedFile.getIsPublic() ? "FILE_MAKE_PUBLIC" : "FILE_MAKE_PRIVATE";
        activityLogService.logFileActivity(user, action, file.getId(), file.getOriginalFilename());

        return new FileResponse(updatedFile);
    }

    // Statistics methods
    public Long getTotalFileSize() {
        return fileRepository.getTotalFileSize();
    }

    public Long getUserFileSize(User user) {
        return fileRepository.getTotalFileSizeByUser(user);
    }

    private boolean hasFileAccess(File file, User user, FileShare.PermissionType requiredPermission) {
        // File owner has full access
        if (file.getUploadedBy().getId().equals(user.getId())) {
            return true;
        }

        // Public files can be viewed and downloaded
        if (file.getIsPublic() && (requiredPermission == FileShare.PermissionType.VIEW ||
                requiredPermission == FileShare.PermissionType.DOWNLOAD)) {
            return true;
        }

        // Check file shares
        List<FileShare> shares = fileShareRepository.findByFileAndUser(file, user);
        return shares.stream().anyMatch(share -> share.getPermissionType() == requiredPermission ||
                (requiredPermission == FileShare.PermissionType.VIEW &&
                        (share.getPermissionType() == FileShare.PermissionType.EDIT ||
                                share.getPermissionType() == FileShare.PermissionType.DOWNLOAD)));
    }
}
