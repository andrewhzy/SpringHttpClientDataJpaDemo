package com.example.springhttpclientdatajpademo.domain.repository;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ChatEvaluationOutput entities
 */
@Repository
public interface ChatEvaluationOutputRepository extends JpaRepository<ChatEvaluationOutput, UUID> {
    
    List<ChatEvaluationOutput> findByTask(Task task);
    List<ChatEvaluationOutput> findByTaskId(UUID taskId);
    long countByTask(Task task);
    void deleteByTask(Task task);
} 