package com.example.springhttpclientdatajpademo.application.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;

/**
 * Factory for creating appropriate TaskService implementations
 * Uses Spring ApplicationContext to discover and register TaskService beans automatically
 * 
 * This factory implements the Factory pattern to provide loose coupling
 * between clients and concrete TaskService implementations
 */
@Component
@Slf4j
public class TaskServiceFactory implements ApplicationContextAware {

    private static final Map<TaskType, TaskService> taskServiceMap = new HashMap<>();

    /**
     * Get the appropriate TaskService for the given task type
     * 
     * @param taskType the task type
     * @return TaskService implementation for the task type, or null if not found
     */
    public TaskService getTaskService(TaskType taskType) {
        TaskService service = taskServiceMap.get(taskType);
        log.debug("Retrieved TaskService for type {}: {}", taskType, service != null ? service.getClass().getSimpleName() : "null");
        return service;
    }

    /**
     * Automatically register all TaskService beans when ApplicationContext is set
     * Called by Spring during bean initialization
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        log.info("Initializing TaskServiceFactory with discovered TaskService beans");
        
        applicationContext.getBeansOfType(TaskService.class)
                .values()
                .forEach(bean -> {
                    TaskType taskType = bean.getTaskType();
                    taskServiceMap.put(taskType, bean);
                    log.info("Registered TaskService: {} for TaskType: {}", 
                            bean.getClass().getSimpleName(), taskType);
                });
                
        log.info("TaskServiceFactory initialization complete. Registered services: {}", taskServiceMap.keySet());
    }
}
