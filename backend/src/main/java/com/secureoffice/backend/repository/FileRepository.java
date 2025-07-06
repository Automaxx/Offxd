package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.File;
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
public interface FileRepository extends JpaRepository<File, Long> {

       List<File> findByUploadedBy(User uploadedBy);

       List<File> findByFolderPath(String folderPath);

       List<File> findByUploadedByAndFolderPath(User uploadedBy, String folderPath);

       @Query("SELECT f FROM File f WHERE f.isPublic = true OR f.uploadedBy = :user OR " +
                     "EXISTS (SELECT fs FROM FileShare fs WHERE fs.file = f AND fs.user = :user)")
       Page<File> findAccessibleFiles(@Param("user") User user, Pageable pageable);

       @Query("SELECT f FROM File f WHERE (f.isPublic = true OR f.uploadedBy = :user OR " +
                     "EXISTS (SELECT fs FROM FileShare fs WHERE fs.file = f AND fs.user = :user)) AND " +
                     "(LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(f.mimeType) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<File> findAccessibleFilesBySearch(@Param("user") User user, @Param("search") String search,
                     Pageable pageable);

       @Query("SELECT f FROM File f WHERE (f.isPublic = true OR f.uploadedBy = :user OR " +
                     "EXISTS (SELECT fs FROM FileShare fs WHERE fs.file = f AND fs.user = :user)) AND " +
                     "f.folderPath = :folderPath")
       Page<File> findAccessibleFilesByFolder(@Param("user") User user, @Param("folderPath") String folderPath,
                     Pageable pageable);

       @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.uploadedBy = :user")
       Long getTotalFileSizeByUser(@Param("user") User user);

       @Query("SELECT COUNT(f) FROM File f WHERE f.createdAt >= :startDate")
       long countFilesUploadedSince(@Param("startDate") LocalDateTime startDate);

       @Query("SELECT SUM(f.fileSize) FROM File f")
       Long getTotalFileSize();

       @Query("SELECT DISTINCT f.folderPath FROM File f WHERE f.uploadedBy = :user ORDER BY f.folderPath")
       List<String> findDistinctFolderPathsByUser(@Param("user") User user);

       // Count files by visibility
       long countByIsPublic(boolean isPublic);

       // Get average file size
       @Query("SELECT AVG(f.fileSize) FROM File f")
       Long getAverageFileSize();

       // Get file type statistics
       @Query("SELECT f.mimeType, COUNT(f) FROM File f GROUP BY f.mimeType ORDER BY COUNT(f) DESC")
       List<Object[]> getFileTypeStatistics();

       // Get largest files
       @Query("SELECT f FROM File f ORDER BY f.fileSize DESC")
       Page<File> findLargestFiles(Pageable pageable);

       // Get recent files
       @Query("SELECT f FROM File f WHERE f.createdAt >= :startDate ORDER BY f.createdAt DESC")
       Page<File> findRecentFiles(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}
