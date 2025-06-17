package com.example.springhttpclientdatajpademo.infrastructure.external;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationInput;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service interface for parsing Excel files
 * Infrastructure service for external file processing
 */
public interface ExcelParsingService {
    
    /**
     * Parse Excel file and extract chat evaluation data from all sheets
     * 
     * @param file the Excel file to parse
     * @return map of sheet names to list of chat evaluation inputs
     * @throws IOException if file cannot be read or parsed
     * @throws IllegalArgumentException if file format is invalid
     */
    Map<String, List<ChatEvaluationInput>> parseExcelFile(MultipartFile file) throws IOException;
    
    /**
     * Validate Excel file format and structure
     * 
     * @param file the Excel file to validate
     * @throws IllegalArgumentException if file is invalid
     * @throws IOException if file cannot be read
     */
    void validateExcelFile(MultipartFile file) throws IOException;
    
    /**
     * Get list of sheet names from Excel file
     * 
     * @param file the Excel file
     * @return list of sheet names
     * @throws IOException if file cannot be read
     */
    List<String> getSheetNames(MultipartFile file) throws IOException;
    
    /**
     * Check if a specific sheet contains valid chat evaluation data
     * 
     * @param sheetName the sheet name to check
     * @param file the Excel file
     * @return true if sheet has valid structure
     * @throws IOException if file cannot be read
     */
    boolean isValidChatEvaluationSheet(String sheetName, MultipartFile file) throws IOException;
    
    /**
     * Parse a specific sheet from Excel file
     * 
     * @param file the Excel file
     * @param sheetName the sheet name to parse
     * @return list of chat evaluation inputs from the sheet
     * @throws IOException if file cannot be read or parsed
     * @throws IllegalArgumentException if sheet format is invalid
     */
    List<ChatEvaluationInput> parseSheet(MultipartFile file, String sheetName) throws IOException;
    
    /**
     * Get supported file extensions
     * 
     * @return list of supported extensions (e.g., [".xlsx", ".xls"])
     */
    List<String> getSupportedExtensions();
    
    /**
     * Get maximum allowed file size in bytes
     * 
     * @return maximum file size in bytes
     */
    long getMaxFileSize();
} 