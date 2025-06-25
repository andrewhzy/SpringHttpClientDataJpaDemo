package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.service.ChatEvaluationService;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Background processor for chat evaluation tasks
 * Implements the task processing flow from sequence diagrams
 * 
 * Following Domain-Driven Design principles:
 * - Application service coordinating domain services
 * - Orchestrates business logic across aggregates
 * - Handles cross-cutting concerns like scheduling and async processing
 * 
 * Key responsibilities:
 * - FIFO task queue processing
 * - Progress tracking and status management
 * - Error handling and recovery
 * - Graceful cancellation support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEvaluationBackgroundProcessor {
    
    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ChatEvaluationService chatEvaluationService;
    
    /**
     * Scheduled method to check for queued chat evaluation tasks
     * Runs every 30 seconds to process tasks in FIFO order
     * 
     * Following the sequence diagram: "SELECT id, user_id, row_count FROM tasks 
     * WHERE task_type = 'chat-evaluation' AND task_status = 'queueing' 
     * ORDER BY created_at ASC LIMIT 1"
     */
    @Scheduled(fixedDelayString = "${app.background-processor.check-interval-ms:30000}")
    public void processQueuedTasks() {
        log.debug("Checking for queued chat evaluation tasks...");
        
        try {
            // Find the oldest queued chat evaluation task (FIFO)
            final Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").ascending());
            final List<Task> queuedTasks = taskRepository.findByTaskTypeAndTaskStatus(
                Task.TaskType.CHAT_EVALUATION, 
                Task.TaskStatus.QUEUEING, 
                pageable
            );
            
            if (queuedTasks.isEmpty()) {
                log.debug("No queued chat evaluation tasks found");
                return;
            }
            
            final Task task = queuedTasks.get(0);
            log.info("Found queued task: {} with {} rows to process", 
                    task.getId(), task.getRowCount());
            
            // Process the task asynchronously
            processTaskAsync(task);
            
        } catch (Exception e) {
            log.error("Error during task queue processing", e);
        }
    }
    
    /**
     * Asynchronously process a chat evaluation task
     * Implements the complete processing flow from sequence diagrams
     * 
     * @param task the task to process
     */
    @Async("chatEvaluationExecutor")
    public void processTaskAsync(final Task task) {
        log.info("Starting async processing for task: {}", task.getId());
        
        try {
            // Step 1: Mark task as processing and validate
            if (!markTaskAsProcessing(task)) {
                log.warn("Task {} is no longer in queueing status, skipping", task.getId());
                return;
            }
            
            // Step 2: Get all input data for this task
            final List<ChatEvaluationInput> inputs = getTaskInputs(task);
            
            if (inputs.isEmpty()) {
                log.error("No input data found for task: {}", task.getId());
                markTaskAsFailed(task, "No input data available for processing");
                return;
            }
            
            log.info("Processing task {} with {} input rows", task.getId(), inputs.size());
            
            // Step 3: Process each input row sequentially
            int processedCount = task.getProcessedRows();
            
            for (final ChatEvaluationInput input : inputs) {
                // Check if task was cancelled during processing
                if (isTaskCancelled(task)) {
                    log.info("Task {} was cancelled during processing. Stopping at row {}",
                            task.getId(), processedCount);
                    updateTaskProgress(task, processedCount);
                    return;
                }
                
                // Skip already processed inputs (for resume capability)
                if (chatEvaluationService.isAlreadyEvaluated(input)) {
                    log.debug("Input {} already evaluated, skipping", input.getId());
                    processedCount++;
                    continue;
                }
                
                try {
                    // Process the individual input
                    log.debug("Processing input {} for task {}", input.getId(), task.getId());
                    
                    final ChatEvaluationOutput output = chatEvaluationService.evaluateInput(input);
                    
                    processedCount++;
                    
                    // Update progress after each successful processing
                    updateTaskProgress(task, processedCount);
                    
                    log.debug("Completed processing input {}. Progress: {}/{} ({}%)",
                            input.getId(), processedCount, inputs.size(),
                            task.getProgressPercentage());
                    
                } catch (ChatEvaluationService.ChatEvaluationException e) {
                    // Handle evaluation failures - mark task as failed
                    final String errorMessage = String.format(
                        "Chat evaluation failed on input %d (ID: %d): %s",
                        processedCount + 1, input.getId(), e.getMessage()
                    );
                    
                    log.error("Processing failed for task {}: {}", task.getId(), errorMessage, e);
                    markTaskAsFailed(task, errorMessage);
                    updateTaskProgress(task, processedCount);
                    return;
                    
                } catch (Exception e) {
                    // Handle unexpected errors
                    final String errorMessage = String.format(
                        "Unexpected error processing input %d (ID: %d): %s",
                        processedCount + 1, input.getId(), e.getMessage()
                    );
                    
                    log.error("Unexpected error for task {}: {}", task.getId(), errorMessage, e);
                    markTaskAsFailed(task, errorMessage);
                    updateTaskProgress(task, processedCount);
                    return;
                }
            }
            
            // Step 4: Mark task as completed
            markTaskAsCompleted(task);
            log.info("Successfully completed processing task {} with {} rows",
                    task.getId(), processedCount);
            
        } catch (Exception e) {
            log.error("Critical error during task processing for task {}", task.getId(), e);
            markTaskAsFailed(task, "Critical processing error: " + e.getMessage());
        }
    }
    
    /**
     * Mark task as processing if it's still in queueing status
     * Implements atomic status change to prevent race conditions
     * Uses REQUIRES_NEW to ensure transaction works in async context
     * 
     * @param task the task to mark as processing
     * @return true if successfully marked as processing
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected boolean markTaskAsProcessing(final Task task) {
        try {
            // Refresh the task from database first
            final Optional<Task> currentTask = taskRepository.findById(task.getId());
            if (currentTask.isEmpty()) {
                log.warn("Task {} not found in database", task.getId());
                return false;
            }
            
            final Task dbTask = currentTask.get();
            if (dbTask.getTaskStatus() != Task.TaskStatus.QUEUEING) {
                log.warn("Task {} is no longer in queueing status: {}", task.getId(), dbTask.getTaskStatus());
                return false;
            }
            
            // Perform atomic update using direct entity manipulation
            dbTask.markAsStarted(); // This sets both status and startedAt timestamp
            taskRepository.save(dbTask);
            
            // Update the passed task entity state for consistency
            task.markAsStarted();
            
            log.info("Marked task {} as processing", task.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error marking task {} as processing", task.getId(), e);
            return false;
        }
    }
    
    /**
     * Get all input data for a task, ordered by ID for consistent processing
     * 
     * @param task the task
     * @return list of input data
     */
    protected List<ChatEvaluationInput> getTaskInputs(final Task task) {
        return inputRepository.findByTaskOrderByIdAsc(task);
    }
    
    /**
     * Check if task was cancelled during processing
     * 
     * @param task the task to check
     * @return true if task is cancelled
     */
    protected boolean isTaskCancelled(final Task task) {
        final Optional<Task> currentTask = taskRepository.findById(task.getId());
        return currentTask.isPresent() && 
               currentTask.get().getTaskStatus() == Task.TaskStatus.CANCELLED;
    }
    
    /**
     * Update task progress with current processed row count
     * Uses REQUIRES_NEW to ensure transaction works in async context
     * 
     * @param task the task to update
     * @param processedRows number of processed rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateTaskProgress(final Task task, final int processedRows) {
        try {
            taskRepository.updateTaskProgress(task.getId(), processedRows);
            task.setProcessedRows(processedRows); // Update entity state
            
        } catch (Exception e) {
            log.error("Error updating progress for task {}", task.getId(), e);
        }
    }
    
    /**
     * Mark task as completed
     * Uses REQUIRES_NEW to ensure transaction works in async context
     * 
     * @param task the task to complete
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void markTaskAsCompleted(final Task task) {
        try {
            task.markAsCompleted();
            taskRepository.save(task);
            log.info("Marked task {} as completed", task.getId());
            
        } catch (Exception e) {
            log.error("Error marking task {} as completed", task.getId(), e);
        }
    }
    
    /**
     * Mark task as failed with error message
     * Uses REQUIRES_NEW to ensure transaction works in async context
     * 
     * @param task the task to mark as failed
     * @param errorMessage the error message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void markTaskAsFailed(final Task task, final String errorMessage) {
        try {
            task.markAsFailed(errorMessage);
            taskRepository.save(task);
            log.error("Marked task {} as failed: {}", task.getId(), errorMessage);
            
        } catch (Exception e) {
            log.error("Error marking task {} as failed", task.getId(), e);
        }
    }
} 