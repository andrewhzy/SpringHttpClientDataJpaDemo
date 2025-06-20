package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Task entity
 * Simplified for POST /rest/v1/tasks endpoint
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // JpaRepository provides save() method needed for task creation
    // Additional methods can be added when other endpoints are implemented
} 