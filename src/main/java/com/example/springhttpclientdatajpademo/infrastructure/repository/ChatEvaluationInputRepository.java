package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatEvaluationInput entities
 */
@Repository
public interface ChatEvaluationInputRepository extends JpaRepository<ChatEvaluationInput, Long> {
    
    List<ChatEvaluationInput> findByTask(Task task);
    List<ChatEvaluationInput> findByTaskId(Long taskId);
    long countByTask(Task task);
    void deleteByTask(Task task);
} 