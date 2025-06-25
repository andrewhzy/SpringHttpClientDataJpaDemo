package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for Task entities
 * Includes methods for background processing support
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find tasks by user ID and task type with cursor-based pagination
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND (:taskType IS NULL OR t.taskType = :taskType) " +
           "AND (:cursor IS NULL OR t.id < :cursor) ORDER BY t.id DESC")
    List<Task> findByUserIdAndTaskTypeWithCursor(@Param("userId") String userId, 
                                                  @Param("taskType") Task.TaskType taskType,
                                                  @Param("cursor") Long cursor, 
                                                  Pageable pageable);
    
    /**
     * Count tasks by user ID and task type
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.userId = :userId AND (:taskType IS NULL OR t.taskType = :taskType)")
    long countByUserIdAndTaskType(@Param("userId") String userId, @Param("taskType") Task.TaskType taskType);
    
    /**
     * Find tasks by task type and status with pagination (for background processing)
     * Used by ChatEvaluationBackgroundProcessor to get queued tasks in FIFO order
     */
    @Query("SELECT t FROM Task t WHERE t.taskType = :taskType AND t.taskStatus = :taskStatus ORDER BY t.createdAt ASC")
    List<Task> findByTaskTypeAndTaskStatus(@Param("taskType") Task.TaskType taskType, 
                                          @Param("taskStatus") Task.TaskStatus taskStatus, 
                                          Pageable pageable);
    
    /**
     * Atomically update task status from queueing to processing
     * Prevents race conditions in background processing
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Task t SET t.taskStatus = :newStatus, t.startedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :taskId AND t.taskStatus = :currentStatus")
    int updateTaskStatusFromQueueingToProcessing(@Param("taskId") Long taskId, 
                                                @Param("currentStatus") Task.TaskStatus currentStatus,
                                                @Param("newStatus") Task.TaskStatus newStatus);
    
    /**
     * Update task progress with processed row count
     * Used during background processing to track progress
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Task t SET t.processedRows = :processedRows, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :taskId")
    int updateTaskProgress(@Param("taskId") Long taskId, @Param("processedRows") int processedRows);
} 