package com.example.springhttpclientdatajpademo.service;

import com.example.springhttpclientdatajpademo.entity.ChatEvaluationInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Placeholder implementation of ExcelParsingService
 * All methods throw UnsupportedOperationException until properly implemented with Apache POI
 */
@Service
@Slf4j
public class ExcelParsingServiceImpl implements ExcelParsingService {
    
    @Override
    public Map<String, List<ChatEvaluationInput>> parseExcelFile(MultipartFile file) throws IOException {
        log.error("ExcelParsingService.parseExcelFile() not yet implemented");
        throw new UnsupportedOperationException("Excel parsing not yet implemented - please implement with Apache POI");
    }
    
    @Override
    public void validateExcelFile(MultipartFile file) {
        log.error("ExcelParsingService.validateExcelFile() not yet implemented");
        throw new UnsupportedOperationException("Excel validation not yet implemented - please implement with Apache POI");
    }
    
    @Override
    public boolean isValidChatEvaluationSheet(String sheetName, MultipartFile file) throws IOException {
        log.error("ExcelParsingService.isValidChatEvaluationSheet() not yet implemented");
        throw new UnsupportedOperationException("Sheet validation not yet implemented - please implement with Apache POI");
    }
    
    @Override
    public List<ChatEvaluationInput> parseSheet(String sheetName, MultipartFile file) throws IOException {
        log.error("ExcelParsingService.parseSheet() not yet implemented");
        throw new UnsupportedOperationException("Sheet parsing not yet implemented - please implement with Apache POI");
    }
    
    @Override
    public List<String> getSheetNames(MultipartFile file) throws IOException {
        log.error("ExcelParsingService.getSheetNames() not yet implemented");
        throw new UnsupportedOperationException("Sheet names extraction not yet implemented - please implement with Apache POI");
    }
    
    @Override
    public void validateRequiredColumns(String sheetName, MultipartFile file) throws IOException {
        log.error("ExcelParsingService.validateRequiredColumns() not yet implemented");
        throw new UnsupportedOperationException("Column validation not yet implemented - please implement with Apache POI");
    }
    
    @Override
    public Object extractRowMetadata(String sheetName, MultipartFile file, int rowIndex) throws IOException {
        log.error("ExcelParsingService.extractRowMetadata() not yet implemented");
        throw new UnsupportedOperationException("Metadata extraction not yet implemented - please implement with Apache POI");
    }
} 