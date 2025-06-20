package com.example.springhttpclientdatajpademo.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Converter to handle List<String> to JSON string conversion
 * 
 * This is needed because MariaDB doesn't have native JSON support like MySQL 5.7+.
 * Without this converter, JPA would use Java serialization which produces binary data
 * that causes encoding errors in MariaDB.
 * 
 * Based on Effective Java principles:
 * - Item 75: Include failure-capture information in detail messages
 * - Item 76: Strive for failure atomicity (null-safe conversions)
 */
@Converter(autoApply = false) // Don't auto-apply, use @Convert annotation explicitly
@Slf4j
public class StringListConverter implements AttributeConverter<List<String>, String> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    
    /**
     * Convert List<String> to JSON string for database storage
     */
    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return "[]"; // Empty JSON array for null/empty lists
        }
        
        try {
            String json = objectMapper.writeValueAsString(stringList);
            log.trace("Converted list {} to JSON: {}", stringList, json);
            return json;
        } catch (JsonProcessingException e) {
            // Item 75: Include failure-capture information
            log.error("Failed to convert list to JSON: {}", stringList, e);
            throw new IllegalArgumentException(
                String.format("Failed to convert list %s to JSON: %s", stringList, e.getMessage()), e);
        }
    }
    
    /**
     * Convert JSON string from database to List<String>
     */
    @Override
    public List<String> convertToEntityAttribute(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>(); // Return empty list for null/empty JSON
        }
        
        try {
            List<String> result = objectMapper.readValue(jsonString, STRING_LIST_TYPE);
            log.trace("Converted JSON {} to list: {}", jsonString, result);
            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            // Item 75: Include failure-capture information  
            log.error("Failed to convert JSON to list: {}", jsonString, e);
            throw new IllegalArgumentException(
                String.format("Failed to convert JSON '%s' to List<String>: %s", jsonString, e.getMessage()), e);
        }
    }
} 