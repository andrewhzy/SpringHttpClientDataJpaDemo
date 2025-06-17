package com.example.springhttpclientdatajpademo.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Command object for task creation
 * Encapsulates all data needed for the operation
 */
@Value
@Builder
public class CreateTaskCommand {
    
    @NotNull(message = "File is required")
    MultipartFile file;
    
    @NotBlank(message = "User ID is required")
    String userId;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description;
    
    @NotNull(message = "Upload batch ID is required")
    UUID uploadBatchId;
    
    String sheetName; // Optional - will process all sheets if null
    
    /**
     * Factory method for single file upload
     */
    public static CreateTaskCommand singleFile(MultipartFile file, String userId, String description) {
        return CreateTaskCommand.builder()
                .file(file)
                .userId(userId)
                .description(description)
                .uploadBatchId(UUID.randomUUID())
                .build();
    }
    
    /**
     * Factory method for batch upload with specific sheet
     */
    public static CreateTaskCommand withSheet(MultipartFile file, String userId, String sheetName, UUID batchId) {
        return CreateTaskCommand.builder()
                .file(file)
                .userId(userId)
                .sheetName(sheetName)
                .uploadBatchId(batchId)
                .build();
    }
    
    /**
     * Check if this is a batch operation
     */
    public boolean isBatchOperation() {
        return sheetName == null; // Process all sheets
    }
    
    /**
     * Get safe filename for storage
     */
    public String getSafeFilename() {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return "unknown_file";
        }
        // Remove potentially dangerous characters
        return originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Validate file type
     */
    public boolean isValidExcelFile() {
        String filename = getSafeFilename().toLowerCase();
        return filename.endsWith(".xlsx") || filename.endsWith(".xls");
    }
} 