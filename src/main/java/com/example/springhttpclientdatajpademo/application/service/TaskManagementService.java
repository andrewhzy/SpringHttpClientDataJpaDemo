package com.example.springhttpclientdatajpademo.application.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListTaskResponse;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;

public class TaskManagementService implements TaskService {

    private TaskServiceFactory taskServiceFactory;

    public TaskManagementService(TaskServiceFactory taskServiceFactory) {
        this.taskServiceFactory = taskServiceFactory;
    }

    @Override
    public UploadResponse createTaskFromExcel(CreateTaskCommand createTaskCommand) {
        TaskType taskType = createTaskCommand.getTaskType();
        TaskService taskService = taskServiceFactory.getTaskService(taskType);
        return taskService.createTaskFromExcel(createTaskCommand);
    }

    @Override
    public File downloadTaskResult(Long taskId, TaskType taskType) {
        TaskService taskService = taskServiceFactory.getTaskService(taskType);
        return taskService.downloadTaskResult(taskId, taskType);
    }

    public ListTaskResponse listUserTasks(ListTasksCommand listTasksCommand) {
        return null;
    }

    public void deleteTask(Long taskId) {

    }

    public void cancelTask(Long taskId) {

    }

    public List<String> getTaskTypes() {
        return Arrays.stream(TaskType.values())
                .filter(taskType -> taskServiceFactory.getTaskService(taskType) != null)
                .map(TaskType::getValue).toList();
    }
}