package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service interface for parsing Excel files for chat evaluation tasks
 * Focus on POST /rest/v1/tasks endpoint requirements
 */
public interface ExcelParsingService {
    
    /**
     * Parse Excel file and extract chat evaluation data from all sheets
     * 
     * Validates file format and structure according to API specification:
     * - File format: .xlsx or .xls
     * - Maximum size: 50MB
     * - Maximum sheets: 20 per file
     * - Maximum rows per sheet: 1,000
     * - Required columns: "question", "golden_answer", "golden_citations"
     * 
     * @param file the Excel file to parse
     * @return map of sheet names to list of chat evaluation inputs
     * @throws IOException if file cannot be read or parsed
     * @throws IllegalArgumentException if file validation fails
     */
    Map<String, List<ChatEvaluationInput>> parseExcelFile(MultipartFile file) throws IOException;
    
    /**
     * Validate Excel file format and structure
     * 
     * Validates according to API specification requirements:
     * - File size within 50MB limit
     * - Valid Excel format (.xlsx or .xls)
     * - Contains at least one sheet with required columns
     * - No more than 20 sheets
     * - No more than 1,000 rows per sheet
     * 
     * @param file the Excel file to validate
     * @throws IllegalArgumentException if file validation fails
     * @throws IOException if file cannot be read
     */
    void validateExcelFile(MultipartFile file) throws IOException;
} 