package com.example.springhttpclientdatajpademo.application.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;



public class TaskServiceFactory implements ApplicationContextAware{

    private static final Map<TaskType, TaskService> taskServiceMap = new HashMap<>();

    public TaskService getTaskService(TaskType taskType) {
        return taskServiceMap.get(taskType);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        for (TaskType taskType : TaskType.values()) {
            taskServiceMap.put(taskType, applicationContext.getBean(taskType.name() + "TaskService", TaskService.class));
        }
    }

}
