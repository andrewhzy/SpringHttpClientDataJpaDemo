package com.example.springhttpclientdatajpademo.repository;

import com.example.springhttpclientdatajpademo.entity.ChatEvaluationInput;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface ChatEvaluationInputRepository extends ReactiveCrudRepository<ChatEvaluationInput, Long> {
    
    Flux<ChatEvaluationInput> findByTaskId(UUID taskId);
    
    Flux<ChatEvaluationInput> findByTaskIdOrderByRowNumber(UUID taskId);
} 