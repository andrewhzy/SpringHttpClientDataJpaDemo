package com.example.springhttpclientdatajpademo.application.exception;

/**
 * Exception thrown when file processing fails
 * 
 * Following Effective Java Item 72: Favor the use of standard exceptions
 * This extends RuntimeException to avoid forcing clients to handle checked exceptions
 */
public final class FileProcessingException extends RuntimeException {
    
    /**
     * Constructs a FileProcessingException with the specified detail message
     * 
     * @param message the detail message
     */
    public FileProcessingException(final String message) {
        super(message);
    }
    
    /**
     * Constructs a FileProcessingException with the specified detail message and cause
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public FileProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
} 