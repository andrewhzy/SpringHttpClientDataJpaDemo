package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.TaskListResponse;
import com.example.springhttpclientdatajpademo.application.dto.TaskSummaryDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
                        .filename(command.getFile().getOriginalFilename())
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
                    .filename(command.getFile().getOriginalFilename())
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

    /**
     * List user tasks with cursor-based pagination
     * Implementation for GET /rest/api/v1/tasks endpoint
     * 
     * @param userId the authenticated user's ID
     * @param perPage number of items per page (1-100)
     * @param taskType task type filter (required)
     * @param cursor optional cursor for pagination (null for first page)
     * @return cursor-based paginated task list response with metadata
     * @throws TaskValidationException if pagination parameters are invalid
     */
    @Override
    @Transactional(readOnly = true)
    public TaskListResponse listUserTasks(
            final String userId,
            final int perPage,
            final String taskType,
            final Long cursor) {

        log.info("Listing tasks for user: {}, perPage: {}, taskType: {}, cursor: {}", 
                userId, perPage, taskType, cursor);

        try {
            // Validate pagination parameters
            validatePaginationParameters(perPage);

            // Convert string taskType to enum
            final Task.TaskType taskTypeEnum = parseTaskType(taskType);

            // Create pageable for limit
            final Pageable pageable = PageRequest.of(0, perPage + 1); // +1 to check if more results exist

            // Query tasks based on cursor
            final List<Task> tasks;
            if (cursor == null) {
                // First page - no cursor
                tasks = taskRepository.findFirstPageByUserIdAndTaskType(userId, taskTypeEnum, pageable);
            } else {
                // Subsequent pages - use cursor
                tasks = taskRepository.findNextPageByUserIdAndTaskTypeAfterCursor(
                        userId, taskTypeEnum, cursor, pageable);
            }

            // Determine if there are more results
            final boolean hasMore = tasks.size() > perPage;
            final List<Task> resultTasks = hasMore ? tasks.subList(0, perPage) : tasks;

            // Convert to DTOs
            final List<TaskSummaryDto> taskSummaries = resultTasks.stream()
                    .map(this::convertToTaskSummaryDto)
                    .collect(Collectors.toList());

            // Generate next cursor from last item (if more results exist)
            final Long nextCursor = hasMore && !resultTasks.isEmpty() ? 
                    resultTasks.get(resultTasks.size() - 1).getId() : null;

            // Get total count for metadata
            final long totalCount = taskRepository.countByUserIdAndTaskType(userId, taskTypeEnum);

            // Build pagination metadata
            final TaskListResponse.PaginationMeta meta = TaskListResponse.PaginationMeta.builder()
                    .perPage(perPage)
                    .total(totalCount)
                    .nextCursor(nextCursor != null ? nextCursor.toString() : null)
                    .hasMore(hasMore)
                    .build();

            final TaskListResponse response = TaskListResponse.builder()
                    .data(taskSummaries)
                    .meta(meta)
                    .build();

            log.info("Listed {} tasks for user: {}, total: {}, hasMore: {}", 
                    taskSummaries.size(), userId, totalCount, hasMore);

            return response;

        } catch (final IllegalArgumentException ex) {
            throw new TaskValidationException("Invalid parameters: " + ex.getMessage(), ex);
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to list user tasks: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validate pagination parameters for cursor-based pagination
     */
    private void validatePaginationParameters(final int perPage) {
        if (perPage < 1 || perPage > 100) {
            throw new IllegalArgumentException("Per page must be between 1 and 100");
        }
    }

    /**
     * Parse task type string to enum
     */
    private Task.TaskType parseTaskType(final String taskType) {
        if (taskType == null || taskType.trim().isEmpty()) {
            return null;
        }
        try {
            // Convert kebab-case to UPPER_CASE
            final String enumName = taskType.toUpperCase().replace("-", "_");
            return Task.TaskType.valueOf(enumName);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid task type: " + taskType);
        }
    }

    /**
     * Convert Task entity to TaskSummaryDto
     */
    private TaskSummaryDto convertToTaskSummaryDto(final Task task) {
        // Calculate progress percentage
        final int progressPercentage = task.getRowCount() > 0 ? 
                (int) Math.round((double) 0 / task.getRowCount() * 100) : 0; // TODO: Add processed_rows to Task entity

        return TaskSummaryDto.builder()
                .id(task.getId().toString())
                .userId(task.getUserId())
                .originalFilename(task.getFilename())
                .sheetName(task.getSheetName())
                .taskType(task.getTaskType().getValue())
                .taskStatus(task.getTaskStatus().getValue())
                .uploadBatchId(task.getUploadBatchId().toString())
                .rowCount(task.getRowCount())
                .processedRows(0) // TODO: Add processed_rows to Task entity
                .progressPercentage(progressPercentage)
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .cancelledAt(task.getCancelledAt())
                .build();
    }
} 