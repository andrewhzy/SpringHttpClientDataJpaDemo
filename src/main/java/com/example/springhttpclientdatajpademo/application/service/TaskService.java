package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.ListTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.TaskListResponse;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Service interface for task management operations
 * 
 * This interface follows clean API design principles:
 * - No checked exceptions declared (implementations handle them internally)
 * - Clients don't need try-catch blocks when using these methods
 * - All exceptions are converted to appropriate RuntimeExceptions
 */
public interface TaskService {
    
    /**
     * Create tasks from Excel file upload
     * 
     * Processes an Excel file containing chat evaluation data and creates
     * tasks for each valid sheet. Validates file format, size, and structure
     * according to API specification requirements.
     * 
     * @param file the Excel file to process (.xlsx or .xls)
     * @param description optional description for the upload batch
     * @return upload response with created task summaries
     * @throws RuntimeException if file processing or validation fails (unchecked)
     */
    UploadResponse createTaskFromExcel(MultipartFile file, String description);
    
    /**
     * List user tasks with cursor-based pagination using query command
     * 
     * @param query the list tasks query command with validation
     * @return cursor-based paginated task list response with metadata
     * @throws RuntimeException if filtering or data access fails (unchecked)
     */
    TaskListResponse listUserTasks(ListTasksCommand query);
    
    /**
     * List user tasks with cursor-based pagination - legacy method for backward compatibility
     * 
     * @param userId the authenticated user's ID
     * @param perPage number of items per page (1-100)
     * @param taskType task type filter (required)
     * @param cursor optional cursor for pagination (null for first page)
     * @return cursor-based paginated task list response with metadata
     * @throws RuntimeException if filtering or data access fails (unchecked)
     * @deprecated Use {@link #listUserTasks(ListTasksCommand)} instead for consistency with command pattern
     */
    @Deprecated
    TaskListResponse listUserTasks(
            String userId,
            int perPage,
            String taskType,
            Long cursor);
} 