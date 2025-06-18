package com.example.springhttpclientdatajpademo.domain.task.event;

import java.time.LocalDateTime;

/**
 * Domain event raised when a task completes successfully
 */
public record TaskCompletedEvent(
    Long taskId,
    LocalDateTime completedAt,
    Integer totalInputs,
    Integer totalOutputs
) {
} 