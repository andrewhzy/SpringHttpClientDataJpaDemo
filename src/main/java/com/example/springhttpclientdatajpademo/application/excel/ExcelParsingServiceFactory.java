package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
    private static final Map<TaskType, ExcelParsingService> TASK_TYPE_TO_BEAN_NAME = new HashMap<>();

    @PostConstruct
    public void init() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not available. Factory not properly initialized.");
        }
        applicationContext
                .getBeansOfType(ExcelParsingService.class)
                .values()
                .forEach(service -> {
            TASK_TYPE_TO_BEAN_NAME.put(service.getTaskType(), service);
        });
    }

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

        ExcelParsingService excelParsingService = TASK_TYPE_TO_BEAN_NAME.get(taskType);
        if (excelParsingService == null) {
            throw new IllegalArgumentException(
                    String.format("Unsupported task type: %s. Supported types: %s",
                            taskType, TASK_TYPE_TO_BEAN_NAME.keySet()));
        }
        return excelParsingService;
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