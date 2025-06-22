package com.example.springhttpclientdatajpademo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * Main application configuration class
 * Enables async processing, scheduling, and retry mechanisms for chat evaluation
 */
@Configuration
@EnableTransactionManagement
@EnableRetry
@EnableAsync
@EnableScheduling
@Slf4j
public class ApplicationConfig {
    
    @PostConstruct
    public void init() {
        log.info("Task Management API configuration initialized with async processing and scheduling");
    }
    
    /**
     * Async executor for background task processing
     * Dedicated thread pool for chat evaluation processing
     * 
     * @return configured thread pool executor
     */
    @Bean(name = "chatEvaluationExecutor")
    public Executor chatEvaluationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ChatEval-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Chat evaluation executor configured: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
} 