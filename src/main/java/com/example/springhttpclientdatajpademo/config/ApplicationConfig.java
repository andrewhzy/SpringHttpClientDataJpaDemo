package com.example.springhttpclientdatajpademo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Main application configuration class
 * Enables async processing, scheduling, retry mechanisms, and CORS for chat evaluation
 */
@Configuration
@EnableTransactionManagement
@EnableRetry
@EnableAsync
@EnableScheduling
@Slf4j
public class ApplicationConfig {
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String[] allowedOrigins;
    
    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;
    
    @Value("${app.cors.max-age:3600}")
    private long maxAge;
    
    @PostConstruct
    public void init() {
        log.info("Task Management API configuration initialized with async processing, scheduling, and CORS");
        log.info("CORS allowed origins: {}", Arrays.toString(allowedOrigins));
    }
    
    /**
     * CORS configuration for Internal Task Management API
     * Allows frontend applications to access the API from different origins
     * 
     * @return CORS configuration source with appropriate settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configurable via properties)
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        
        // Allow all headers (including custom ones for JWT)
        configuration.addAllowedHeader("*");
        
        // Allow credentials (important for JWT authentication)
        configuration.setAllowCredentials(true);
        
        // Cache preflight responses (OPTIONS requests)
        configuration.setMaxAge(maxAge);
        
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/rest/api/**", configuration);
        
        log.info("CORS configuration applied to /rest/api/** endpoints");
        log.info("Allowed methods: {}, Max age: {}s", Arrays.toString(allowedMethods), maxAge);
        
        return source;
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