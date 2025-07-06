package com.secureoffice.backend.controller;

import com.secureoffice.backend.dto.department.CreateDepartmentRequest;
import com.secureoffice.backend.dto.department.DepartmentResponse;
import com.secureoffice.backend.dto.user.UserResponse;
import com.secureoffice.backend.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse department = departmentService.createDepartment(request);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    public ResponseEntity<Page<DepartmentResponse>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DepartmentResponse> departments;
        
        if (search != null && !search.trim().isEmpty()) {
            departments = departmentService.searchDepartments(search.trim(), pageable);
        } else {
            departments = departmentService.getAllDepartments(pageable);
        }
        
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/my")
    public ResponseEntity<List<DepartmentResponse>> getUserDepartments() {
        List<DepartmentResponse> departments = departmentService.getUserDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id, 
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Department deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{departmentId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DepartmentResponse> addUserToDepartment(
            @PathVariable Long departmentId, 
            @PathVariable Long userId) {
        DepartmentResponse department = departmentService.addUserToDepartment(departmentId, userId);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{departmentId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DepartmentResponse> removeUserFromDepartment(
            @PathVariable Long departmentId, 
            @PathVariable Long userId) {
        DepartmentResponse department = departmentService.removeUserFromDepartment(departmentId, userId);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserResponse>> getDepartmentUsers(@PathVariable Long id) {
        List<UserResponse> users = departmentService.getDepartmentUsers(id);
        return ResponseEntity.ok(users);
    }
}
