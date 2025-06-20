package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.excel.ExcelParsingService;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.task.event.TaskStartedEvent;
import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for task creation from Excel upload
 * Focuses only on the POST /rest/v1/tasks endpoint functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskApplicationService implements TaskService {

    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ExcelParsingService excelParsingService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create task from Excel upload - simplified method for controller
     *
     * @param file the Excel file to process
     * @param description optional description for the upload batch
     * @return upload response with created tasks
     * @throws FileProcessingException if file processing fails
     * @throws TaskValidationException if file validation fails
     */
    public UploadResponse createTaskFromExcel(MultipartFile file, String description) {
        // TODO: Extract user ID from JWT token when authentication is implemented
        String userId = getCurrentUserId();

        // Create command internally
        CreateTaskCommand command = CreateTaskCommand.builder()
                .file(file)
                .userId(userId)
                .description(description)
                .build();

        return createTaskFromExcel(command);
    }

    /**
     * Create task from Excel upload - main use case for POST /rest/v1/tasks
     *
     * @param command the create task command
     * @return upload response with created tasks
     * @throws FileProcessingException if file processing fails
     * @throws TaskValidationException if file validation fails
     */
    @Transactional
    public UploadResponse createTaskFromExcel(CreateTaskCommand command) {

        log.info("Processing Excel upload for user: {}, filename: {}",
                command.getUserId(), command.getFile().getOriginalFilename());

                // 1. Validate Excel file (includes file size, type, and structure validation)
        excelParsingService.validateExcelFile(command.getFile());

        // 2. Parse Excel file and extract all sheets with chat evaluation data
        Map<String, List<ChatEvaluationInput>> parsedData = excelParsingService.parseExcelFile(command.getFile());

        // 3. Generate upload batch ID using UUID for better uniqueness
        String uploadBatchId = UUID.randomUUID().toString();
        
        // Convert to Long for database storage (using hash of UUID for uniqueness)
        Long uploadBatchIdLong = Math.abs(uploadBatchId.hashCode() % 10000000L) + 1000000L;

        // 4. Create tasks for each valid sheet
        List<UploadResponse.TaskSummary> taskSummaries = new ArrayList<>();

        for (Map.Entry<String, List<ChatEvaluationInput>> entry : parsedData.entrySet()) {
            String sheetName = entry.getKey();
            List<ChatEvaluationInput> inputData = entry.getValue();

            if (inputData.isEmpty()) {
                log.warn("Skipping empty sheet: {} in file: {}", sheetName, command.getFile().getOriginalFilename());
                continue;
            }

            // Create task using domain model with correct field name
            Task task = Task.builder()
                    .userId(command.getUserId())
                    .filename(command.getFile().getOriginalFilename())
                    .sheetName(sheetName)
                    .taskType(Task.TaskType.CHAT_EVALUATION)
                    .taskStatus(Task.TaskStatus.QUEUEING)
                    .uploadBatchId(uploadBatchIdLong)
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

            // Publish domain event for task creation
            publishTaskStartedEvent(task);

            // Add to response with correct String type for task ID
            taskSummaries.add(UploadResponse.TaskSummary.builder()
                    .taskId(task.getId().toString())
                    .sheetName(sheetName)
                    .taskType(task.getTaskType().getValue())
                    .status(task.getTaskStatus().getValue())
                    .rowCount(task.getRowCount())
                    .build());
        }

        if (taskSummaries.isEmpty()) {
            throw new TaskValidationException("No valid chat evaluation sheets found in Excel file. " +
                    "Excel must contain sheets with required columns: question, golden_answer, golden_citations");
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
     * Publish task started event for potential background processing
     *
     * @param task the task that was started
     */
    private void publishTaskStartedEvent(Task task) {
        try {
            TaskStartedEvent event = new TaskStartedEvent(
                    task.getId(),
                    LocalDateTime.now(),
                    task.getRowCount()
            );
            eventPublisher.publishEvent(event);
            log.debug("Published TaskStartedEvent for task: {}", task.getId());
        } catch (Exception e) {
            log.warn("Failed to publish TaskStartedEvent for task: {}, error: {}", task.getId(), e.getMessage());
            // Don't fail the entire operation if event publishing fails
        }
    }

    /**
     * Get current user ID - placeholder implementation
     * TODO: Extract from JWT token when authentication is implemented
     *
     * @return current user ID
     */
    private String getCurrentUserId() {
        // Placeholder implementation - in real app would extract from security context/JWT
        return "test-user";
    }

    // Custom Runtime Exceptions for better error handling

    /**
     * Exception thrown when file processing fails
     */
    public static class FileProcessingException extends RuntimeException {
        public FileProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception thrown when task validation fails
     */
    public static class TaskValidationException extends RuntimeException {
        public TaskValidationException(String message) {
            super(message);
        }

        public TaskValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 