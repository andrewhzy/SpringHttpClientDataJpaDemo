package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Task entity
 * Enhanced for cursor-based pagination in GET /rest/api/v1/tasks endpoint
 * 
 * Following Effective Java Item 64: Refer to objects by their interfaces
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // JpaRepository provides save() method needed for task creation
    
    /**
     * Find first page of tasks for a user with task type filter
     * Ordered by created_at DESC, id DESC for consistent pagination
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.taskType = :taskType " +
           "ORDER BY t.createdAt DESC, t.id DESC")
    List<Task> findFirstPageByUserIdAndTaskType(
            @Param("userId") String userId,
            @Param("taskType") Task.TaskType taskType,
            Pageable pageable);
    
    /**
     * Find next page of tasks after cursor for a user with task type filter
     * Cursor is the ID of the last item from previous page
     * Ordered by created_at DESC, id DESC for consistent pagination
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.taskType = :taskType " +
           "AND t.id < :cursor " +
           "ORDER BY t.createdAt DESC, t.id DESC")
    List<Task> findNextPageByUserIdAndTaskTypeAfterCursor(
            @Param("userId") String userId,
            @Param("taskType") Task.TaskType taskType,
            @Param("cursor") Long cursor,
            Pageable pageable);
    
    /**
     * Count total tasks for a user with task type filter
     * Used for total count in response metadata
     */
    long countByUserIdAndTaskType(String userId, Task.TaskType taskType);
} 