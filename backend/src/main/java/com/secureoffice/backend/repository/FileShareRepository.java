package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.File;
import com.secureoffice.backend.model.FileShare;
import com.secureoffice.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    List<FileShare> findByFile(File file);
    
    List<FileShare> findByUser(User user);
    
    List<FileShare> findByFileAndUser(File file, User user);
    
    Optional<FileShare> findByFileAndUserAndPermissionType(File file, User user, FileShare.PermissionType permissionType);
    
    @Query("SELECT fs FROM FileShare fs WHERE fs.file.id = :fileId")
    List<FileShare> findByFileId(@Param("fileId") Long fileId);
    
    @Query("SELECT fs FROM FileShare fs WHERE fs.user.id = :userId")
    List<FileShare> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(fs) FROM FileShare fs WHERE fs.file = :file")
    long countByFile(@Param("file") File file);
    
    void deleteByFileAndUser(File file, User user);
    
    void deleteByFile(File file);
}
