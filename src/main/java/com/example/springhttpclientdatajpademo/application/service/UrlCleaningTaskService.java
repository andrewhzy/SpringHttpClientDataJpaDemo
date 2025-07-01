package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.CreateTasksResponse;
import com.example.springhttpclientdatajpademo.application.excel.UrlCleaningExcelParsingService;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationTaskItemRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;

import java.io.File;

public class UrlCleaningTaskService implements TaskService {
    UrlCleaningExcelParsingService urlCleaningExcelParsingService;
    ChatEvaluationTaskItemRepository chatEvaluationTaskItemRepository;
    TaskRepository taskRepository;

    UrlCleaningTaskService(UrlCleaningExcelParsingService urlCleaningExcelParsingService) {
        // TODO: Implement constructor
    }
    @Override
    public CreateTasksResponse createTasksFromExcel(CreateTasksCommand createTasksCommand) {
        return null;
    }

    @Override
    public File downloadTaskResult(Long taskId, Task.TaskType taskType) {
        return null;
    }

    @Override
    public Task.TaskType getTaskType() {
        return null;
    }
}
