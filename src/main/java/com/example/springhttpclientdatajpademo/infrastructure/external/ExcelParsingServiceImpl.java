package com.example.springhttpclientdatajpademo.infrastructure.external;

import com.example.springhttpclientdatajpademo.domain.model.ChatEvaluationInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ExcelParsingService
 * Infrastructure service for parsing Excel files
 * TODO: Implement actual Excel parsing logic using Apache POI
 */
@Service
@Slf4j
public class ExcelParsingServiceImpl implements ExcelParsingService {
    
    @Override
    public Map<String, List<ChatEvaluationInput>> parseExcelFile(MultipartFile file) throws IOException {
        log.warn("ExcelParsingService.parseExcelFile() not implemented yet");
        throw new UnsupportedOperationException("Excel parsing not implemented yet");
    }
    
    @Override
    public void validateExcelFile(MultipartFile file) throws IOException {
        log.warn("ExcelParsingService.validateExcelFile() not implemented yet");
        throw new UnsupportedOperationException("Excel validation not implemented yet");
    }
    
    @Override
    public List<String> getSheetNames(MultipartFile file) throws IOException {
        log.warn("ExcelParsingService.getSheetNames() not implemented yet");
        throw new UnsupportedOperationException("Sheet name extraction not implemented yet");
    }
    
    @Override
    public boolean isValidChatEvaluationSheet(String sheetName, MultipartFile file) throws IOException {
        log.warn("ExcelParsingService.isValidChatEvaluationSheet() not implemented yet");
        throw new UnsupportedOperationException("Sheet validation not implemented yet");
    }
    
    @Override
    public List<ChatEvaluationInput> parseSheet(MultipartFile file, String sheetName) throws IOException {
        log.warn("ExcelParsingService.parseSheet() not implemented yet");
        throw new UnsupportedOperationException("Sheet parsing not implemented yet");
    }
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(".xlsx", ".xls");
    }
    
    @Override
    public long getMaxFileSize() {
        return 50 * 1024 * 1024; // 50MB
    }
} 