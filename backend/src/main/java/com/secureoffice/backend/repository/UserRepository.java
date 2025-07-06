package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Optional<User> findByPasswordResetToken(String token);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByIsActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findActiveUsersBySearch(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Page<User> findActiveUsersByRole(@Param("role") User.Role role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") User.Role role);
}
