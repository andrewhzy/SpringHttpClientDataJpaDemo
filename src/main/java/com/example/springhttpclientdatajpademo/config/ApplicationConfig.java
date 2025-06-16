package com.example.springhttpclientdatajpademo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;

/**
 * Main application configuration class
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class ApplicationConfig {
    
    @PostConstruct
    public void init() {
        log.info("Task Management API configuration initialized");
    }
} 