package com.example.springhttpclientdatajpademo.domain.chatevaluation.service;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskItem;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskResult;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.GleanApiResponse;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.LlmSimilarityResponse;
import com.example.springhttpclientdatajpademo.infrastructure.client.GleanServiceClient;
import com.example.springhttpclientdatajpademo.infrastructure.client.GleanServiceException;
import com.example.springhttpclientdatajpademo.infrastructure.client.LlmSimilarityServiceClient;
import com.example.springhttpclientdatajpademo.infrastructure.client.LlmSimilarityServiceException;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationTaskResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain service for chat evaluation processing
 * Contains core business logic for evaluating individual chat inputs
 * 
 * This service orchestrates:
 * - Glean API calls for question answering
 * - LLM Similarity API calls for answer and citation comparison
 * - Result storage and progress tracking
 * 
 * Following Domain-Driven Design principles:
 * - Domain service for complex business logic
 * - Encapsulates external service coordination
 * - Maintains transactional boundaries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEvaluationService {
    
    private final GleanServiceClient gleanServiceClient;
    private final LlmSimilarityServiceClient llmSimilarityServiceClient;
    private final ChatEvaluationTaskResultRepository outputRepository;
    
    /**
     * Evaluate a single chat taskItem and store the results
     * 
     * @param taskItem the chat evaluation taskItem to process
     * @return the evaluation output with similarity scores
     * @throws ChatEvaluationException if evaluation fails after retries
     */
    @Transactional
    @Retryable(
        value = {GleanServiceException.class, LlmSimilarityServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public ChatEvaluationTaskResult evaluateInput(final ChatEvaluationTaskItem taskItem) {
        log.info("Starting evaluation for taskItem ID: {} from task: {}",
                taskItem.getId(), taskItem.getTask().getId());
        
        final long startTime = System.currentTimeMillis();
        
        try {
            // Mark evaluation as started
            taskItem.markEvaluationStarted();
            
            // Step 1: Get API response from Glean Platform Services
            log.debug("Calling Glean API for question: {}", 
                     taskItem.getQuestion().substring(0, Math.min(50, taskItem.getQuestion().length())));
            
            final GleanApiResponse gleanResponse = gleanServiceClient.askQuestion(taskItem.getQuestion());
            
            if (!gleanResponse.isSuccessful()) {
                throw new ChatEvaluationException(
                    "Glean API call failed: " + gleanResponse.getErrorMessage()
                );
            }
            
            // Step 2: Calculate answer similarity
            log.debug("Calculating answer similarity");
            final LlmSimilarityResponse answerSimilarity = llmSimilarityServiceClient.calculateSimilarity(
                taskItem.getExpectedAnswer(),
                gleanResponse.getAnswer()
            );
            
            if (!answerSimilarity.isSuccessful()) {
                throw new ChatEvaluationException(
                    "Answer similarity calculation failed: " + answerSimilarity.getErrorMessage()
                );
            }
            
            // Step 3: Calculate citation similarity
            log.debug("Calculating citation similarity");
            final LlmSimilarityResponse citationSimilarity = llmSimilarityServiceClient.calculateCitationSimilarity(
                taskItem.getExpectedDocs(),
                gleanResponse.getCitations()
            );
            
            if (!citationSimilarity.isSuccessful()) {
                throw new ChatEvaluationException(
                    "Citation similarity calculation failed: " + citationSimilarity.getErrorMessage()
                );
            }
            
            // Step 4: Create and save evaluation output
            final long processingTime = System.currentTimeMillis() - startTime;
            
            final ChatEvaluationTaskResult output = ChatEvaluationTaskResult.builder()
                    .taskItem(taskItem)
                    .latency((int) processingTime)  // Add required latency field
                    .modelOutput(gleanResponse.getAnswer())
                    .reference(gleanResponse.getCitations())
                    .alignModel("default-llm")  // TODO: Make this configurable
                    .alignInferenceOutput("evaluation completed")  // TODO: Add proper align inference
                    .alignJudgeRating(ChatEvaluationTaskResult.AlignJudgeRating.PASS_ACCURATE_COMPLETE)  // TODO: Implement proper rating logic
                    .retrievedDocs(gleanResponse.getCitations())  // Using citations as retrieved docs for now
                    .expectedDocsRetrieved(taskItem.getExpectedDocs())
                    .matchedProp(answerSimilarity.getSimilarity())
                    .minHit(citationSimilarity.getSimilarity())
                    .build();
            
            output.setTaskItem(taskItem);
            
            final ChatEvaluationTaskResult savedOutput = outputRepository.save(output);
            
            // Mark evaluation as completed
            taskItem.markEvaluationCompleted();
            
            log.info("Completed evaluation for taskItem ID: {} in {}ms. Matched proportion: {}, Min hit: {}",
                    taskItem.getId(), processingTime,
                    answerSimilarity.getSimilarity(), citationSimilarity.getSimilarity());
            
            return savedOutput;
            
        } catch (Exception e) {
            log.error("Evaluation failed for taskItem ID: {} from task: {}. Error: {}",
                     taskItem.getId(), taskItem.getTask().getId(), e.getMessage(), e);
            throw new ChatEvaluationException("Chat evaluation failed for taskItem " + taskItem.getId(), e);
        }
    }
    
    /**
     * Check if an input has already been evaluated
     * 
     * @param taskItem the chat evaluation task item
     * @return true if output already exists
     */
    public boolean isAlreadyEvaluated(final ChatEvaluationTaskItem taskItem) {
        return outputRepository.existsByTaskItemId(taskItem.getId());
    }
    
    /**
     * Get existing evaluation output for an input
     * 
     * @param taskItem the chat evaluation task item
     * @return the evaluation output if it exists
     */
    public ChatEvaluationTaskResult getExistingOutput(final ChatEvaluationTaskItem taskItem) {
        return outputRepository.findByTaskItemId(taskItem.getId()).orElse(null);
    }
    
    // Note: buildResponseMetadata method removed as metadata fields no longer exist in ChatEvaluationTaskResult
    
    /**
     * Custom exception for chat evaluation failures
     */
    public static class ChatEvaluationException extends RuntimeException {
        public ChatEvaluationException(String message) {
            super(message);
        }
        
        public ChatEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 