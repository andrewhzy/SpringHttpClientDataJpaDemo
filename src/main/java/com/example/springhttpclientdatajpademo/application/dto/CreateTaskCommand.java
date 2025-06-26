package com.example.springhttpclientdatajpademo.application.dto;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command for creating a task from Excel upload
 * Application layer DTO for use case input
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskCommand {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String description;

    @NotNull(message = "Task type is required")
    private TaskType taskType;
} 