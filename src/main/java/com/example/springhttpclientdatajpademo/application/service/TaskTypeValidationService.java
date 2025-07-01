//package com.example.springhttpclientdatajpademo.application.service;
//
//import com.example.springhttpclientdatajpademo.config.TaskTypeConfig;
//import com.example.springhttpclientdatajpademo.domain.task.Task;
//import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import jakarta.annotation.PostConstruct;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * Service for validating TaskTypes against configuration
// * Now works with UPPER_CASE enum names for consistency
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TaskTypeValidationService {
//
//    private final TaskTypeConfig taskTypeConfig;
//
//    @PostConstruct
//    public void init() {
//        log.info("TaskType validation service initialized");
//        log.info("Enabled task types from config: {}", getEnabledTaskTypes());
//
//        // Validate that all enabled task types exist in enum
//        Set<String> invalidTypes = getEnabledTaskTypes().stream()
//                .filter(type -> !isValidEnumValue(type))
//                .collect(Collectors.toSet());
//
//        if (!invalidTypes.isEmpty()) {
//            log.warn("Configuration contains invalid task types: {}", invalidTypes);
//        }
//    }
//
//    /**
//     * Get enabled task types from configuration (converted to UPPER_CASE)UPPER_CASE
//     */
//    public Set<String> getEnabledTaskTypes() {
//        if (taskTypeConfig.getEnabled() == null) {
//            return Set.of();
//        }
//
//        // Convert kebab-case config values to UPPER_CASE enum names
//        return taskTypeConfig.getEnabled().stream()
//                .map(this::kebabCaseToUpperCase)
//                .collect(Collectors.toSet());
//    }
//
//    /**
//     * Check if a task type is enabled in configuration
//     */
//    public boolean isTaskTypeEnabled(TaskType taskType) {
//        return getEnabledTaskTypes().contains(taskType.name());
//    }
//
//    /**
//     * Validate task type against configuration
//     */
//    public void validateTaskType(TaskType taskType) {
//        if (!isTaskTypeEnabled(taskType)) {
//            throw new IllegalArgumentException(
//                String.format("Task type '%s' is disabled in configuration. Enabled types: %s",
//                    taskType, getEnabledTaskTypes()));
//        }
//    }
//
//    /**
//     * Get task type settings from configuration
//     */
//    public TaskTypeConfig.TaskTypeSettings getTaskTypeSettings(TaskType taskType) {
//        if (taskTypeConfig.getSettings() == null) {
//            return null;
//        }
//
//        // Convert enum name back to kebab-case for config lookup
//        String kebabCase = upperCaseToKebabCase(taskType.name());
//        return taskTypeConfig.getSettings().get(kebabCase);
//    }
//
//    /**
//     * Get display name for task type
//     */
//    public String getDisplayName(TaskType taskType) {
//        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
//        if (settings != null && settings.getDisplayName() != null) {
//            return settings.getDisplayName();
//        }
//
//        // Fallback to kebab-case transformation
//        return upperCaseToKebabCase(taskType.name());
//    }
//
//    /**
//     * Get maximum file size for task type
//     */
//    public int getMaxFileSizeMb(TaskType taskType) {
//        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
//        return settings != null ? settings.getMaxFileSizeMb() : 50; // default
//    }
//
//    /**
//     * Get maximum rows per sheet for task type
//     */
//    public int getMaxRowsPerSheet(TaskType taskType) {
//        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
//        return settings != null ? settings.getMaxRowsPerSheet() : 1000; // default
//    }
//
//    /**
//     * Get required columns for task type
//     */
//    public Set<String> getRequiredColumns(TaskType taskType) {
//        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
//        return settings != null ? settings.getRequiredColumns() : Set.of();
//    }
//
//    /**
//     * Check if task type supports background processing
//     */
//    public boolean supportsBackgroundProcessing(TaskType taskType) {
//        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
//        return settings == null || settings.isBackgroundProcessing(); // default true
//    }
//
//    /**
//     * Get estimated processing time per row
//     */
//    public double getEstimatedTimePerRowSeconds(TaskType taskType) {
//        TaskTypeConfig.TaskTypeSettings settings = getTaskTypeSettings(taskType);
//        return settings != null ? settings.getEstimatedTimePerRowSeconds() : 0.5; // default
//    }
//
//    // Private helper methods
//
//    private boolean isValidEnumValue(String upperCaseName) {
//        try {
//            TaskType.valueOf(upperCaseName);
//            return true;
//        } catch (IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    /**
//     * Convert kebab-case to UPPER_CASE
//     * "chat-evaluation" -> "CHAT_EVALUATION"
//     */
//    private String kebabCaseToUpperCase(String kebabCase) {
//        return kebabCase.toUpperCase().replace('-', '_');
//    }
//
//    /**
//     * Convert UPPER_CASE to kebab-case
//     * "CHAT_EVALUATION" -> "chat-evaluation"
//     */
//    private String upperCaseToKebabCase(String upperCase) {
//        return upperCase.toLowerCase().replace('_', '-');
//    }
//
//    /**
//     * Get valid enum values as UPPER_CASE strings
//     */
//    private Set<String> getValidEnumValues() {
//        return Set.of(Task.TaskType.values())
//                .stream()
//                .map(Enum::name)
//                .collect(Collectors.toSet());
//    }
//}