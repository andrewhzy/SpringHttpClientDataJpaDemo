package com.example.springhttpclientdatajpademo.service;

import com.example.springhttpclientdatajpademo.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.entity.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.entity.Task;
import com.example.springhttpclientdatajpademo.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core business logic service for task management operations
 * Coordinates between repositories and Excel parsing service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ExcelParsingService excelParsingService;
    
    /**
     * Process uploaded Excel file and create chat evaluation tasks
     * 
     * @param file the uploaded Excel file
     * @param description optional description for the upload batch
     * @param userId the user ID from JWT token
     * @return upload response with created tasks
     * @throws IOException if file processing fails
     * @throws IllegalArgumentException if file validation fails
     */
    @Transactional
    public UploadResponse processExcelUpload(MultipartFile file, String description, String userId) 
            throws IOException {
        
        log.info("Processing Excel upload for user: {}, filename: {}", userId, file.getOriginalFilename());
        
        // 1. Validate Excel file
        excelParsingService.validateExcelFile(file);
        
        // 2. Parse Excel file and extract all sheets
        Map<String, List<ChatEvaluationInput>> parsedData = excelParsingService.parseExcelFile(file);
        
        // 3. Generate upload batch ID
        UUID uploadBatchId = UUID.randomUUID();
        
        // 4. Create tasks for each valid sheet
        List<UploadResponse.TaskSummary> taskSummaries = new ArrayList<>();
        
        for (Map.Entry<String, List<ChatEvaluationInput>> entry : parsedData.entrySet()) {
            String sheetName = entry.getKey();
            List<ChatEvaluationInput> inputData = entry.getValue();
            
            if (inputData.isEmpty()) {
                log.warn("Skipping empty sheet: {} in file: {}", sheetName, file.getOriginalFilename());
                continue;
            }
            
            // Create task entity
            Task task = Task.builder()
                    .userId(userId)
                    .filename(file.getOriginalFilename())
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
            for (int i = 0; i < inputData.size(); i++) {
                ChatEvaluationInput input = inputData.get(i);
                input.setTask(task);
                input.setRowNumber(i + 1); // Row numbers start from 1
            }
            
            inputRepository.saveAll(inputData);
            log.info("Saved {} input records for task {}", inputData.size(), task.getId());
            
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
     * Get task by ID with user ownership validation
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
} 