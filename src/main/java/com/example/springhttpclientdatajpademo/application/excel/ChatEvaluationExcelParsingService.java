package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.TaskItem;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskItem;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ExcelParsingService for chat evaluation tasks
 * Implements API specification requirements for POST /rest/api/v1/tasks
 */
@Service()
@Slf4j
public class ChatEvaluationExcelParsingService implements ExcelParsingService<ChatEvaluationTaskItem> {

    // API specification constants
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_SHEETS = 20;
    private static final int MAX_ROWS_PER_SHEET = 1000;
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".xlsx", ".xls");

    // Required column names (case-insensitive)
    private static final String QUESTION_COLUMN = "question";
    private static final String GOLDEN_ANSWER_COLUMN = "golden_answer";
    private static final String GOLDEN_CITATIONS_COLUMN = "golden_citations";
    private final TaskType TASK_TYPE = Task.TaskType.CHAT_EVALUATION;


    /**
     * Parse Excel file and return data separated by sheet names
     * This method preserves the sheet structure for creating separate tasks per sheet
     *
     * @param file the Excel file to parse
     * @return Map where key is sheet name and value is list of parsed inputs from that sheet
     */
    @Override
    public Map<String, List<ChatEvaluationTaskItem>> parseExcelFile(MultipartFile file) {
        log.info("Parsing Excel file by sheets: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

        // Validate file first
        validateExcelFile(file);

        Map<String, List<ChatEvaluationTaskItem>> result = new LinkedHashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename())) {

            int numberOfSheets = workbook.getNumberOfSheets();
            log.info("Found {} sheets in Excel file", numberOfSheets);

            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                log.info("Processing sheet: {}", sheetName);

                if (isValidChatEvaluationSheet(sheet)) {
                    List<ChatEvaluationTaskItem> sheetData = parseSheet(sheet);
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
                throw new IllegalArgumentException("No valid chat evaluation sheets found in Excel file. " +
                        "Excel must contain sheets with required columns: question, golden_answer, golden_citations");
            }

            log.info("Successfully parsed {} records from {} sheets", 
                    result.values().stream().mapToInt(List::size).sum(), result.size());

            return result;

        } catch (IOException e) {
            log.error("Failed to parse Excel file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("Failed to parse Excel file: " + e.getMessage(), e);
        }
    }

    @Override
    public void validateExcelFile(MultipartFile file) {
        // Basic file validation
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        // File size validation (API spec: max 50MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size %d bytes exceeds maximum limit of %d bytes (50MB)",
                            file.getSize(), MAX_FILE_SIZE));
        }

        // File extension validation (API spec: .xlsx or .xls)
        if (!SUPPORTED_EXTENSIONS.stream().anyMatch(ext ->
                filename.toLowerCase().endsWith(ext.toLowerCase()))) {
            throw new IllegalArgumentException(
                    String.format("File must be Excel format (.xlsx or .xls). Found: %s",
                            getFileExtension(filename)));
        }

        // Excel structure validation
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, filename)) {

            int numberOfSheets = workbook.getNumberOfSheets();

            if (numberOfSheets == 0) {
                throw new IllegalArgumentException("Excel file contains no sheets");
            }

            // API spec: max 20 sheets per file
            if (numberOfSheets > MAX_SHEETS) {
                throw new IllegalArgumentException(
                        String.format("Excel file contains %d sheets, maximum allowed is %d",
                                numberOfSheets, MAX_SHEETS));
            }

            // Validate each sheet
            boolean hasValidSheet = false;
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);

                // API spec: max 1000 rows per sheet
                if (sheet.getPhysicalNumberOfRows() > MAX_ROWS_PER_SHEET + 1) { // +1 for header
                    throw new IllegalArgumentException(
                            String.format("Sheet '%s' contains %d rows, maximum allowed is %d",
                                    sheet.getSheetName(), sheet.getPhysicalNumberOfRows() - 1, MAX_ROWS_PER_SHEET));
                }

                if (isValidChatEvaluationSheet(sheet)) {
                    hasValidSheet = true;
                }
            }

            if (!hasValidSheet) {
                throw new IllegalArgumentException(
                        "Excel file must contain at least one sheet with required columns: question, golden_answer, golden_citations");
            }

            log.info("Excel file validation passed for: {}", filename);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid Excel file format: " + e.getMessage(), e);
        }
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

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
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

    private List<ChatEvaluationTaskItem> parseSheet(Sheet sheet) {
        List<ChatEvaluationTaskItem> results = new ArrayList<>();

        if (sheet.getPhysicalNumberOfRows() < 2) {
            return results; // No data rows
        }

        // Get header row and find column indices
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        Map<String, Integer> columnIndices = findColumnIndices(headerRow);

        // Get column indices for required fields
        Integer questionIndex = columnIndices.get(QUESTION_COLUMN);
        Integer answerIndex = columnIndices.get(GOLDEN_ANSWER_COLUMN);
        Integer citationsIndex = columnIndices.get(GOLDEN_CITATIONS_COLUMN);

        // Parse data rows
        for (int rowNum = sheet.getFirstRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;

            try {
                String question = getCellValueAsString(row.getCell(questionIndex));
                String goldenAnswer = getCellValueAsString(row.getCell(answerIndex));
                String citationsStr = getCellValueAsString(row.getCell(citationsIndex));

                // Skip rows with empty required fields
                if (!StringUtils.hasText(question) || !StringUtils.hasText(goldenAnswer)) {
                    log.warn("Skipping row {} - missing required fields", rowNum + 1);
                    continue;
                }

                List<String> citations = parseCitations(citationsStr);

                ChatEvaluationTaskItem input = ChatEvaluationTaskItem.builder()
                        .question(question.trim())
                        .goldenAnswer(goldenAnswer.trim())
                        .goldenCitations(citations)
                        .build();

                results.add(input);

            } catch (Exception e) {
                log.warn("Error parsing row {}: {}", rowNum + 1, e.getMessage());
                // Continue processing other rows
            }
        }

        return results;
    }

    private Map<String, Integer> findColumnIndices(Row headerRow) {
        Map<String, Integer> columnIndices = new HashMap<>();

        for (Cell cell : headerRow) {
            String columnName = getCellValueAsString(cell);
            if (StringUtils.hasText(columnName)) {
                String normalizedName = columnName.toLowerCase().trim();

                if (normalizedName.equals(QUESTION_COLUMN)) {
                    columnIndices.put(QUESTION_COLUMN, cell.getColumnIndex());
                } else if (normalizedName.equals(GOLDEN_ANSWER_COLUMN)) {
                    columnIndices.put(GOLDEN_ANSWER_COLUMN, cell.getColumnIndex());
                } else if (normalizedName.equals(GOLDEN_CITATIONS_COLUMN)) {
                    columnIndices.put(GOLDEN_CITATIONS_COLUMN, cell.getColumnIndex());
                }
            }
        }

        return columnIndices;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return getCellValueAsString(cell.getCellType(), cell);
    }

    private String getCellValueAsString(CellType cellType, Cell cell) {
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getCellValueAsString(cell.getCachedFormulaResultType(), cell);
            default:
                return "";
        }
    }

    private List<String> parseCitations(String citationsStr) {
        if (!StringUtils.hasText(citationsStr)) {
            return new ArrayList<>();
        }

        // Parse citations separated by comma, semicolon, or newline
        return Arrays.stream(citationsStr.split("[,;\n]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    @Override
    public TaskType getTaskType() {
        return Task.TaskType.CHAT_EVALUATION;
    }
} 