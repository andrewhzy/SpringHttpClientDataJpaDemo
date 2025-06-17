package com.example.springhttpclientdatajpademo.config;

import com.example.springhttpclientdatajpademo.domain.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.domain.repository.ChatEvaluationOutputRepository;
import com.example.springhttpclientdatajpademo.domain.repository.TaskRepository;
import com.example.springhttpclientdatajpademo.infrastructure.persistence.JpaChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.infrastructure.persistence.JpaChatEvaluationOutputRepository;
import com.example.springhttpclientdatajpademo.infrastructure.persistence.JpaTaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for Domain-Driven Design components
 * Wires domain interfaces to infrastructure implementations
 */
@Configuration
public class DomainConfig {
    
    /**
     * Wire domain TaskRepository to JPA implementation
     */
    @Bean
    @Primary
    public TaskRepository taskRepository(JpaTaskRepository jpaRepository) {
        return jpaRepository;
    }
    
    /**
     * Wire domain ChatEvaluationInputRepository to JPA implementation
     */
    @Bean
    @Primary
    public ChatEvaluationInputRepository chatEvaluationInputRepository(JpaChatEvaluationInputRepository jpaRepository) {
        return jpaRepository;
    }
    
    /**
     * Wire domain ChatEvaluationOutputRepository to JPA implementation
     */
    @Bean
    @Primary
    public ChatEvaluationOutputRepository chatEvaluationOutputRepository(JpaChatEvaluationOutputRepository jpaRepository) {
        return jpaRepository;
    }
} 