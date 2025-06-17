package com.example.springhttpclientdatajpademo.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a task completes successfully
 */
public record TaskCompletedEvent(
    UUID taskId,
    LocalDateTime completedAt,
    Integer totalInputs,
    Integer totalOutputs
) {
} 