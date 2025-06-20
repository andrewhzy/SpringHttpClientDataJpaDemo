package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.excel.ExcelParsingService;
import com.example.springhttpclientdatajpademo.application.exception.FileProcessingException;
import com.example.springhttpclientdatajpademo.application.exception.TaskValidationException;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.task.model.Task;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for task creation from Excel upload
 * Focuses only on the POST /rest/api/v1/tasks endpoint functionality
 * 
 * Following Effective Java principles:
 * - Item 72: Favor the use of standard exceptions
 * - Item 73: Throw exceptions appropriate to the abstraction level
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskApplicationService implements TaskService {

    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ExcelParsingService excelParsingService;

    /**
     * Create task from Excel upload - simplified method for controller
     *
     * @param file the Excel file to process
     * @param description optional description for the upload batch
     * @return upload response with created tasks
     * @throws FileProcessingException if file processing fails
     * @throws TaskValidationException if file validation fails
     */
    @Override
    public UploadResponse createTaskFromExcel(final MultipartFile file, final String description) {
        // TODO: Extract user ID from JWT token when authentication is implemented
        final String userId = getCurrentUserId();

        // Create command internally
        final CreateTaskCommand command = CreateTaskCommand.builder()
                .file(file)
                .userId(userId)
                .description(description)
                .build();

        return createTaskFromExcel(command);
    }

    /**
     * Create task from Excel upload - main use case for POST /rest/api/v1/tasks
     *
     * @param command the create task command
     * @return upload response with created tasks
     * @throws FileProcessingException if file processing fails
     * @throws TaskValidationException if file validation fails
     */
    @Transactional
    public UploadResponse createTaskFromExcel(final CreateTaskCommand command) {

        log.info("Processing Excel upload for user: {}, filename: {}",
                command.getUserId(), command.getFile().getOriginalFilename());

        try {
            // 1. Validate Excel file (includes file size, type, and structure validation)
            excelParsingService.validateExcelFile(command.getFile());

            // 2. Parse Excel file and extract all sheets with chat evaluation data
            final Map<String, List<ChatEvaluationInput>> parsedData = excelParsingService.parseExcelFile(command.getFile());

            // 3. Generate upload batch ID using UUID for better uniqueness
            final String uploadBatchId = UUID.randomUUID().toString();
            
            // Convert to Long for database storage (using hash of UUID for uniqueness)
            final Long uploadBatchIdLong = Math.abs(uploadBatchId.hashCode() % 10000000L) + 1000000L;

            // 4. Create tasks for each valid sheet
            final List<UploadResponse.TaskSummary> taskSummaries = new ArrayList<>();
            int totalQuestions = 0;

            for (final Map.Entry<String, List<ChatEvaluationInput>> entry : parsedData.entrySet()) {
                final String sheetName = entry.getKey();
                final List<ChatEvaluationInput> inputs = entry.getValue();

                if (inputs.isEmpty()) {
                    log.warn("Sheet '{}' contains no valid data, skipping", sheetName);
                    continue;
                }

                // Create task for this sheet
                final Task task = Task.builder()
                        .userId(command.getUserId())
                        .filename(command.getFile().getOriginalFilename())
                        .sheetName(sheetName)
                        .taskType(Task.TaskType.CHAT_EVALUATION)
                        .taskStatus(Task.TaskStatus.QUEUEING)
                        .uploadBatchId(uploadBatchIdLong)
                        .rowCount(inputs.size())
                        .build();

                // Save task first to get ID
                final Task savedTask = taskRepository.save(task);

                // Associate inputs with task and save them
                inputs.forEach(input -> input.setTask(savedTask));
                inputRepository.saveAll(inputs);

                // Create task summary
                final UploadResponse.TaskSummary taskSummary = UploadResponse.TaskSummary.builder()
                        .taskId(savedTask.getId().toString())
                        .sheetName(sheetName)
                        .taskType(Task.TaskType.CHAT_EVALUATION.getValue())
                        .status(Task.TaskStatus.QUEUEING.getValue())
                        .rowCount(inputs.size())
                        .build();

                taskSummaries.add(taskSummary);
                totalQuestions += inputs.size();

                log.info("Created task for sheet '{}' with {} questions", sheetName, inputs.size());
            }

            if (taskSummaries.isEmpty()) {
                throw new TaskValidationException("No valid sheets found in Excel file");
            }

            // 5. Build response
            final UploadResponse response = UploadResponse.builder()
                    .uploadBatchId(uploadBatchId)
                    .tasks(taskSummaries)
                    .totalSheets(taskSummaries.size())
                    .message(String.format("Successfully created %d tasks with %d total questions", 
                            taskSummaries.size(), totalQuestions))
                    .build();

            log.info("Excel upload completed successfully: {} tasks created with {} total questions",
                    taskSummaries.size(), totalQuestions);

            return response;

        } catch (final IllegalArgumentException ex) {
            // Convert validation exceptions to our custom exception
            throw new TaskValidationException("File validation failed: " + ex.getMessage(), ex);
        } catch (final Exception ex) {
            // Convert any other exceptions to file processing exception
            throw new FileProcessingException("Failed to process Excel file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get current user ID - placeholder implementation
     * TODO: Replace with actual JWT token extraction when authentication is implemented
     */
    private String getCurrentUserId() {
        // Placeholder implementation - in real app, extract from JWT token
        return "system-user";
    }
} 