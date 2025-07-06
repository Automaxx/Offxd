package com.secureoffice.backend.controller;

import com.secureoffice.backend.dto.message.CreateMessageRequest;
import com.secureoffice.backend.dto.message.MessageResponse;
import com.secureoffice.backend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody CreateMessageRequest request) {
        MessageResponse message = messageService.sendMessage(request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/direct")
    public ResponseEntity<Page<MessageResponse>> getDirectMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MessageResponse> messages = messageService.getDirectMessages(pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/department")
    public ResponseEntity<Page<MessageResponse>> getDepartmentMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MessageResponse> messages = messageService.getDepartmentMessages(pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/announcements")
    public ResponseEntity<Page<MessageResponse>> getAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MessageResponse> messages = messageService.getAnnouncements(pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MessageResponse>> searchMessages(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MessageResponse> messages = messageService.searchMessages(search, pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages() {
        List<MessageResponse> messages = messageService.getUnreadMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount() {
        long count = messageService.getUnreadMessageCount();
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markMessageAsRead(@PathVariable Long id) {
        messageService.markMessageAsRead(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Message marked as read");
        return ResponseEntity.ok(response);
    }
}
