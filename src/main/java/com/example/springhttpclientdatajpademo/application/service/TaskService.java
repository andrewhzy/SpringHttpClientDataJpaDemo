package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.CreateTasksResponse;
import java.io.File;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;

public interface TaskService {

    CreateTasksResponse createTasksFromExcel(CreateTasksCommand createTasksCommand);

    File downloadTaskResult(Long taskId, TaskType taskType);

    TaskType getTaskType();


}
