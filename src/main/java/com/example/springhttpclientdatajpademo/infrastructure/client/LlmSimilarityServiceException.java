package com.example.springhttpclientdatajpademo.infrastructure.client;

/**
 * Exception thrown when LLM Similarity Service API calls fail
 * 
 * Following Effective Java principles:
 * - Item 72: Favor the use of standard exceptions
 * - Item 75: Include failure-capture information in detail messages
 */
public class LlmSimilarityServiceException extends RuntimeException {
    
    private final String service = "LLM Similarity Service";
    private final Integer httpStatus;
    private final String apiErrorCode;
    
    public LlmSimilarityServiceException(String message) {
        super(message);
        this.httpStatus = null;
        this.apiErrorCode = null;
    }
    
    public LlmSimilarityServiceException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.apiErrorCode = null;
    }
    
    public LlmSimilarityServiceException(String message, Integer httpStatus, String apiErrorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.apiErrorCode = apiErrorCode;
    }
    
    public LlmSimilarityServiceException(String message, Throwable cause, Integer httpStatus, String apiErrorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.apiErrorCode = apiErrorCode;
    }
    
    public String getService() {
        return service;
    }
    
    public Integer getHttpStatus() {
        return httpStatus;
    }
    
    public String getApiErrorCode() {
        return apiErrorCode;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(": ");
        sb.append(getMessage());
        
        if (httpStatus != null) {
            sb.append(" (HTTP ").append(httpStatus).append(")");
        }
        
        if (apiErrorCode != null) {
            sb.append(" [Error: ").append(apiErrorCode).append("]");
        }
        
        return sb.toString();
    }
} 