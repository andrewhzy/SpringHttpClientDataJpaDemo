package com.example.springhttpclientdatajpademo.infrastructure.persistence;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.model.Task;
import com.example.springhttpclientdatajpademo.domain.repository.ChatEvaluationInputRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA implementation of ChatEvaluationInputRepository
 */
@Repository
public interface JpaChatEvaluationInputRepository extends JpaRepository<ChatEvaluationInput, UUID>, ChatEvaluationInputRepository {
    
    @Override
    List<ChatEvaluationInput> findByTask(Task task);
    
    @Override
    List<ChatEvaluationInput> findByTaskId(UUID taskId);
    
    @Override
    long countByTask(Task task);
    
    @Override
    void deleteByTask(Task task);
    
    // The saveAll method is automatically provided by JpaRepository
    // No need to override it explicitly
} 