package com.example.springhttpclientdatajpademo.domain.repository;

import com.example.springhttpclientdatajpademo.domain.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Task aggregate
 * Clean of infrastructure concerns
 */
public interface TaskRepository {
    
    /**
     * Save a task (create or update)
     */
    Task save(Task task);
    
    /**
     * Find task by ID
     */
    Optional<Task> findById(UUID id);
    
    /**
     * Find task by ID and user ID (for ownership validation)
     */
    Optional<Task> findByIdAndUserId(UUID id, String userId);
    
    /**
     * Find all tasks for a user
     */
    List<Task> findByUserId(String userId);
    
    /**
     * Find tasks by user and status
     */
    List<Task> findByUserIdAndStatus(String userId, Task.TaskStatus status);
    
    /**
     * Find tasks by upload batch ID
     */
    List<Task> findByUploadBatchId(UUID uploadBatchId);
    
    /**
     * Find tasks ready for processing (queued status)
     */
    List<Task> findTasksReadyForProcessing();
    
    /**
     * Find tasks that can be cancelled
     */
    Optional<Task> findCancellableTask(UUID taskId, String userId);
    
    /**
     * Find tasks that can be deleted
     */
    Optional<Task> findDeletableTask(UUID taskId, String userId);
    
    /**
     * Count tasks by user ID
     */
    long countByUserId(String userId);
    
    /**
     * Count tasks by user ID and status
     */
    long countByUserIdAndStatus(String userId, Task.TaskStatus status);
    
    /**
     * Delete a task
     */
    void delete(Task task);
    
    /**
     * Check if task exists
     */
    boolean existsById(UUID id);
} 