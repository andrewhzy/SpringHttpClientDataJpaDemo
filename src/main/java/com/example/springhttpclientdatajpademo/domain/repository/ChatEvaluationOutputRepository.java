package com.example.springhttpclientdatajpademo.domain.repository;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for ChatEvaluationOutput
 */
public interface ChatEvaluationOutputRepository {
    
    /**
     * Save output data
     */
    ChatEvaluationOutput save(ChatEvaluationOutput output);
    

    
    /**
     * Find output by ID
     */
    Optional<ChatEvaluationOutput> findById(UUID id);
    
    /**
     * Find all outputs for a task
     */
    List<ChatEvaluationOutput> findByTask(Task task);
    
    /**
     * Find outputs by task ID
     */
    List<ChatEvaluationOutput> findByTaskId(UUID taskId);
    
    /**
     * Count outputs for a task
     */
    long countByTask(Task task);
    
    /**
     * Delete output
     */
    void delete(ChatEvaluationOutput output);
    
    /**
     * Delete all outputs for a task
     */
    void deleteByTask(Task task);
} 