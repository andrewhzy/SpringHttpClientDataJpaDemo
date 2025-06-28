package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.TaskItem;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for parsing Excel files for chat evaluation tasks
 * Focus on POST /rest/api/v1/tasks endpoint requirements
 * <p>
 * Note: This interface does not declare checked exceptions.
 * Implementations should convert any checked exceptions to appropriate
 * unchecked exceptions for clean client code.
 */
public interface ExcelParsingService {

    /**
     * Parse Excel file and extract chat evaluation data from all sheets
     * <p>
     * Validates file format and structure according to API specification:
     * - File format: .xlsx or .xls
     * - Maximum size: 50MB
     * - Maximum sheets: 20 per file
     * - Maximum rows per sheet: 1,000
     * - Required columns: "question", "golden_answer", "golden_citations"
     *
     * @param file the Excel file to parse
     * @return flattened list of all input records from all valid sheets
     * @throws RuntimeException if file cannot be read or parsed (unchecked)
     */
    List<? extends TaskItem> parseExcelFile(MultipartFile file);

    /**
     * Validate Excel file format and structure
     * <p>
     * Validates according to API specification requirements:
     * - File size within 50MB limit
     * - Valid Excel format (.xlsx or .xls)
     * - Contains at least one sheet with required columns
     * - No more than 20 sheets
     * - No more than 1,000 rows per sheet
     *
     * @param file the Excel file to validate
     * @throws RuntimeException if file validation fails (unchecked)
     */
    void validateExcelFile(MultipartFile file);

    Task.TaskType getTaskType();
} 