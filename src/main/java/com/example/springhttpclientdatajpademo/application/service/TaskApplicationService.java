package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.model.Task;
import com.example.springhttpclientdatajpademo.domain.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.domain.repository.TaskRepository;
import com.example.springhttpclientdatajpademo.infrastructure.external.ExcelParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for task management use cases
 * Orchestrates domain services and repositories
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskApplicationService {
    
    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ExcelParsingService excelParsingService;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Use case: Create task from Excel upload
     * 
     * @param command the create task command
     * @return upload response with created tasks
     * @throws IOException if file processing fails
     * @throws IllegalArgumentException if file validation fails
     */
    @Transactional
    public UploadResponse createTaskFromExcel(CreateTaskCommand command) throws IOException {
        
        log.info("Processing Excel upload for user: {}, filename: {}", 
                command.getUserId(), command.getFile().getOriginalFilename());
        
        // 1. Validate Excel file
        excelParsingService.validateExcelFile(command.getFile());
        
        // 2. Parse Excel file and extract all sheets
        Map<String, List<ChatEvaluationInput>> parsedData = excelParsingService.parseExcelFile(command.getFile());
        
        // 3. Generate upload batch ID
        UUID uploadBatchId = UUID.randomUUID();
        
        // 4. Create tasks for each valid sheet
        List<UploadResponse.TaskSummary> taskSummaries = new ArrayList<>();
        
        for (Map.Entry<String, List<ChatEvaluationInput>> entry : parsedData.entrySet()) {
            String sheetName = entry.getKey();
            List<ChatEvaluationInput> inputData = entry.getValue();
            
            if (inputData.isEmpty()) {
                log.warn("Skipping empty sheet: {} in file: {}", sheetName, command.getFile().getOriginalFilename());
                continue;
            }
            
            // Create task using domain model
            Task task = Task.builder()
                    .userId(command.getUserId())
                    .filename(command.getFile().getOriginalFilename())
                    .sheetName(sheetName)
                    .taskType(Task.TaskType.CHAT_EVALUATION)
                    .taskStatus(Task.TaskStatus.QUEUEING)
                    .uploadBatchId(uploadBatchId)
                    .rowCount(inputData.size())
                    .processedRows(0)
                    .build();
            
            // Save task
            task = taskRepository.save(task);
            log.info("Created task {} for sheet: {}, rows: {}", task.getId(), sheetName, inputData.size());
            
            // Associate input data with task and save
            for (ChatEvaluationInput input : inputData) {
                input.setTask(task);
            }
            
            inputRepository.saveAll(inputData);
            log.info("Saved {} input records for task {}", inputData.size(), task.getId());
            
            // Publish domain events
            publishDomainEvents(task);
            
            // Add to response
            taskSummaries.add(UploadResponse.TaskSummary.builder()
                    .taskId(task.getId())
                    .sheetName(sheetName)
                    .taskType(task.getTaskType().getValue())
                    .status(task.getTaskStatus().getValue())
                    .rowCount(task.getRowCount())
                    .build());
        }
        
        if (taskSummaries.isEmpty()) {
            throw new IllegalArgumentException("No valid chat evaluation sheets found in Excel file");
        }
        
        // 5. Build response
        UploadResponse response = UploadResponse.builder()
                .uploadBatchId(uploadBatchId)
                .tasks(taskSummaries)
                .totalSheets(taskSummaries.size())
                .message(String.format("Successfully created %d tasks from uploaded Excel file", 
                        taskSummaries.size()))
                .build();
        
        log.info("Upload processing completed for batch: {}, created {} tasks", 
                uploadBatchId, taskSummaries.size());
        
        return response;
    }
    
    /**
     * Use case: Start task processing
     * 
     * @param taskId the task ID
     * @param userId the user ID for ownership validation
     */
    @Transactional
    public void startTaskProcessing(UUID taskId, String userId) {
        Task task = getTaskWithOwnershipValidation(taskId, userId);
        
        // Use domain logic to start processing
        task.startProcessing();
        
        taskRepository.save(task);
        publishDomainEvents(task);
        
        log.info("Started processing for task: {}", taskId);
    }
    
    /**
     * Use case: Cancel task
     * 
     * @param taskId the task ID
     * @param userId the user ID for ownership validation
     */
    @Transactional
    public void cancelTask(UUID taskId, String userId) {
        Task task = taskRepository.findCancellableTask(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or cannot be cancelled"));
        
        // Use domain logic to cancel
        task.cancel();
        
        taskRepository.save(task);
        publishDomainEvents(task);
        
        log.info("Cancelled task: {}", taskId);
    }
    
    /**
     * Use case: Update task progress
     * 
     * @param taskId the task ID
     * @param processedRows number of processed rows
     * @param userId the user ID for ownership validation
     */
    @Transactional
    public void updateTaskProgress(UUID taskId, int processedRows, String userId) {
        Task task = getTaskWithOwnershipValidation(taskId, userId);
        
        // Use domain logic to update progress
        task.updateProgress(processedRows);
        
        taskRepository.save(task);
        publishDomainEvents(task);
        
        log.info("Updated progress for task: {} to {}/{}", taskId, processedRows, task.getRowCount());
    }
    
    /**
     * Use case: Get task with ownership validation
     * 
     * @param taskId the task ID
     * @param userId the user ID for ownership validation
     * @return the task if found and owned by user
     * @throws IllegalArgumentException if task not found or not owned by user
     */
    public Task getTaskWithOwnershipValidation(UUID taskId, String userId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or access denied"));
    }
    
    /**
     * Use case: Get tasks for user
     * 
     * @param userId the user ID
     * @return list of tasks for the user
     */
    public List<Task> getTasksForUser(String userId) {
        return taskRepository.findByUserId(userId);
    }
    
    /**
     * Use case: Get tasks by status for user
     * 
     * @param userId the user ID
     * @param status the task status
     * @return list of tasks matching criteria
     */
    public List<Task> getTasksByStatusForUser(String userId, Task.TaskStatus status) {
        return taskRepository.findByUserIdAndTaskStatus(userId, status);
    }
    
    /**
     * Validate Excel file for chat evaluation requirements
     * 
     * @param file the Excel file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateExcelForChatEvaluation(MultipartFile file) throws IOException {
        // Delegate to Excel parsing service
        excelParsingService.validateExcelFile(file);
        
        // Additional business logic validation can be added here
        List<String> sheetNames = excelParsingService.getSheetNames(file);
        
        boolean hasValidSheet = false;
        for (String sheetName : sheetNames) {
            if (excelParsingService.isValidChatEvaluationSheet(sheetName, file)) {
                hasValidSheet = true;
                break;
            }
        }
        
        if (!hasValidSheet) {
            throw new IllegalArgumentException(
                "No valid chat evaluation sheets found. Sheets must contain columns: question, golden_answer, golden_citations");
        }
    }
    
    /**
     * Check if file size is within limits
     * 
     * @param file the file to check
     * @param maxSizeBytes maximum allowed size in bytes
     * @throws IllegalArgumentException if file is too large
     */
    public void validateFileSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                String.format("File size %d bytes exceeds maximum limit of %d bytes", 
                        file.getSize(), maxSizeBytes));
        }
    }
    
    /**
     * Validate file type based on extension
     * 
     * @param file the file to validate
     * @param allowedExtensions list of allowed extensions (e.g., [".xlsx", ".xls"])
     * @throws IllegalArgumentException if file type is not allowed
     */
    public void validateFileType(MultipartFile file, List<String> allowedExtensions) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        boolean isValidType = allowedExtensions.stream()
                .anyMatch(ext -> filename.toLowerCase().endsWith(ext.toLowerCase()));
        
        if (!isValidType) {
            throw new IllegalArgumentException(
                String.format("File type not supported. Allowed types: %s", allowedExtensions));
        }
    }
    
    /**
     * Publish domain events from aggregate
     */
    private void publishDomainEvents(Task task) {
        task.getDomainEvents().forEach(eventPublisher::publishEvent);
        task.clearDomainEvents();
    }
} 