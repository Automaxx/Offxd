package com.secureoffice.backend.service;

import com.secureoffice.backend.dto.department.CreateDepartmentRequest;
import com.secureoffice.backend.dto.department.DepartmentResponse;
import com.secureoffice.backend.dto.user.UserResponse;
import com.secureoffice.backend.exception.ResourceNotFoundException;
import com.secureoffice.backend.model.Department;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.DepartmentRepository;
import com.secureoffice.backend.repository.UserRepository;
import com.secureoffice.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        // Check if department name already exists
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = new Department(request.getName(), request.getDescription());

        // Set manager if provided
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            department.setManager(manager);
        }

        Department savedDepartment = departmentRepository.save(department);

        // Log activity
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId()).orElse(null);
        if (currentUser != null) {
            activityLogService.logActivity(currentUser, "DEPARTMENT_CREATE", "DEPARTMENT", savedDepartment.getId());
        }

        return new DepartmentResponse(savedDepartment);
    }

    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(pageable);
        return departments.map(DepartmentResponse::new);
    }

    public Page<DepartmentResponse> searchDepartments(String search, Pageable pageable) {
        Page<Department> departments = departmentRepository.findBySearch(search, pageable);
        return departments.map(DepartmentResponse::new);
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        return new DepartmentResponse(department);
    }

    public List<DepartmentResponse> getUserDepartments() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Department> departments = departmentRepository.findByUserId(userPrincipal.getId());
        return departments.stream().map(DepartmentResponse::new).collect(Collectors.toList());
    }

    public DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check if new name conflicts with existing department
        if (!department.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with name '" + request.getName() + "' already exists");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        // Update manager if provided
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        Department updatedDepartment = departmentRepository.save(department);

        // Log activity
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId()).orElse(null);
        if (currentUser != null) {
            activityLogService.logActivity(currentUser, "DEPARTMENT_UPDATE", "DEPARTMENT", id);
        }

        return new DepartmentResponse(updatedDepartment);
    }

    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Log activity before deletion
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId()).orElse(null);
        if (currentUser != null) {
            activityLogService.logActivity(currentUser, "DEPARTMENT_DELETE", "DEPARTMENT", id, 
                java.util.Map.of("departmentName", department.getName()));
        }

        departmentRepository.delete(department);
    }

    public DepartmentResponse addUserToDepartment(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        department.addUser(user);
        Department updatedDepartment = departmentRepository.save(department);

        // Send notification to user
        notificationService.createNotification(
            user,
            "Added to Department",
            "You have been added to the " + department.getName() + " department",
            com.secureoffice.backend.model.Notification.NotificationType.INFO,
            "DEPARTMENT",
            departmentId
        );

        // Log activity
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId()).orElse(null);
        if (currentUser != null) {
            activityLogService.logActivity(currentUser, "DEPARTMENT_ADD_USER", "DEPARTMENT", departmentId, 
                java.util.Map.of("addedUserId", userId, "addedUsername", user.getUsername()));
        }

        return new DepartmentResponse(updatedDepartment);
    }

    public DepartmentResponse removeUserFromDepartment(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        department.removeUser(user);
        Department updatedDepartment = departmentRepository.save(department);

        // Send notification to user
        notificationService.createNotification(
            user,
            "Removed from Department",
            "You have been removed from the " + department.getName() + " department",
            com.secureoffice.backend.model.Notification.NotificationType.WARNING,
            "DEPARTMENT",
            departmentId
        );

        // Log activity
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId()).orElse(null);
        if (currentUser != null) {
            activityLogService.logActivity(currentUser, "DEPARTMENT_REMOVE_USER", "DEPARTMENT", departmentId, 
                java.util.Map.of("removedUserId", userId, "removedUsername", user.getUsername()));
        }

        return new DepartmentResponse(updatedDepartment);
    }

    public List<UserResponse> getDepartmentUsers(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        return department.getUsers().stream()
            .map(UserResponse::new)
            .collect(Collectors.toList());
    }
}
