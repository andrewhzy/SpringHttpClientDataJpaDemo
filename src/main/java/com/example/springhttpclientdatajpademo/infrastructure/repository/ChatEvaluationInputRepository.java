package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ChatEvaluationInput entities
 * Simplified for POST /rest/api/v1/tasks endpoint
 * 
 * Following Effective Java Item 64: Refer to objects by their interfaces
 */
@Repository
public interface ChatEvaluationInputRepository extends JpaRepository<ChatEvaluationInput, Long> {
    
    // JpaRepository provides saveAll() method needed for batch saving inputs
    // Additional methods can be added when other endpoints are implemented
} 