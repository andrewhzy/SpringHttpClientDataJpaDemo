package com.example.springhttpclientdatajpademo.service;

import com.example.springhttpclientdatajpademo.dto.CreateTaskResponse;
import com.example.springhttpclientdatajpademo.dto.ParsedExcelData;
import com.example.springhttpclientdatajpademo.entity.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.entity.Task;
import com.example.springhttpclientdatajpademo.enums.TaskStatus;
import com.example.springhttpclientdatajpademo.enums.TaskType;
import com.example.springhttpclientdatajpademo.repository.ChatEvaluationInputRepository;
import com.example.springhttpclientdatajpademo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ChatEvaluationInputRepository inputRepository;
    private final ExcelParsingService excelParsingService;

    /**
     * Create tasks from uploaded Excel file
     * @param filePart Uploaded file
     * @param userId User identifier from JWT
     * @return Task creation response
     */
    @Transactional
    public Mono<CreateTaskResponse> createTasks(FilePart filePart, String userId) {
        log.info("Creating tasks for user: {} from file: {}", userId, filePart.filename());
        
        String filename = filePart.filename();
        Flux<DataBuffer> fileContent = filePart.content();
        
        return excelParsingService.validateExcelFile(filename, null)
            .flatMap(isValid -> {
                if (!isValid) {
                    return Mono.error(new IllegalArgumentException("Invalid Excel file format"));
                }
                return excelParsingService.parseExcelFile(fileContent, filename);
            })
            .flatMap(parsedData -> processExcelData(parsedData, userId))
            .doOnSuccess(response -> log.info("Successfully created {} tasks for upload batch: {}", 
                response.getTotalTasks(), response.getUploadBatchId()))
            .doOnError(error -> log.error("Failed to create tasks from file: {}", filename, error));
    }

    /**
     * Process parsed Excel data and create tasks
     */
    private Mono<CreateTaskResponse> processExcelData(ParsedExcelData parsedData, String userId) {
        UUID uploadBatchId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        return Flux.fromIterable(parsedData.getSheets())
            .filter(sheet -> sheet.getTaskType() == TaskType.CHAT_EVALUATION)
            .flatMap(sheetData -> {
                // Create task for each valid sheet
                Task task = Task.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .filename(parsedData.getFilename())
                    .sheetName(sheetData.getSheetName())
                    .taskType(sheetData.getTaskType())
                    .taskStatus(TaskStatus.QUEUEING)
                    .uploadBatchId(uploadBatchId)
                    .rowCount(sheetData.getRowCount())
                    .processedRows(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
                
                return taskRepository.save(task)
                    .flatMap(savedTask -> 
                        saveInputData(savedTask, sheetData)
                            .then(Mono.just(savedTask))
                    );
            })
            .collectList()
            .map(tasks -> CreateTaskResponse.builder()
                .uploadBatchId(uploadBatchId)
                .tasks(tasks.stream()
                    .map(task -> CreateTaskResponse.TaskSummary.builder()
                        .taskId(task.getId())
                        .sheetName(task.getSheetName())
                        .taskType(task.getTaskType())
                        .rowCount(task.getRowCount())
                        .createdAt(task.getCreatedAt())
                        .build())
                    .toList())
                .totalTasks(tasks.size())
                .build());
    }

    /**
     * Save input data for a task
     */
    private Mono<Void> saveInputData(Task task, ParsedExcelData.SheetData sheetData) {
        return Flux.fromIterable(sheetData.getRows())
            .flatMap(rowData -> {
                ChatEvaluationInput input = ChatEvaluationInput.builder()
                    .taskId(task.getId())
                    .rowNumber(rowData.getRowNumber())
                    .question(rowData.getQuestion())
                    .goldenAnswer(rowData.getGoldenAnswer())
                    .goldenCitations(rowData.getGoldenCitations())
                    .metadata(rowData.getMetadata())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                return inputRepository.save(input);
            })
            .then();
    }

    /**
     * Get user's tasks
     */
    public Flux<Task> getUserTasks(String userId) {
        return taskRepository.findByUserId(userId);
    }

    /**
     * Get task by ID (with ownership validation)
     */
    public Mono<Task> getTaskById(UUID taskId, String userId) {
        return taskRepository.findById(taskId)
            .filter(task -> task.getUserId().equals(userId))
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Task not found or access denied")));
    }
} 