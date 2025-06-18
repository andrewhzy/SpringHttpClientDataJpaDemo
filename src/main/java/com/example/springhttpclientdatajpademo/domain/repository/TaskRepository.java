package com.example.springhttpclientdatajpademo.domain.repository;

import com.example.springhttpclientdatajpademo.domain.model.Task;
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
 * Repository for Task aggregate root
 * Combines domain interface with JPA implementation
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    // Basic domain queries
    Optional<Task> findByIdAndUserId(UUID id, String userId);
    List<Task> findByUserId(String userId);
    List<Task> findByUserIdAndTaskStatus(String userId, Task.TaskStatus status);
    List<Task> findByUploadBatchIdOrderByCreatedAtDesc(UUID uploadBatchId);
    long countByUserId(String userId);
    long countByUserIdAndTaskStatus(String userId, Task.TaskStatus status);
    
    // Pagination queries
    Page<Task> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<Task> findByUserIdAndTaskStatusOrderByCreatedAtDesc(String userId, Task.TaskStatus taskStatus, Pageable pageable);
    Page<Task> findByUserIdAndTaskTypeOrderByCreatedAtDesc(String userId, Task.TaskType taskType, Pageable pageable);
    Page<Task> findByUserIdAndFilenameContainingIgnoreCaseOrderByCreatedAtDesc(String userId, String filename, Pageable pageable);
    Page<Task> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(String userId, LocalDateTime createdAfter, Pageable pageable);
    Page<Task> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(String userId, LocalDateTime createdBefore, Pageable pageable);
    
    // Business-specific queries
    @Query("SELECT t FROM Task t WHERE t.taskStatus = 'QUEUEING' AND t.rowCount > 0 ORDER BY t.createdAt ASC")
    List<Task> findTasksReadyForProcessing();
    
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.userId = :userId " +
           "AND t.taskStatus IN ('QUEUEING', 'PROCESSING')")
    Optional<Task> findCancellableTask(@Param("taskId") UUID taskId, @Param("userId") String userId);
    
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.userId = :userId " +
           "AND t.taskStatus != 'PROCESSING'")
    Optional<Task> findDeletableTask(@Param("taskId") UUID taskId, @Param("userId") String userId);
} 