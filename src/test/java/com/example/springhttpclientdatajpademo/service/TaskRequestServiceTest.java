package com.example.springhttpclientdatajpademo.service;

import com.example.springhttpclientdatajpademo.dto.CreateTaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskRequestServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private JwtService jwtService;

    @Mock
    private FilePart filePart;

    private TaskRequestService taskRequestService;

    @BeforeEach
    void setUp() {
        taskRequestService = new TaskRequestService(taskService, jwtService);
    }

    @Test
    void handleTaskCreationRequest_Success() {
        // Given
        String authHeader = "Bearer valid-token";
        String userId = "test-user-123";
        UUID uploadBatchId = UUID.randomUUID();
        
        CreateTaskResponse expectedResponse = CreateTaskResponse.builder()
            .uploadBatchId(uploadBatchId)
            .totalTasks(1)
            .build();

        when(filePart.filename()).thenReturn("test.xlsx");
        when(jwtService.extractUserIdFromToken(authHeader)).thenReturn(userId);
        when(taskService.createTasks(any(FilePart.class), eq(userId)))
            .thenReturn(Mono.just(expectedResponse));

        // When
        Mono<CreateTaskResponse> result = taskRequestService
            .handleTaskCreationRequest(Mono.just(filePart), authHeader);

        // Then
        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete();
    }

    @Test
    void handleTaskCreationRequest_InvalidFileExtension() {
        // Given
        String authHeader = "Bearer valid-token";
        String userId = "test-user-123";

        when(filePart.filename()).thenReturn("test.txt");
        when(jwtService.extractUserIdFromToken(authHeader)).thenReturn(userId);

        // When
        Mono<CreateTaskResponse> result = taskRequestService
            .handleTaskCreationRequest(Mono.just(filePart), authHeader);

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(throwable -> 
                throwable instanceof IllegalArgumentException &&
                throwable.getMessage().contains("Only Excel files"))
            .verify();
    }

    @Test
    void handleTaskCreationRequest_EmptyFilename() {
        // Given
        String authHeader = "Bearer valid-token";
        String userId = "test-user-123";

        when(filePart.filename()).thenReturn("");
        when(jwtService.extractUserIdFromToken(authHeader)).thenReturn(userId);

        // When
        Mono<CreateTaskResponse> result = taskRequestService
            .handleTaskCreationRequest(Mono.just(filePart), authHeader);

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(throwable -> 
                throwable instanceof IllegalArgumentException &&
                throwable.getMessage().contains("Filename cannot be empty"))
            .verify();
    }

    @Test
    void handleTaskCreationRequest_InvalidAuthHeader() {
        // Given
        String authHeader = "Invalid header";

        when(jwtService.extractUserIdFromToken(authHeader))
            .thenThrow(new IllegalArgumentException("Invalid authorization header"));

        // When
        Mono<CreateTaskResponse> result = taskRequestService
            .handleTaskCreationRequest(Mono.just(filePart), authHeader);

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(throwable -> 
                throwable instanceof IllegalArgumentException &&
                throwable.getMessage().contains("Invalid authorization header"))
            .verify();
    }
}