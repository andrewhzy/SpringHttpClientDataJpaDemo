package com.example.springhttpclientdatajpademo.repository;

import com.example.springhttpclientdatajpademo.entity.Task;
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
 * Repository interface for Task entity operations
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    /**
     * Find all tasks for a specific user with pagination
     */
    Page<Task> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find tasks by user and status
     */
    Page<Task> findByUserIdAndTaskStatusOrderByCreatedAtDesc(
            String userId, Task.TaskStatus taskStatus, Pageable pageable);
    
    /**
     * Find tasks by user and task type
     */
    Page<Task> findByUserIdAndTaskTypeOrderByCreatedAtDesc(
            String userId, Task.TaskType taskType, Pageable pageable);
    
    /**
     * Find tasks by upload batch ID
     */
    List<Task> findByUploadBatchIdOrderByCreatedAtDesc(UUID uploadBatchId);
    
    /**
     * Find tasks by user and filename containing
     */
    Page<Task> findByUserIdAndFilenameContainingIgnoreCaseOrderByCreatedAtDesc(
            String userId, String filename, Pageable pageable);
    
    /**
     * Find tasks created after specific date
     */
    Page<Task> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, LocalDateTime createdAfter, Pageable pageable);
    
    /**
     * Find tasks created before specific date
     */
    Page<Task> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String userId, LocalDateTime createdBefore, Pageable pageable);
    
    /**
     * Find a task by ID and user ID (for ownership validation)
     */
    Optional<Task> findByIdAndUserId(UUID id, String userId);
    
    /**
     * Count tasks by user ID
     */
    long countByUserId(String userId);
    
    /**
     * Count tasks by user ID and status
     */
    long countByUserIdAndTaskStatus(String userId, Task.TaskStatus taskStatus);
    
    /**
     * Find tasks queued for processing
     */
    @Query("SELECT t FROM Task t WHERE t.taskStatus = :status ORDER BY t.createdAt ASC")
    List<Task> findTasksForProcessing(@Param("status") Task.TaskStatus status);
    
    /**
     * Find tasks that can be cancelled (queueing or processing status)
     */
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.userId = :userId " +
           "AND t.taskStatus IN ('QUEUEING', 'PROCESSING')")
    Optional<Task> findCancellableTask(@Param("taskId") UUID taskId, @Param("userId") String userId);
    
    /**
     * Find tasks that can be deleted (not in processing status)
     */
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.userId = :userId " +
           "AND t.taskStatus != 'PROCESSING'")
    Optional<Task> findDeletableTask(@Param("taskId") UUID taskId, @Param("userId") String userId);
} 