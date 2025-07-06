package com.secureoffice.backend.controller;

import com.secureoffice.backend.dto.file.FileResponse;
import com.secureoffice.backend.dto.file.FileShareRequest;
import com.secureoffice.backend.dto.file.FileShareResponse;
import com.secureoffice.backend.service.FileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderPath", defaultValue = "/") String folderPath) {
        
        FileResponse fileResponse = fileService.uploadFile(file, folderPath);
        return ResponseEntity.ok(fileResponse);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        Resource resource = fileService.downloadFile(fileId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<Page<FileResponse>> getFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String folderPath) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FileResponse> files;
        
        if (search != null && !search.trim().isEmpty()) {
            files = fileService.searchFiles(search.trim(), pageable);
        } else if (folderPath != null) {
            files = fileService.getFilesByFolder(folderPath, pageable);
        } else {
            files = fileService.getAccessibleFiles(pageable);
        }
        
        return ResponseEntity.ok(files);
    }

    @GetMapping("/folders")
    public ResponseEntity<List<String>> getUserFolders() {
        List<String> folders = fileService.getUserFolders();
        return ResponseEntity.ok(folders);
    }

    @PostMapping("/share")
    public ResponseEntity<List<FileShareResponse>> shareFile(@Valid @RequestBody FileShareRequest request) {
        List<FileShareResponse> shares = fileService.shareFile(request);
        return ResponseEntity.ok(shares);
    }

    @GetMapping("/{fileId}/shares")
    public ResponseEntity<List<FileShareResponse>> getFileShares(@PathVariable Long fileId) {
        List<FileShareResponse> shares = fileService.getFileShares(fileId);
        return ResponseEntity.ok(shares);
    }

    @PutMapping("/{fileId}/visibility")
    public ResponseEntity<FileResponse> toggleFileVisibility(@PathVariable Long fileId) {
        FileResponse file = fileService.toggleFileVisibility(fileId);
        return ResponseEntity.ok(file);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFileSize", fileService.getTotalFileSize());
        
        return ResponseEntity.ok(stats);
    }
}
