package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implementation of ExcelParsingService
 * Infrastructure service for parsing Excel files using Apache POI
 */
@Service
@Slf4j
public class ExcelParsingServiceImpl implements ExcelParsingService {
    
    // Required column names (case-insensitive)
    private static final String QUESTION_COLUMN = "question";
    private static final String GOLDEN_ANSWER_COLUMN = "golden_answer";
    private static final String GOLDEN_CITATIONS_COLUMN = "golden_citations";
    
    @Override
    public Map<String, List<ChatEvaluationInput>> parseExcelFile(MultipartFile file) throws IOException {
        log.info("Parsing Excel file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        
        // Validate file first
        validateExcelFile(file);
        
        Map<String, List<ChatEvaluationInput>> result = new HashMap<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename())) {
            
            int numberOfSheets = workbook.getNumberOfSheets();
            log.info("Found {} sheets in Excel file", numberOfSheets);
            
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                
                log.info("Processing sheet: {}", sheetName);
                
                if (isValidChatEvaluationSheet(sheet)) {
                    List<ChatEvaluationInput> sheetData = parseSheet(sheet);
                    if (!sheetData.isEmpty()) {
                        result.put(sheetName, sheetData);
                        log.info("Successfully parsed {} records from sheet: {}", sheetData.size(), sheetName);
                    } else {
                        log.warn("Sheet {} contains no valid data rows", sheetName);
                    }
                } else {
                    log.warn("Skipping sheet {} - invalid format or missing required columns", sheetName);
                }
            }
            
            if (result.isEmpty()) {
                throw new IllegalArgumentException("No valid chat evaluation sheets found in Excel file");
            }
            
            log.info("Successfully parsed {} sheets with total {} records", result.size(), 
                    result.values().stream().mapToInt(List::size).sum());
            
