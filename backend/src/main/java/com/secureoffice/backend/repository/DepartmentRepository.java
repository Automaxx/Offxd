package com.secureoffice.backend.repository;

import com.secureoffice.backend.model.Department;
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
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);
    
    Boolean existsByName(String name);
    
    List<Department> findByManager(User manager);
    
    @Query("SELECT d FROM Department d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Department> findBySearch(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT d FROM Department d JOIN d.users u WHERE u.id = :userId")
    List<Department> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ud) FROM Department d JOIN d.users ud WHERE d.id = :departmentId")
    long countUsersByDepartmentId(@Param("departmentId") Long departmentId);
}
