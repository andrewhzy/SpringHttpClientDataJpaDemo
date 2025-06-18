package com.example.springhttpclientdatajpademo.domain.repository;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ChatEvaluationInput entities
 */
@Repository
public interface ChatEvaluationInputRepository extends JpaRepository<ChatEvaluationInput, UUID> {
    
    List<ChatEvaluationInput> findByTask(Task task);
    List<ChatEvaluationInput> findByTaskId(UUID taskId);
    long countByTask(Task task);
    void deleteByTask(Task task);
} 