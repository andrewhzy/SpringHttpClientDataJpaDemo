package com.example.springhttpclientdatajpademo.application.exception;

/**
 * Exception thrown when task validation fails
 * 
 * Following Effective Java Item 72: Favor the use of standard exceptions
 * This extends RuntimeException to avoid forcing clients to handle checked exceptions
 */
public final class TaskValidationException extends RuntimeException {
    
    /**
     * Constructs a TaskValidationException with the specified detail message
     * 
     * @param message the detail message
     */
    public TaskValidationException(final String message) {
        super(message);
    }
    
    /**
     * Constructs a TaskValidationException with the specified detail message and cause
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public TaskValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
} 