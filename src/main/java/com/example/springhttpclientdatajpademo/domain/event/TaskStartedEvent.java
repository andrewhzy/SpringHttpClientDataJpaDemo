package com.example.springhttpclientdatajpademo.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a task starts processing
 */
public record TaskStartedEvent(
    UUID taskId,
    LocalDateTime startedAt,
    Integer totalRows
) {
} 