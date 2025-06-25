package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.task.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatEvaluationInput entities
 * Simplified for POST /rest/api/v1/tasks endpoint
 * 
 * Following Effective Java Item 64: Refer to objects by their interfaces
 */
@Repository
public interface ChatEvaluationInputRepository extends JpaRepository<ChatEvaluationInput, Long> {
    
    // JpaRepository provides saveAll() method needed for batch saving inputs
    // Additional methods can be added when other endpoints are implemented
    
    /**
     * Find all inputs for a task, ordered by ID for consistent processing
     * Used by background processor to get inputs in deterministic order
     * 
     * @param task the task to find inputs for
     * @return list of inputs ordered by ID
     */
    List<ChatEvaluationInput> findByTaskOrderByIdAsc(Task task);
} 