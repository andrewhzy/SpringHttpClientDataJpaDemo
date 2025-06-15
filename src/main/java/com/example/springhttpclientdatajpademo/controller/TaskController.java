package com.example.springhttpclientdatajpademo.controller;

import com.example.springhttpclientdatajpademo.dto.CreateTaskResponse;
import com.example.springhttpclientdatajpademo.service.TaskRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/rest/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRequestService taskRequestService;

    /**
     * Upload Excel file and create tasks
     * POST /rest/v1/tasks
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateTaskResponse> createTasks(
            @RequestPart("file") Mono<FilePart> filePartMono,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Received task creation request");
        
        return taskRequestService.handleTaskCreationRequest(filePartMono, authHeader);
    }
}