            return result;
        }
    }
    
    @Override
    public void validateExcelFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        // Check file extension
        if (!getSupportedExtensions().stream().anyMatch(ext -> 
                filename.toLowerCase().endsWith(ext.toLowerCase()))) {
            throw new IllegalArgumentException(
                String.format("Unsupported file type. Allowed types: %s", getSupportedExtensions()));
        }
        
        // Check file size
        if (file.getSize() > getMaxFileSize()) {
            throw new IllegalArgumentException(
                String.format("File size %d bytes exceeds maximum limit of %d bytes", 
                        file.getSize(), getMaxFileSize()));
        }
        
        // Try to open the file to validate it's a valid Excel file
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, filename)) {
            
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Excel file contains no sheets");
            }
            
            log.info("Excel file validation passed for: {}", filename);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid Excel file format: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> getSheetNames(MultipartFile file) throws IOException {
        List<String> sheetNames = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename())) {
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetAt(i).getSheetName());
            }
        }
        
        return sheetNames;
    }
    
    @Override
    public boolean isValidChatEvaluationSheet(String sheetName, MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename())) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return false;
            }
            
            return isValidChatEvaluationSheet(sheet);
        }
    }
    
    @Override
    public List<ChatEvaluationInput> parseSheet(MultipartFile file, String sheetName) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename())) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            
            return parseSheet(sheet);
        }
    }
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(".xlsx", ".xls");
    }
    
    @Override
    public long getMaxFileSize() {
        return 50 * 1024 * 1024; // 50MB
    }
    
    // Private helper methods
    
    private Workbook createWorkbook(InputStream inputStream, String filename) throws IOException {
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (filename.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filename);
        }
    }
    
    private boolean isValidChatEvaluationSheet(Sheet sheet) {
        if (sheet == null || sheet.getPhysicalNumberOfRows() < 1) {
            return false;
        }
        
        // Get header row
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow == null) {
            return false;
        }
        
        // Find required columns
        Map<String, Integer> columnIndices = findColumnIndices(headerRow);
        
        return columnIndices.containsKey(QUESTION_COLUMN) &&
               columnIndices.containsKey(GOLDEN_ANSWER_COLUMN) &&
               columnIndices.containsKey(GOLDEN_CITATIONS_COLUMN);
    }
    
    private List<ChatEvaluationInput> parseSheet(Sheet sheet) {
        List<ChatEvaluationInput> results = new ArrayList<>();
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            log.warn("Sheet {} has no data rows", sheet.getSheetName());
            return results;
        }
        
        // Get header row and find column indices
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        Map<String, Integer> columnIndices = findColumnIndices(headerRow);
        
        if (!columnIndices.containsKey(QUESTION_COLUMN) ||
            !columnIndices.containsKey(GOLDEN_ANSWER_COLUMN) ||
            !columnIndices.containsKey(GOLDEN_CITATIONS_COLUMN)) {
            throw new IllegalArgumentException(
                String.format("Sheet %s is missing required columns. Expected: %s, %s, %s", 
                        sheet.getSheetName(), QUESTION_COLUMN, GOLDEN_ANSWER_COLUMN, GOLDEN_CITATIONS_COLUMN));
        }
        
        int questionCol = columnIndices.get(QUESTION_COLUMN);
        int goldenAnswerCol = columnIndices.get(GOLDEN_ANSWER_COLUMN);
        int goldenCitationsCol = columnIndices.get(GOLDEN_CITATIONS_COLUMN);
        
        // Parse data rows
        for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            
            try {
                String question = getCellValueAsString(row.getCell(questionCol));
                String goldenAnswer = getCellValueAsString(row.getCell(goldenAnswerCol));
                String goldenCitationsStr = getCellValueAsString(row.getCell(goldenCitationsCol));
                
                // Skip empty rows
                if (!StringUtils.hasText(question) && !StringUtils.hasText(goldenAnswer)) {
                    continue;
                }
                
                // Validate required fields
                if (!StringUtils.hasText(question)) {
                    log.warn("Skipping row {} in sheet {}: missing question", rowIndex + 1, sheet.getSheetName());
                    continue;
                }
                
                if (!StringUtils.hasText(goldenAnswer)) {
                    log.warn("Skipping row {} in sheet {}: missing golden_answer", rowIndex + 1, sheet.getSheetName());
                    continue;
                }
                
                // Parse citations (can be comma-separated or JSON-like format)
                List<String> citations = parseCitations(goldenCitationsStr);
                
                ChatEvaluationInput input = ChatEvaluationInput.builder()
                        .question(question.trim())
                        .goldenAnswer(goldenAnswer.trim())
                        .goldenCitations(citations)
                        .build();
                
                results.add(input);
                
            } catch (Exception e) {
                log.warn("Error parsing row {} in sheet {}: {}", rowIndex + 1, sheet.getSheetName(), e.getMessage());
                // Continue processing other rows
            }
        }
        
        return results;
    }
    
    private Map<String, Integer> findColumnIndices(Row headerRow) {
        Map<String, Integer> columnIndices = new HashMap<>();
        
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            if (cell != null) {
                String cellValue = getCellValueAsString(cell);
                if (StringUtils.hasText(cellValue)) {
                    String normalizedValue = cellValue.trim().toLowerCase();
                    
                    // Map common variations
                    if (normalizedValue.equals("question") || normalizedValue.equals("questions")) {
                        columnIndices.put(QUESTION_COLUMN, cellIndex);
                    } else if (normalizedValue.equals("golden_answer") || normalizedValue.equals("golden answer") ||
                               normalizedValue.equals("goldenanswer") || normalizedValue.equals("answer")) {
                        columnIndices.put(GOLDEN_ANSWER_COLUMN, cellIndex);
                    } else if (normalizedValue.equals("golden_citations") || normalizedValue.equals("golden citations") ||
                               normalizedValue.equals("goldencitations") || normalizedValue.equals("citations")) {
                        columnIndices.put(GOLDEN_CITATIONS_COLUMN, cellIndex);
                    }
                }
            }
        }
        
        return columnIndices;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // Format as integer if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        yield String.valueOf((long) numericValue);
                    } else {
                        yield String.valueOf(numericValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield getCellValueAsString(cell.getCachedFormulaResultType(), cell);
                } catch (Exception e) {
                    yield cell.getCellFormula();
                }
            }
            case BLANK -> "";
            default -> "";
        };
    }
    
    private String getCellValueAsString(CellType cellType, Cell cell) {
        return switch (cellType) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
    
    private List<String> parseCitations(String citationsStr) {
        if (!StringUtils.hasText(citationsStr)) {
            return new ArrayList<>();
        }
        
        // Handle different citation formats
        String trimmed = citationsStr.trim();
        
        // If it looks like JSON array, try to parse it
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                // Simple JSON array parsing for citations
                String content = trimmed.substring(1, trimmed.length() - 1);
                if (content.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                
                List<String> citations = new ArrayList<>();
                String[] parts = content.split(",");
                for (String part : parts) {
                    String citation = part.trim();
                    // Remove quotes if present
                    if (citation.startsWith("\"") && citation.endsWith("\"")) {
                        citation = citation.substring(1, citation.length() - 1);
                    }
                    if (StringUtils.hasText(citation)) {
                        citations.add(citation.trim());
                    }
                }
                return citations;
            } catch (Exception e) {
                log.warn("Failed to parse JSON-like citations format: {}", trimmed);
            }
        }
        
        // Fall back to comma-separated parsing
        List<String> citations = new ArrayList<>();
        String[] parts = trimmed.split(",");
        for (String part : parts) {
            String citation = part.trim();
            if (StringUtils.hasText(citation)) {
                citations.add(citation);
            }
        }
        
        return citations;
    }
} 