package com.secureoffice.backend.service;

import com.secureoffice.backend.dto.user.ChangePasswordRequest;
import com.secureoffice.backend.dto.user.UpdateUserRequest;
import com.secureoffice.backend.dto.user.UserResponse;
import com.secureoffice.backend.exception.ResourceNotFoundException;
import com.secureoffice.backend.model.User;
import com.secureoffice.backend.repository.UserRepository;
import com.secureoffice.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivityLogService activityLogService;

    public UserResponse getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));
        
        return new UserResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return new UserResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserResponse::new);
    }

    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.findActiveUsersBySearch(search, pageable);
        return users.map(UserResponse::new);
    }

    public Page<UserResponse> getUsersByRole(User.Role role, Pageable pageable) {
        Page<User> users = userRepository.findActiveUsersByRole(role, pageable);
        return users.map(UserResponse::new);
    }

    public List<UserResponse> getActiveUsers() {
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream().map(UserResponse::new).collect(Collectors.toList());
    }

    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));

        // Check if email is already taken by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use by another user");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);
        
        // Log activity
        activityLogService.logActivity(updatedUser, "PROFILE_UPDATE", "USER", updatedUser.getId());

        return new UserResponse(updatedUser);
    }

    public void changePassword(ChangePasswordRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Log activity
        activityLogService.logActivity(user, "PASSWORD_CHANGE", "USER", user.getId());
    }

    // Admin methods
    public UserResponse updateUserRole(Long userId, User.Role role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User.Role oldRole = user.getRole();
        user.setRole(role);
        User updatedUser = userRepository.save(user);

        // Log activity
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User adminUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (adminUser != null) {
            activityLogService.logActivity(adminUser, "USER_ROLE_UPDATE", "USER", userId, 
                java.util.Map.of("oldRole", oldRole.name(), "newRole", role.name()));
        }

        return new UserResponse(updatedUser);
    }

    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean oldStatus = user.getIsActive();
        user.setIsActive(!oldStatus);
        User updatedUser = userRepository.save(user);

        // Log activity
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User adminUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (adminUser != null) {
            String action = updatedUser.getIsActive() ? "USER_ACTIVATE" : "USER_DEACTIVATE";
            activityLogService.logActivity(adminUser, action, "USER", userId);
        }

        return new UserResponse(updatedUser);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Log activity before deletion
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User adminUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (adminUser != null) {
            activityLogService.logActivity(adminUser, "USER_DELETE", "USER", userId, 
                java.util.Map.of("deletedUsername", user.getUsername()));
        }

        userRepository.delete(user);
    }

    // Statistics methods
    public long getTotalActiveUsers() {
        return userRepository.countActiveUsers();
    }

    public long getUserCountByRole(User.Role role) {
        return userRepository.countActiveUsersByRole(role);
    }
}
