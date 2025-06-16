package com.example.springhttpclientdatajpademo.service;

import com.example.springhttpclientdatajpademo.entity.ChatEvaluationInput;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Excel file parsing and validation
 * This service handles immediate parsing of Excel files during upload
 */
public interface ExcelParsingService {
    
    /**
     * Parse Excel file and extract all sheets with their data
     * 
     * @param file the uploaded Excel file
     * @return Map where key is sheet name and value is list of parsed input data
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if file format is invalid
     */
    Map<String, List<ChatEvaluationInput>> parseExcelFile(MultipartFile file) throws IOException;
    
    /**
     * Validate Excel file format and basic structure
     * 
     * @param file the uploaded Excel file
     * @throws IllegalArgumentException if file is invalid
     */
    void validateExcelFile(MultipartFile file);
    
    /**
     * Check if a sheet contains required columns for chat evaluation
     * Required columns: question, golden_answer, golden_citations
     * 
     * @param sheetName the name of the sheet to validate
     * @param file the Excel file
     * @return true if sheet has required columns
     * @throws IOException if file cannot be read
     */
    boolean isValidChatEvaluationSheet(String sheetName, MultipartFile file) throws IOException;
    
    /**
     * Parse a specific sheet from Excel file into ChatEvaluationInput objects
     * 
     * @param sheetName the name of the sheet to parse
     * @param file the Excel file
     * @return list of parsed ChatEvaluationInput objects
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if sheet structure is invalid
     */
    List<ChatEvaluationInput> parseSheet(String sheetName, MultipartFile file) throws IOException;
    
    /**
     * Get list of sheet names from Excel file
     * 
     * @param file the Excel file
     * @return list of sheet names
     * @throws IOException if file cannot be read
     */
    List<String> getSheetNames(MultipartFile file) throws IOException;
    
    /**
     * Validate that required columns exist in a sheet
     * 
     * @param sheetName the sheet name to validate
     * @param file the Excel file
     * @throws IllegalArgumentException if required columns are missing
     * @throws IOException if file cannot be read
     */
    void validateRequiredColumns(String sheetName, MultipartFile file) throws IOException;
    
    /**
     * Extract metadata from additional columns in Excel sheet
     * 
     * @param sheetName the sheet name
     * @param file the Excel file
     * @param rowIndex the row index to extract metadata from
     * @return metadata object containing additional column data
     * @throws IOException if file cannot be read
     */
    Object extractRowMetadata(String sheetName, MultipartFile file, int rowIndex) throws IOException;
} 