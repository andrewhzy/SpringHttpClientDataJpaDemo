package com.example.springhttpclientdatajpademo.application.service;

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
     * List user tasks with filtering and pagination
     * 
     * Returns a paginated list of task metadata for the authenticated user.
     * Supports filtering by various criteria and always returns tasks ordered
     * by creation date (newest first).
     * 
     * @param userId the authenticated user's ID
     * @param page page number (1-based)
     * @param perPage number of items per page (1-100)
     * @param status optional status filter
     * @param taskType optional task type filter  
     * @param uploadBatchId optional upload batch ID filter
     * @param filename optional filename filter (partial match)
     * @param createdAfter optional created after date filter
     * @param createdBefore optional created before date filter
     * @return paginated task list response with metadata
     * @throws RuntimeException if filtering or data access fails (unchecked)
     */
    TaskListResponse listUserTasks(
            String userId,
            int page,
            int perPage,
            String status,
            String taskType,
            String uploadBatchId,
            String filename,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore);
} 