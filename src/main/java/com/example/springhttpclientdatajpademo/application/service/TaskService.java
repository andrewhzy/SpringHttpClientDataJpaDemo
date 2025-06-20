package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

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
} 