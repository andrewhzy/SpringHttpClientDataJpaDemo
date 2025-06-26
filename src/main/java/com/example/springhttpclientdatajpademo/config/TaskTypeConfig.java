package com.example.springhttpclientdatajpademo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Configuration for TaskType settings loaded from application.yml
 * Provides additional metadata and validation rules for each task type
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.task-types")
public class TaskTypeConfig {
    
    /**
     * Set of enabled task types
     */
    private Set<String> enabled;
    
    /**
     * Detailed configuration for each task type
     */
    private Map<String, TaskTypeSettings> settings;
    
    @Data
    public static class TaskTypeSettings {
        /**
         * Display name for UI
         */
        private String displayName;
        
        /**
         * Description of the task type
         */
        private String description;
        
        /**
         * Maximum file size in MB
         */
        private int maxFileSizeMb = 50;
        
        /**
         * Maximum rows per sheet
         */
        private int maxRowsPerSheet = 1000;
        
        /**
         * Required columns for this task type
         */
        private Set<String> requiredColumns;
        
        /**
         * Parser service bean name
         */
        private String parserServiceBean;
        
        /**
         * Whether this task type supports background processing
         */
        private boolean backgroundProcessing = true;
        
        /**
         * Estimated processing time per row in seconds
         */
        private double estimatedTimePerRowSeconds = 0.5;
    }
} 