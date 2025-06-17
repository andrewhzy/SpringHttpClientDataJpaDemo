package com.example.springhttpclientdatajpademo.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing task progress
 * Immutable with business logic
 */
@Value
public class TaskProgressVO {
    int totalRows;
    int processedRows;
    int remainingRows;
    BigDecimal percentageComplete;
    ProgressStatus status;
    
    /**
     * Private constructor - use factory methods
     */
    private TaskProgressVO(int totalRows, int processedRows) {
        this.totalRows = totalRows;
        this.processedRows = processedRows;
        this.remainingRows = totalRows - processedRows;
        this.percentageComplete = calculatePercentage(totalRows, processedRows);
        this.status = determineStatus();
    }
    
    /**
     * Factory method to create progress from counts
     */
    public static TaskProgressVO of(int totalRows, int processedRows) {
        if (totalRows < 0 || processedRows < 0) {
            throw new IllegalArgumentException("Row counts cannot be negative");
        }
        if (processedRows > totalRows) {
            throw new IllegalArgumentException("Processed rows cannot exceed total rows");
        }
        return new TaskProgressVO(totalRows, processedRows);
    }
    
    /**
     * Factory method for completed task
     */
    public static TaskProgressVO completed(int totalRows) {
        return new TaskProgressVO(totalRows, totalRows);
    }
    
    /**
     * Factory method for new task
     */
    public static TaskProgressVO notStarted(int totalRows) {
        return new TaskProgressVO(totalRows, 0);
    }
    
    /**
     * Create new progress with additional processed rows
     */
    public TaskProgressVO withAdditionalProcessed(int additionalRows) {
        return TaskProgressVO.of(totalRows, processedRows + additionalRows);
    }
    
    /**
     * Check if task is complete
     */
    public boolean isComplete() {
        return processedRows >= totalRows;
    }
    
    /**
     * Check if task has started
     */
    public boolean hasStarted() {
        return processedRows > 0;
    }
    
    /**
     * Get estimated time remaining (if processing rate is known)
     */
    public long getEstimatedSecondsRemaining(double rowsPerSecond) {
        if (rowsPerSecond <= 0 || isComplete()) {
            return 0;
        }
        return Math.round(remainingRows / rowsPerSecond);
    }
    
    private BigDecimal calculatePercentage(int total, int processed) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(processed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
    
    private ProgressStatus determineStatus() {
        if (processedRows == 0) {
            return ProgressStatus.NOT_STARTED;
        } else if (processedRows >= totalRows) {
            return ProgressStatus.COMPLETED;
        } else {
            return ProgressStatus.IN_PROGRESS;
        }
    }
    
    public enum ProgressStatus {
        NOT_STARTED,
        IN_PROGRESS, 
        COMPLETED
    }
} 