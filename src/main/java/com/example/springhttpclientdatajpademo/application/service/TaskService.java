package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import java.io.File;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;

public interface TaskService {

    UploadResponse createTaskFromExcel(CreateTaskCommand createTaskCommand);

    File downloadTaskResult(Long taskId, TaskType taskType);

    TaskType getTaskType();


}
