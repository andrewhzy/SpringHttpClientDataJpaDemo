package com.example.springhttpclientdatajpademo.domain.repository;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for ChatEvaluationInput
 */
public interface ChatEvaluationInputRepository {
    
        /**
     * Save input data
     */
    ChatEvaluationInput save(ChatEvaluationInput input);
    
    /**
     * Find input by ID
     */
    Optional<ChatEvaluationInput> findById(UUID id);
    
    /**
     * Find all inputs for a task
     */
    List<ChatEvaluationInput> findByTask(Task task);
    
    /**
     * Find inputs by task ID
     */
    List<ChatEvaluationInput> findByTaskId(UUID taskId);
    
    /**
     * Count inputs for a task
     */
    long countByTask(Task task);
    
    /**
     * Delete input
     */
    void delete(ChatEvaluationInput input);
    
    /**
     * Delete all inputs for a task
     */
    void deleteByTask(Task task);
} 