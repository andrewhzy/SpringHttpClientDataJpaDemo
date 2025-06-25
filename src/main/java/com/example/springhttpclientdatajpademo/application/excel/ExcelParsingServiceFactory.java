package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating appropriate ExcelParsingService implementations
 * based on task type using ApplicationContextAware.
 * <p>
 * Implements ApplicationContextAware to get access to Spring ApplicationContext
 * and dynamically retrieve beans based on TaskType.
 */
@Component
@Slf4j
public class ExcelParsingServiceFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    // Mapping of TaskType to Spring bean names
    private static final Map<TaskType, String> TASK_TYPE_TO_BEAN_NAME = Map.of(
            TaskType.CHAT_EVALUATION, "chatEvaluationExcelParsingService",
            TaskType.URL_CLEANING, "urlCleaningExcelParsingService"
    );

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.debug("ApplicationContext injected into ExcelParsingServiceFactory");
    }

    /**
     * Get appropriate ExcelParsingService based on TaskType
     *
     * @param taskType the TaskType enum value
     * @return appropriate ExcelParsingService implementation
     * @throws IllegalArgumentException if task type is not supported
     * @throws IllegalStateException    if ApplicationContext is not available
     */
    public ExcelParsingService getExcelParsingService(TaskType taskType) {
        if (taskType == null) {
            throw new IllegalArgumentException("TaskType cannot be null");
        }

        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not available. Factory not properly initialized.");
        }

        String beanName = TASK_TYPE_TO_BEAN_NAME.get(taskType);
        if (beanName == null) {
            throw new IllegalArgumentException(
                    String.format("Unsupported task type: %s. Supported types: %s",
                            taskType, TASK_TYPE_TO_BEAN_NAME.keySet()));
        }

        try {
            ExcelParsingService service = applicationContext.getBean(beanName, ExcelParsingService.class);
            log.debug("Retrieved ExcelParsingService for task type '{}': {}", taskType, beanName);
            return service;
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Failed to retrieve ExcelParsingService for task type '%s' with bean name '%s'",
                            taskType, beanName), e);
        }
    }

    /**
     * Check if a TaskType is supported by this factory
     *
     * @param taskType the TaskType to check
     * @return true if supported, false otherwise
     */
    public boolean isTaskTypeSupported(TaskType taskType) {
        return TASK_TYPE_TO_BEAN_NAME.containsKey(taskType);
    }

    /**
     * Get all supported TaskTypes
     *
     * @return set of supported TaskTypes
     */
    public java.util.Set<TaskType> getSupportedTaskTypes() {
        return TASK_TYPE_TO_BEAN_NAME.keySet();
    }
} 