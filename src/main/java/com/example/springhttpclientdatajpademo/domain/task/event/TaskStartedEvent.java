package com.example.springhttpclientdatajpademo.domain.task.event;

import java.time.LocalDateTime;

/**
 * Domain event raised when a task starts processing
 */
public record TaskStartedEvent(
    Long taskId,
    LocalDateTime startedAt,
    Integer totalRows
) {
} 