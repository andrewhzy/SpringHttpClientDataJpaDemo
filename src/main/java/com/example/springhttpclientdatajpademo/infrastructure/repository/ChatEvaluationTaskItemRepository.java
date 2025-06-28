package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskItem;
import com.example.springhttpclientdatajpademo.domain.task.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatEvaluationTaskItem entities
 * Simplified for POST /rest/api/v1/tasks endpoint
 * 
 * Following Effective Java Item 64: Refer to objects by their interfaces
 */
@Repository
public interface ChatEvaluationTaskItemRepository extends JpaRepository<ChatEvaluationTaskItem, Long> {
    
    // JpaRepository provides saveAll() method needed for batch saving inputs
    // Additional methods can be added when other endpoints are implemented
    
    /**
     * Find all task items for a task, ordered by ID for consistent processing
     * Used by background processor to get task items in deterministic order
     * 
     * @param task the task to find task items for
     * @return list of task items ordered by ID
     */
    List<ChatEvaluationTaskItem> findByTaskOrderByIdAsc(Task task);
} 