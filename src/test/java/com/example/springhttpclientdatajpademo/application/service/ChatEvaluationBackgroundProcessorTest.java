package com.example.springhttpclientdatajpademo.application.service;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskItem;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskResult;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.service.ChatEvaluationService;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationTaskItemRepository;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test for ChatEvaluationBackgroundProcessor
 * Tests the complete background processing workflow following DDD principles
 */
@ExtendWith(MockitoExtension.class)
class ChatEvaluationBackgroundProcessorTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ChatEvaluationTaskItemRepository taskItemRepository;

    @Mock
    private ChatEvaluationService chatEvaluationService;

    @InjectMocks
    private ChatEvaluationBackgroundProcessor processor;

    private Task testTask;
    private List<ChatEvaluationTaskItem> testTaskItems;

    @BeforeEach
    void setUp() {
        // Create test task
        testTask = Task.builder()
                .id(1L)
                .userId("test-user")
                .filename("test.xlsx")
                .sheetName("Sheet1")
                .taskType(Task.TaskType.CHAT_EVALUATION)
                .taskStatus(Task.TaskStatus.QUEUEING)
                .rowCount(2)
                .processedRows(0)
                .build();

        // Create test inputs
        ChatEvaluationTaskItem input1 = ChatEvaluationTaskItem.builder()
                .id(1L)
                .task(testTask)
                .question("What is AI?")
                .goldenAnswer("AI is artificial intelligence")
                .goldenCitations(Arrays.asList("https://example.com/ai"))
                .build();

        ChatEvaluationTaskItem input2 = ChatEvaluationTaskItem.builder()
                .id(2L)
                .task(testTask)
                .question("What is ML?")
                .goldenAnswer("ML is machine learning")
                .goldenCitations(Arrays.asList("https://example.com/ml"))
                .build();

        testTaskItems = Arrays.asList(input1, input2);
    }

    @Test
    void processQueuedTasks_ShouldProcessTaskWhenQueuedTasksExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").ascending());
        when(taskRepository.findByTaskTypeAndTaskStatus(
                Task.TaskType.CHAT_EVALUATION, 
                Task.TaskStatus.QUEUEING, 
                pageable))
                .thenReturn(Arrays.asList(testTask));

        // When
        processor.processQueuedTasks();

        // Then
        verify(taskRepository).findByTaskTypeAndTaskStatus(
                Task.TaskType.CHAT_EVALUATION, 
                Task.TaskStatus.QUEUEING, 
                pageable);
        // Note: processTaskAsync is async, so we can't directly verify its execution
    }

    @Test
    void processQueuedTasks_ShouldDoNothingWhenNoQueuedTasks() {
        // Given
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").ascending());
        when(taskRepository.findByTaskTypeAndTaskStatus(
                Task.TaskType.CHAT_EVALUATION, 
                Task.TaskStatus.QUEUEING, 
                pageable))
                .thenReturn(Arrays.asList());

        // When
        processor.processQueuedTasks();

        // Then
        verify(taskRepository).findByTaskTypeAndTaskStatus(
                Task.TaskType.CHAT_EVALUATION, 
                Task.TaskStatus.QUEUEING, 
                pageable);
        verifyNoMoreInteractions(taskRepository, taskItemRepository, chatEvaluationService);
    }

    @Test
    void markTaskAsProcessing_ShouldReturnTrueWhenSuccessfulUpdate() {
        // Given
        Task dbTask = Task.builder()
                .id(1L)
                .taskStatus(Task.TaskStatus.QUEUEING)
                .build();
        
        when(taskRepository.findById(testTask.getId()))
                .thenReturn(Optional.of(dbTask));
        when(taskRepository.save(any(Task.class)))
                .thenReturn(dbTask);

        // When
        boolean result = processor.markTaskAsProcessing(testTask);

        // Then
        verify(taskRepository).findById(testTask.getId());
        verify(taskRepository).save(dbTask);
        assert result;
        assert dbTask.getTaskStatus() == Task.TaskStatus.PROCESSING;
    }

    @Test
    void markTaskAsProcessing_ShouldReturnFalseWhenNoUpdate() {
        // Given - Task not found in database
        when(taskRepository.findById(testTask.getId()))
                .thenReturn(Optional.empty());

        // When
        boolean result = processor.markTaskAsProcessing(testTask);

        // Then
        verify(taskRepository).findById(testTask.getId());
        verify(taskRepository, never()).save(any());
        assert !result;
    }

    @Test
    void getTaskItems_ShouldReturnInputsOrderedById() {
        // Given
        when(taskItemRepository.findByTaskOrderByIdAsc(testTask))
                .thenReturn(testTaskItems);

        // When
        List<ChatEvaluationTaskItem> result = processor.getTaskItems(testTask);

        // Then
        verify(taskItemRepository).findByTaskOrderByIdAsc(testTask);
        assert result.size() == 2;
        assert result.get(0).getId().equals(1L);
        assert result.get(1).getId().equals(2L);
    }

    @Test
    void isTaskCancelled_ShouldReturnTrueWhenTaskIsCancelled() {
        // Given
        Task cancelledTask = Task.builder()
                .id(1L)
                .taskStatus(Task.TaskStatus.CANCELLED)
                .build();
        
        when(taskRepository.findById(testTask.getId()))
                .thenReturn(Optional.of(cancelledTask));

        // When
        boolean result = processor.isTaskCancelled(testTask);

        // Then
        verify(taskRepository).findById(testTask.getId());
        assert result;
    }

    @Test
    void isTaskCancelled_ShouldReturnFalseWhenTaskIsNotCancelled() {
        // Given
        when(taskRepository.findById(testTask.getId()))
                .thenReturn(Optional.of(testTask));

        // When
        boolean result = processor.isTaskCancelled(testTask);

        // Then
        verify(taskRepository).findById(testTask.getId());
        assert !result;
    }

    @Test
    void updateTaskProgress_ShouldUpdateProgressSuccessfully() {
        // Given
        int processedRows = 5;

        // When
        processor.updateTaskProgress(testTask, processedRows);

        // Then
        verify(taskRepository).updateTaskProgress(testTask.getId(), processedRows);
        assert testTask.getProcessedRows().equals(processedRows);
    }

    @Test
    void markTaskAsCompleted_ShouldSaveCompletedTask() {
        // When
        processor.markTaskAsCompleted(testTask);

        // Then
        verify(taskRepository).save(testTask);
        assert testTask.getTaskStatus() == Task.TaskStatus.COMPLETED;
        assert testTask.getCompletedAt() != null;
    }

    @Test
    void markTaskAsFailed_ShouldSaveFailedTaskWithErrorMessage() {
        // Given
        String errorMessage = "Test error";

        // When
        processor.markTaskAsFailed(testTask, errorMessage);

        // Then
        verify(taskRepository).save(testTask);
        assert testTask.getTaskStatus() == Task.TaskStatus.FAILED;
        assert testTask.getErrorMessage().equals(errorMessage);
        assert testTask.getFailedAt() != null;
    }

    /**
     * Integration test for the complete processing workflow
     * This test verifies the happy path scenario
     */
    @Test
    void processTaskAsync_ShouldCompleteSuccessfully() {
        // Given - Setup successful processing scenario
        Task dbTask = Task.builder()
                .id(1L)
                .taskStatus(Task.TaskStatus.QUEUEING)
                .build();
        
        when(taskRepository.findById(testTask.getId()))
                .thenReturn(Optional.of(dbTask));
        when(taskRepository.save(any(Task.class)))
                .thenReturn(dbTask);
        when(taskItemRepository.findByTaskOrderByIdAsc(testTask))
                .thenReturn(testTaskItems);
        when(chatEvaluationService.isAlreadyEvaluated(any()))
                .thenReturn(false);
        
        // Mock successful evaluation outputs
        ChatEvaluationTaskResult output1 = mockChatEvaluationTaskResult(testTaskItems.get(0));
        ChatEvaluationTaskResult output2 = mockChatEvaluationTaskResult(testTaskItems.get(1));
        
        when(chatEvaluationService.evaluateInput(testTaskItems.get(0)))
                .thenReturn(output1);
        when(chatEvaluationService.evaluateInput(testTaskItems.get(1)))
                .thenReturn(output2);

        // When
        processor.processTaskAsync(testTask);

        // Then - Verify the complete workflow
        verify(taskRepository, atLeast(1)).findById(testTask.getId());
        verify(taskItemRepository).findByTaskOrderByIdAsc(testTask);
        verify(chatEvaluationService, times(2)).isAlreadyEvaluated(any());
        verify(chatEvaluationService).evaluateInput(testTaskItems.get(0));
        verify(chatEvaluationService).evaluateInput(testTaskItems.get(1));
        verify(taskRepository, times(2)).updateTaskProgress(eq(testTask.getId()), anyInt());
        verify(taskRepository, atLeast(1)).save(any(Task.class));
        
        assert testTask.getTaskStatus() == Task.TaskStatus.COMPLETED;
        assert testTask.getCompletedAt() != null;
    }

    private ChatEvaluationTaskResult mockChatEvaluationTaskResult(ChatEvaluationTaskItem input) {
        return ChatEvaluationTaskResult.builder()
                .id(input.getId())
                .taskItem(input)
                .apiAnswer("Mock API answer")
                .apiCitations(Arrays.asList("https://mock-api.com/citation"))
                .answerSimilarity(BigDecimal.valueOf(0.85))
                .citationSimilarity(BigDecimal.valueOf(0.75))
                .processingTimeMs(1000)
                .build();
    }
} 