package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.*;
import com.example.springhttpclientdatajpademo.application.excel.ExcelParsingService;
import com.example.springhttpclientdatajpademo.application.excel.ExcelParsingServiceFactory;
import com.example.springhttpclientdatajpademo.application.exception.FileProcessingException;
import com.example.springhttpclientdatajpademo.application.exception.TaskValidationException;
import com.example.springhttpclientdatajpademo.domain.Input;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for task creation from Excel upload
 * Focuses only on the POST /rest/api/v1/tasks endpoint functionality
 * <p>
 * Following Effective Java principles:
 * - Item 72: Favor the use of standard exceptions
 * - Item 73: Throw exceptions appropriate to the abstraction level
 */
@Service
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ExcelParsingServiceFactory excelParsingServiceFactory;
    private final TaskTypeValidationService taskTypeValidationService;

    public TaskService(
            TaskRepository taskRepository,
            ChatEvaluationInputRepository inputRepository,
            ExcelParsingServiceFactory excelParsingServiceFactory,
            TaskTypeValidationService taskTypeValidationService) {
        this.taskRepository = taskRepository;
        this.inputRepository = inputRepository;
        this.excelParsingServiceFactory = excelParsingServiceFactory;
        this.taskTypeValidationService = taskTypeValidationService;
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
            // 1. Get appropriate Excel parsing service based on file content
            final Task.TaskType taskType = command.getTaskType();
            final ExcelParsingService excelParsingService = excelParsingServiceFactory.getExcelParsingService(taskType);

            // 2. Validate Excel file (includes file size, type, and structure validation)
            excelParsingService.validateExcelFile(command.getFile());

            // 3. Parse Excel file and extract all data
            final List<? extends Input> parsedData = excelParsingService.parseExcelFile(command.getFile());

            // Cast to ChatEvaluationInput - this will need to be made more generic for other task types
            @SuppressWarnings("unchecked") final List<ChatEvaluationInput> chatInputs = (List<ChatEvaluationInput>) parsedData;

            if (chatInputs.isEmpty()) {
                throw new TaskValidationException("No valid data found in Excel file");
            }

            // 3. Generate upload batch ID using UUID for better uniqueness
            final String uploadBatchId = UUID.randomUUID().toString();

            // Convert to Long for database storage (using hash of UUID for uniqueness)
            final Long uploadBatchIdLong = Math.abs(uploadBatchId.hashCode() % 10000000L) + 1000000L;

            // 4. Create a single task for all parsed data
            final Task task = Task.builder()
                    .userId(command.getUserId())
                    .filename(command.getFile().getOriginalFilename())
                    .sheetName("All Sheets") // Since we're combining all sheets
                    .taskType(taskType)
                    .taskStatus(Task.TaskStatus.QUEUEING)
                    .uploadBatchId(uploadBatchIdLong)
                    .rowCount(chatInputs.size())
                    .build();

            // Save task first to get ID
            final Task savedTask = taskRepository.save(task);

            // Associate all inputs with the task and save them
            chatInputs.forEach(input -> input.setTask(savedTask));
            inputRepository.saveAll(chatInputs);

            // Create task summary
            final UploadResponse.TaskSummary taskSummary = UploadResponse.TaskSummary.builder()
                    .taskId(savedTask.getId().toString())
                    .filename(command.getFile().getOriginalFilename())
                    .sheetName("All Sheets")
                    .taskType(taskType.getValue())
                    .status(Task.TaskStatus.QUEUEING.getValue())
                    .rowCount(chatInputs.size())
                    .build();

            final List<UploadResponse.TaskSummary> taskSummaries = List.of(taskSummary);
            final int totalQuestions = chatInputs.size();

            log.info("Created single task with {} questions from all sheets", totalQuestions);

            // 5. Build response
            final UploadResponse response = UploadResponse.builder()
                    .uploadBatchId(uploadBatchId)
                    .filename(command.getFile().getOriginalFilename())
                    .tasks(taskSummaries)
                    .totalSheets(1) // Single task for all sheets
                    .message(String.format("Successfully created 1 task with %d total questions", totalQuestions))
                    .build();

            log.info("Excel upload completed successfully: 1 task created with {} total questions", totalQuestions);

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
     * List user tasks with cursor-based pagination using query command
     * Implementation for GET /rest/api/v1/tasks endpoint - preferred method
     *
     * @param query the list tasks query command with validation
     * @return cursor-based paginated task list response with metadata
     * @throws TaskValidationException if query parameters are invalid
     */
    @Transactional(readOnly = true)
    public TaskListResponse listUserTasks(final ListTasksCommand query) {

        log.info("Listing tasks with query: userId={}, perPage={}, taskType={}, cursor={}",
                query.getUserId(), query.getPerPage(), query.getTaskType(), query.getCursor());

        try {
            // Convert string taskType to enum
            final Task.TaskType taskTypeEnum = parseTaskType(query.getTaskType());

            // Create pageable for limit
            final Pageable pageable = PageRequest.of(0, query.getPerPage() + 1); // +1 to check if more results exist

            // Query tasks based on cursor
            final Long cursorValue = query.isFirstPage() ? null : query.getCursor();
            final List<Task> tasks = taskRepository.findByUserIdAndTaskTypeWithCursor(
                    query.getUserId(), taskTypeEnum, cursorValue, pageable);

            // Determine if there are more results
            final boolean hasMore = tasks.size() > query.getPerPage();
            final List<Task> resultTasks = hasMore ? tasks.subList(0, query.getPerPage()) : tasks;

            // Convert to DTOs
            final List<TaskSummaryDto> taskSummaries = resultTasks.stream()
                    .map(this::convertToTaskSummaryDto)
                    .collect(Collectors.toList());

            // Generate next cursor from last item (if more results exist)
            final Long nextCursor = hasMore && !resultTasks.isEmpty() ?
                    resultTasks.get(resultTasks.size() - 1).getId() : null;

            // Get total count for metadata
            final long totalCount = taskRepository.countByUserIdAndTaskType(
                    query.getUserId(), taskTypeEnum);

            // Build pagination metadata
            final TaskListResponse.PaginationMeta meta = TaskListResponse.PaginationMeta.builder()
                    .perPage(query.getPerPage())
                    .total(totalCount)
                    .nextCursor(nextCursor != null ? nextCursor.toString() : null)
                    .hasMore(hasMore)
                    .build();

            final TaskListResponse response = TaskListResponse.builder()
                    .data(taskSummaries)
                    .meta(meta)
                    .build();

            log.info("Listed {} tasks for user: {}, total: {}, hasMore: {}",
                    taskSummaries.size(), query.getUserId(), totalCount, hasMore);

            return response;

        } catch (final IllegalArgumentException ex) {
            throw new TaskValidationException("Invalid query parameters: " + ex.getMessage(), ex);
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to list user tasks: " + ex.getMessage(), ex);
        }
    }

    /**
     * List user tasks with cursor-based pagination - legacy method for backward compatibility
     * Implementation for GET /rest/api/v1/tasks endpoint
     *
     * @param userId   the authenticated user's ID
     * @param perPage  number of items per page (1-100)
     * @param taskType task type filter (required)
     * @param cursor   optional cursor for pagination (null for first page)
     * @return cursor-based paginated task list response with metadata
     * @throws TaskValidationException if pagination parameters are invalid
     * @deprecated Use {@link #listUserTasks(ListTasksCommand)} instead for consistency with command pattern
     */
    @Deprecated
    @Transactional(readOnly = true)
    public TaskListResponse listUserTasks(
            final String userId,
            final int perPage,
            final String taskType,
            final Long cursor) {

        // Create query command and delegate to new method
        final ListTasksCommand query = ListTasksCommand.builder()
                .userId(userId)
                .perPage(perPage)
                .taskType(taskType)
                .cursor(cursor)
                .build();

        return listUserTasks(query);
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
     * Parse task type string to enum using configuration-based validation
     */
    private Task.TaskType parseTaskType(final String taskType) {
        if (taskType == null || taskType.trim().isEmpty()) {
            return null;
        }
        try {
            // Use configuration-based validation
            taskTypeValidationService.validateTaskType(taskType);
            return Task.TaskType.fromValue(taskType);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid task type: " + taskType + ". " + ex.getMessage());
        }
    }

    /**
     * Convert Task entity to TaskSummaryDto
     */
    private TaskSummaryDto convertToTaskSummaryDto(final Task task) {
        return TaskSummaryDto.builder()
                .id(task.getId().toString())
                .userId(task.getUserId())
                .originalFilename(task.getFilename())
                .sheetName(task.getSheetName())
                .taskType(task.getTaskType().getValue())
                .taskStatus(task.getTaskStatus().getValue())
                .uploadBatchId(task.getUploadBatchId().toString())
                .rowCount(task.getRowCount())
                .processedRows(task.getProcessedRows())
                .progressPercentage(task.getProgressPercentage())
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .cancelledAt(task.getCancelledAt())
                .build();
    }
} 