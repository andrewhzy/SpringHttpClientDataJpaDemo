package com.example.springhttpclientdatajpademo.domain.chatevaluation.service;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.GleanApiResponse;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.LlmSimilarityResponse;
import com.example.springhttpclientdatajpademo.infrastructure.client.GleanServiceClient;
import com.example.springhttpclientdatajpademo.infrastructure.client.GleanServiceException;
import com.example.springhttpclientdatajpademo.infrastructure.client.LlmSimilarityServiceClient;
import com.example.springhttpclientdatajpademo.infrastructure.client.LlmSimilarityServiceException;
import com.example.springhttpclientdatajpademo.infrastructure.repository.ChatEvaluationOutputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ChatEvaluationOutputRepository outputRepository;
    
    /**
     * Evaluate a single chat input and store the results
     * 
     * @param input the chat evaluation input to process
     * @return the evaluation output with similarity scores
     * @throws ChatEvaluationException if evaluation fails after retries
     */
    @Transactional
    @Retryable(
        value = {GleanServiceException.class, LlmSimilarityServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public ChatEvaluationOutput evaluateInput(final ChatEvaluationInput input) {
        log.info("Starting evaluation for input ID: {} from task: {}", 
                input.getId(), input.getTask().getId());
        
        final long startTime = System.currentTimeMillis();
        
        try {
            // Mark evaluation as started
            input.markEvaluationStarted();
            
            // Step 1: Get API response from Glean Platform Services
            log.debug("Calling Glean API for question: {}", 
                     input.getQuestion().substring(0, Math.min(50, input.getQuestion().length())));
            
            final GleanApiResponse gleanResponse = gleanServiceClient.askQuestion(input.getQuestion());
            
            if (!gleanResponse.isSuccessful()) {
                throw new ChatEvaluationException(
                    "Glean API call failed: " + gleanResponse.getErrorMessage()
                );
            }
            
            // Step 2: Calculate answer similarity
            log.debug("Calculating answer similarity");
            final LlmSimilarityResponse answerSimilarity = llmSimilarityServiceClient.calculateSimilarity(
                input.getGoldenAnswer(), 
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
                input.getGoldenCitations(),
                gleanResponse.getCitations()
            );
            
            if (!citationSimilarity.isSuccessful()) {
                throw new ChatEvaluationException(
                    "Citation similarity calculation failed: " + citationSimilarity.getErrorMessage()
                );
            }
            
            // Step 4: Create and save evaluation output
            final long processingTime = System.currentTimeMillis() - startTime;
            
            final ChatEvaluationOutput output = ChatEvaluationOutput.builder()
                    .input(input)
                    .apiAnswer(gleanResponse.getAnswer())
                    .apiCitations(gleanResponse.getCitations())
                    .answerSimilarity(answerSimilarity.getSimilarity())
                    .citationSimilarity(citationSimilarity.getSimilarity())
                    .processingTimeMs((int) processingTime)
                    .apiResponseMetadata(buildResponseMetadata(gleanResponse, answerSimilarity, citationSimilarity))
                    .build();
            
            output.setInput(input);
            
            final ChatEvaluationOutput savedOutput = outputRepository.save(output);
            
            // Mark evaluation as completed
            input.markEvaluationCompleted();
            
            log.info("Completed evaluation for input ID: {} in {}ms. Answer similarity: {}, Citation similarity: {}",
                    input.getId(), processingTime, 
                    answerSimilarity.getSimilarity(), citationSimilarity.getSimilarity());
            
            return savedOutput;
            
        } catch (Exception e) {
            log.error("Evaluation failed for input ID: {} from task: {}. Error: {}", 
                     input.getId(), input.getTask().getId(), e.getMessage(), e);
            throw new ChatEvaluationException("Chat evaluation failed for input " + input.getId(), e);
        }
    }
    
    /**
     * Check if an input has already been evaluated
     * 
     * @param input the chat evaluation input
     * @return true if output already exists
     */
    public boolean isAlreadyEvaluated(final ChatEvaluationInput input) {
        return outputRepository.existsByInputId(input.getId());
    }
    
    /**
     * Get existing evaluation output for an input
     * 
     * @param input the chat evaluation input
     * @return the evaluation output if it exists
     */
    public ChatEvaluationOutput getExistingOutput(final ChatEvaluationInput input) {
        return outputRepository.findByInputId(input.getId()).orElse(null);
    }
    
    /**
     * Build comprehensive metadata from all API responses
     */
    private Map<String, Object> buildResponseMetadata(
            final GleanApiResponse gleanResponse,
            final LlmSimilarityResponse answerSimilarity,
            final LlmSimilarityResponse citationSimilarity) {
        
        final Map<String, Object> metadata = new HashMap<>();
        
        // Glean API metadata
        metadata.put("glean_confidence", gleanResponse.getConfidence());
        metadata.put("glean_response_time_ms", gleanResponse.getResponseTimeMs());
        metadata.put("glean_citation_count", gleanResponse.getCitationCount());
        
        if (gleanResponse.getMetadata() != null) {
            metadata.put("glean_metadata", gleanResponse.getMetadata());
        }
        
        // Answer similarity metadata
        metadata.put("answer_similarity_method", answerSimilarity.getMethod());
        metadata.put("answer_similarity_confidence", answerSimilarity.getConfidence());
        metadata.put("answer_similarity_response_time_ms", answerSimilarity.getResponseTimeMs());
        
        if (answerSimilarity.getMetadata() != null) {
            metadata.put("answer_similarity_metadata", answerSimilarity.getMetadata());
        }
        
        // Citation similarity metadata
        metadata.put("citation_similarity_method", citationSimilarity.getMethod());
        metadata.put("citation_similarity_confidence", citationSimilarity.getConfidence());
        metadata.put("citation_similarity_response_time_ms", citationSimilarity.getResponseTimeMs());
        
        if (citationSimilarity.getMetadata() != null) {
            metadata.put("citation_similarity_metadata", citationSimilarity.getMetadata());
        }
        
        // Overall evaluation metadata
        metadata.put("evaluation_timestamp", System.currentTimeMillis());
        metadata.put("evaluation_version", "1.0");
        
        return metadata;
    }
    
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