package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatEvaluationOutput entities
 */
@Repository
public interface ChatEvaluationOutputRepository extends JpaRepository<ChatEvaluationOutput, Long> {
    
    List<ChatEvaluationOutput> findByTask(Task task);
    List<ChatEvaluationOutput> findByTaskId(Long taskId);
    long countByTask(Task task);
    void deleteByTask(Task task);
} 