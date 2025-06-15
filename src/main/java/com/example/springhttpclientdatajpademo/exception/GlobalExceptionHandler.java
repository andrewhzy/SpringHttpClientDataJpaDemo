package com.example.springhttpclientdatajpademo.exception;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("INVALID_REQUEST")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .traceId(UUID.randomUUID().toString())
            .build();
            
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        log.error("Operation not implemented: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code("NOT_IMPLEMENTED")
            .message("This feature is not yet implemented")
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .traceId(UUID.randomUUID().toString())
            .build();
            
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRuntimeException(RuntimeException ex) {
        log.error("Internal server error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An internal server error occurred")
            .timestamp(LocalDateTime.now())
            .traceId(UUID.randomUUID().toString())
            .build();
            
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    @Data
    @Builder
    public static class ErrorResponse {
        private String code;
        private String message;
        private String details;
        private LocalDateTime timestamp;
        private String traceId;
    }
}