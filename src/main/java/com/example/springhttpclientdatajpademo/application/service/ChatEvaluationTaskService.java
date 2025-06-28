package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.*;
import com.example.springhttpclientdatajpademo.application.excel.ChatEvaluationExcelParsingService;
import com.example.springhttpclientdatajpademo.application.exception.FileProcessingException;
import com.example.springhttpclientdatajpademo.application.exception.TaskValidationException;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskItem;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationTaskItemRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class ChatEvaluationTaskService implements TaskService {

    private final TaskRepository taskRepository;
    private final ChatEvaluationTaskItemRepository taskItemRepository;
    private final ChatEvaluationExcelParsingService chatEvaluationExcelParsingService;

    public ChatEvaluationTaskService(
            TaskRepository taskRepository,
            ChatEvaluationTaskItemRepository taskItemRepository,
            ChatEvaluationExcelParsingService chatEvaluationExcelParsingService) {
        this.taskRepository = taskRepository;
        this.taskItemRepository = taskItemRepository;
        this.chatEvaluationExcelParsingService = chatEvaluationExcelParsingService;
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
    @Override
    public UploadResponse createTaskFromExcel(final CreateTaskCommand command) {

        log.info("Processing Excel upload for user: {}, filename: {}", command.getUserId(), command.getFile().getOriginalFilename());

        try {
            // 1. Get appropriate Excel parsing service based on file content
            final Task.TaskType taskType = command.getTaskType();

            // 2. Validate Excel file (includes file size, type, and structure validation)
            chatEvaluationExcelParsingService.validateExcelFile(command.getFile());

            // 3. Parse Excel file and extract data separated by sheets
            final Map<String, List<ChatEvaluationTaskItem>> parsedDataBySheet = chatEvaluationExcelParsingService.parseExcelFileBySheets(command.getFile());

            if (parsedDataBySheet.isEmpty()) {
                throw new TaskValidationException("No valid data found in Excel file");
            }

            // 4. Create tasks for each sheet
            final List<UploadResponse.TaskSummary> taskSummaries = new ArrayList<>();
            int totalQuestions = 0;

            for (Map.Entry<String, List<ChatEvaluationTaskItem>> entry : parsedDataBySheet.entrySet()) {
                final String sheetName = entry.getKey();
                final List<ChatEvaluationTaskItem> taskItems = entry.getValue();

                if (taskItems.isEmpty()) {
                    continue;
                }

                // Create task for this sheet
                final Task task = Task.builder()
                        .userId(command.getUserId())
                        .filename(command.getFile().getOriginalFilename())
                        .sheetName(sheetName)
                        .taskType(taskType)
                        .taskStatus(Task.TaskStatus.QUEUEING)
                        .rowCount(taskItems.size())
                        .build();

                // Save task first to get ID
                final Task savedTask = taskRepository.save(task);

                // Associate all inputs with the task and save them
                taskItems.forEach(taskItem -> taskItem.setTask(savedTask));
                taskItemRepository.saveAll(taskItems);

                // Create task summary
                final UploadResponse.TaskSummary taskSummary = UploadResponse.TaskSummary.builder()
                        .taskId(savedTask.getId().toString())
                        .filename(command.getFile().getOriginalFilename())
                        .sheetName(sheetName)
                        .taskType(taskType)
                        .status(Task.TaskStatus.QUEUEING)
                        .rowCount(taskItems.size())
                        .build();

                taskSummaries.add(taskSummary);
                totalQuestions += taskItems.size();

                log.info("Created task for sheet '{}' with {} questions", sheetName, taskItems.size());
            }

            log.info("Created {} tasks with {} total questions from {} sheets",
                    taskSummaries.size(), totalQuestions, parsedDataBySheet.size());

            // 5. Build response
            final UploadResponse response = UploadResponse.builder()
                    .filename(command.getFile().getOriginalFilename())
                    .tasks(taskSummaries)
                    .totalSheets(taskSummaries.size())
                    .message(String.format("Successfully created %d tasks with %d total questions", taskSummaries.size(), totalQuestions))
                    .build();

            log.info("Excel upload completed successfully: {} tasks created with {} total questions", taskSummaries.size(), totalQuestions);

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
     * TODO: Replace with actual JWT token extraction when authentication is
     * implemented
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
    public ListTaskResponse listUserTasks(final ListTasksCommand query) {

        log.info("Listing tasks with query: userId={}, perPage={}, taskType={}, cursor={}",
                query.getUserId(), query.getPerPage(), query.getTaskType(), query.getCursor());

        try {
            // Get taskType enum directly (no parsing needed as it's already TaskType)
            final Task.TaskType taskTypeEnum = query.getTaskType();

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
            final List<TaskInfoDto> taskSummaries = resultTasks.stream()
                    .map(this::convertToTaskSummaryDto)
                    .collect(Collectors.toList());

            // Generate next cursor from last item (if more results exist)
            final Long nextCursor = hasMore && !resultTasks.isEmpty() ? resultTasks.get(resultTasks.size() - 1).getId()
                    : null;

            // Get total count for metadata
            final long totalCount = taskRepository.countByUserIdAndTaskType(
                    query.getUserId(), taskTypeEnum);

            // Build pagination metadata
            final ListTaskResponse.PaginationMeta meta = ListTaskResponse.PaginationMeta.builder()
                    .perPage(query.getPerPage())
                    .total(totalCount)
                    .nextCursor(nextCursor != null ? nextCursor.toString() : null)
                    .hasMore(hasMore)
                    .build();

            final ListTaskResponse response = ListTaskResponse.builder()
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

    @Override
    public File downloadTaskResult(Long taskId, TaskType taskType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'downloadTaskResult'");
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.CHAT_EVALUATION;
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
     * Convert Task entity to TaskSummaryDto
     */
    private TaskInfoDto convertToTaskSummaryDto(final Task task) {
        return TaskInfoDto.builder()
                .id(task.getId().toString())
                .userId(task.getUserId())
                .originalFilename(task.getFilename())
                .sheetName(task.getSheetName())
                .taskType(task.getTaskType())
                .taskStatus(task.getTaskStatus())
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