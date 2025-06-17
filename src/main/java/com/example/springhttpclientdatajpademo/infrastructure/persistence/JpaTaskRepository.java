package com.example.springhttpclientdatajpademo.infrastructure.persistence;

import com.example.springhttpclientdatajpademo.domain.model.Task;
import com.example.springhttpclientdatajpademo.domain.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of TaskRepository
 * Infrastructure layer repository implementation
 */
@Repository
public interface JpaTaskRepository extends JpaRepository<Task, UUID>, TaskRepository {
    
    // Spring Data JPA specific methods for pagination and complex queries
    
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
     * Find tasks by user and filename containing with pagination
     */
    Page<Task> findByUserIdAndFilenameContainingIgnoreCaseOrderByCreatedAtDesc(
            String userId, String filename, Pageable pageable);
    
    /**
     * Find tasks created after specific date with pagination
     */
    Page<Task> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, LocalDateTime createdAfter, Pageable pageable);
    
    /**
     * Find tasks created before specific date with pagination
     */
    Page<Task> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String userId, LocalDateTime createdBefore, Pageable pageable);
    
    // Domain repository interface implementations (Spring Data JPA provides these automatically)
    
    @Override
    default List<Task> findByUserId(String userId) {
        return findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
    }
    
    @Override
    default List<Task> findByUserIdAndStatus(String userId, Task.TaskStatus status) {
        return findByUserIdAndTaskStatusOrderByCreatedAtDesc(userId, status, Pageable.unpaged()).getContent();
    }
    
    List<Task> findByUploadBatchIdOrderByCreatedAtDesc(UUID uploadBatchId);
    
    @Override
    default List<Task> findByUploadBatchId(UUID uploadBatchId) {
        return findByUploadBatchIdOrderByCreatedAtDesc(uploadBatchId);
    }
    
    @Override
    @Query("SELECT t FROM Task t WHERE t.taskStatus = 'QUEUEING' AND t.rowCount > 0 ORDER BY t.createdAt ASC")
    List<Task> findTasksReadyForProcessing();
    
    @Override
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.userId = :userId " +
           "AND t.taskStatus IN ('QUEUEING', 'PROCESSING')")
    Optional<Task> findCancellableTask(@Param("taskId") UUID taskId, @Param("userId") String userId);
    
    @Override
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.userId = :userId " +
           "AND t.taskStatus != 'PROCESSING'")
    Optional<Task> findDeletableTask(@Param("taskId") UUID taskId, @Param("userId") String userId);
    
    @Override
    long countByUserId(String userId);
    
    long countByUserIdAndTaskStatus(String userId, Task.TaskStatus taskStatus);
    
    @Override
    default long countByUserIdAndStatus(String userId, Task.TaskStatus status) {
        return countByUserIdAndTaskStatus(userId, status);
    }
} 