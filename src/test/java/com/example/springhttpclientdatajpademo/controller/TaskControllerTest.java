package com.example.springhttpclientdatajpademo.controller;

import com.example.springhttpclientdatajpademo.dto.CreateTaskResponse;
import com.example.springhttpclientdatajpademo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TaskService taskService;

    @Test
    void shouldReturnNotImplementedWhenExcelParsingNotImplemented() {
        // Given - Mock service to throw UnsupportedOperationException
        when(taskService.createTasks(any(), anyString()))
            .thenReturn(Mono.error(new UnsupportedOperationException("Excel parsing not yet implemented")));

        // When & Then - Call endpoint and expect NOT_IMPLEMENTED response
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-file.txt")).filename("test.xlsx");

        webTestClient
            .post()
            .uri("/rest/v1/tasks")
            .header("Authorization", "Bearer test-token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().isEqualTo(501) // NOT_IMPLEMENTED
            .expectBody()
            .jsonPath("$.code").isEqualTo("NOT_IMPLEMENTED")
            .jsonPath("$.message").isEqualTo("This feature is not yet implemented");
    }

    @Test
    void shouldReturnBadRequestForInvalidAuthHeader() {
        webTestClient
            .post()
            .uri("/rest/v1/tasks")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .exchange()
            .expectStatus().isBadRequest();
    }
} 