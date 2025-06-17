package com.example.springhttpclientdatajpademo.infrastructure.persistence;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.model.Task;
import com.example.springhttpclientdatajpademo.domain.repository.ChatEvaluationOutputRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA implementation of ChatEvaluationOutputRepository
 */
@Repository
public interface JpaChatEvaluationOutputRepository extends JpaRepository<ChatEvaluationOutput, UUID>, ChatEvaluationOutputRepository {
    
    @Override
    List<ChatEvaluationOutput> findByTask(Task task);
    
    @Override
    List<ChatEvaluationOutput> findByTaskId(UUID taskId);
    
    @Override
    long countByTask(Task task);
    
    @Override
    void deleteByTask(Task task);
} 