package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.config.TaskTypeConfig;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for validating TaskTypes against configuration
 * Provides configuration-based validation while keeping enum benefits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskTypeValidationService {
    
    private final TaskTypeConfig taskTypeConfig;
    
    @PostConstruct
    public void init() {
        log.info("TaskType validation service initialized");
        log.info("Enabled task types from config: {}", getEnabledTaskTypes());
        
        // Validate that all enabled task types exist in enum
        Set<String> invalidTypes = getEnabledTaskTypes().stream()
                .filter(type -> !isValidEnumValue(type))
                .collect(Collectors.toSet());
        
        if (!invalidTypes.isEmpty()) {
            log.warn("Configuration contains invalid task types: {}", invalidTypes);
        }
    }
    
    /**
     * Get enabled task types from configuration
     */
    public Set<String> getEnabledTaskTypes() {
        return taskTypeConfig.getEnabled() != null ? 
               taskTypeConfig.getEnabled() : Set.of();
    }
    
    /**
     * Check if a task type is enabled in configuration
     */
    public boolean isTaskTypeEnabled(String taskType) {
        return getEnabledTaskTypes().contains(taskType);
    }
    
    /**
     * Validate task type against both enum and configuration
     */
    public void validateTaskType(String taskType) {
        // First check if it's a valid enum value
        if (!isValidEnumValue(taskType)) {
            throw new IllegalArgumentException(
                String.format("Invalid task type: '%s'. Valid types: %s", 
                    taskType, getValidEnumValues()));
        }
        
        // Then check if it's enabled in configuration
        if (!isTaskTypeEnabled(taskType)) {
            throw new IllegalArgumentException(
                String.format("Task type '%s' is disabled in configuration. Enabled types: %s",
                    taskType, getEnabledTaskTypes()));
        }
    }
    
    /**
     * Get task type settings from configuration
     */
    public TaskTypeConfig.TaskTypeSettings getTaskTypeSettings(String taskType) {
        if (taskTypeConfig.getSettings() == null) {
            return null;
        }
        return taskTypeConfig.getSettings().get(taskType);
    }
    
    /**
     * Get display name for task type
     */
    public String getDisplayName(String taskType) {
        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
        if (settings != null && settings.getDisplayName() != null) {
            return settings.getDisplayName();
        }
        
        // Fallback to enum display logic
        try {
            Task.TaskType enumValue = Task.TaskType.fromValue(taskType);
            return enumValue.getValue();
        } catch (IllegalArgumentException e) {
            return taskType;
        }
    }
    
    /**
     * Get maximum file size for task type
     */
    public int getMaxFileSizeMb(String taskType) {
        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
        return settings != null ? settings.getMaxFileSizeMb() : 50; // default
    }
    
    /**
     * Get maximum rows per sheet for task type
     */
    public int getMaxRowsPerSheet(String taskType) {
        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
        return settings != null ? settings.getMaxRowsPerSheet() : 1000; // default
    }
    
    /**
     * Get required columns for task type
     */
    public Set<String> getRequiredColumns(String taskType) {
        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
        return settings != null ? settings.getRequiredColumns() : Set.of();
    }
    
    /**
     * Check if task type supports background processing
     */
    public boolean supportsBackgroundProcessing(String taskType) {
        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
        return settings == null || settings.isBackgroundProcessing(); // default true
    }
    
    /**
     * Get estimated processing time per row
     */
    public double getEstimatedTimePerRowSeconds(String taskType) {
        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
        return settings != null ? settings.getEstimatedTimePerRowSeconds() : 0.5; // default
    }
    
    // Private helper methods
    
    private boolean isValidEnumValue(String taskType) {
        try {
            Task.TaskType.fromValue(taskType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    private Set<String> getValidEnumValues() {
        return Set.of(Task.TaskType.values())
                .stream()
                .map(Task.TaskType::getValue)
                .collect(Collectors.toSet());
    }
} 