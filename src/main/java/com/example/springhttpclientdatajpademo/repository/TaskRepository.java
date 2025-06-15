package com.example.springhttpclientdatajpademo.repository;

import com.example.springhttpclientdatajpademo.entity.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TaskRepository extends ReactiveCrudRepository<Task, UUID> {
    
    Flux<Task> findByUserId(String userId);
    
    Flux<Task> findByUploadBatchId(UUID uploadBatchId);
} 