package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for Task entity
 * Enhanced for GET /rest/api/v1/tasks endpoint
 * 
 * Following Effective Java Item 64: Refer to objects by their interfaces
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // JpaRepository provides save() method needed for task creation
    
    /**
     * Find all tasks for a specific user with pagination
     */
    Page<Task> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find tasks by user and status with pagination
     */
    Page<Task> findByUserIdAndTaskStatusOrderByCreatedAtDesc(
            String userId, Task.TaskStatus taskStatus, Pageable pageable);
    
    /**
     * Find tasks by user and task type with pagination
     */
    Page<Task> findByUserIdAndTaskTypeOrderByCreatedAtDesc(
            String userId, Task.TaskType taskType, Pageable pageable);
    
    /**
     * Find tasks by user and upload batch ID with pagination
     */
    Page<Task> findByUserIdAndUploadBatchIdOrderByCreatedAtDesc(
            String userId, Long uploadBatchId, Pageable pageable);
    
    /**
     * Find tasks by user and filename containing (case-insensitive) with pagination
     */
    Page<Task> findByUserIdAndFilenameContainingIgnoreCaseOrderByCreatedAtDesc(
            String userId, String filename, Pageable pageable);
    
    /**
     * Find tasks by user created after a specific date with pagination
     */
    Page<Task> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, LocalDateTime createdAfter, Pageable pageable);
    
    /**
     * Find tasks by user created before a specific date with pagination
     */
    Page<Task> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String userId, LocalDateTime createdBefore, Pageable pageable);
    
    /**
     * Complex query for multiple filters
     * Using @Query for complex filtering scenarios
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId " +
           "AND (:status IS NULL OR t.taskStatus = :status) " +
           "AND (:taskType IS NULL OR t.taskType = :taskType) " +
           "AND (:uploadBatchId IS NULL OR t.uploadBatchId = :uploadBatchId) " +
           "AND (:filename IS NULL OR LOWER(t.filename) LIKE LOWER(CONCAT('%', :filename, '%'))) " +
           "AND (:createdAfter IS NULL OR t.createdAt >= :createdAfter) " +
           "AND (:createdBefore IS NULL OR t.createdAt <= :createdBefore) " +
           "ORDER BY t.createdAt DESC")
    Page<Task> findTasksWithFilters(
            @Param("userId") String userId,
            @Param("status") Task.TaskStatus status,
            @Param("taskType") Task.TaskType taskType,
            @Param("uploadBatchId") Long uploadBatchId,
            @Param("filename") String filename,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);
} 