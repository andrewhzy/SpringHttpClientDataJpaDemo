package com.example.springhttpclientdatajpademo.infrastructure.client.mock;

import com.example.springhttpclientdatajpademo.config.MockExternalServicesConfig;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.GleanApiResponse;
import com.example.springhttpclientdatajpademo.infrastructure.client.GleanServiceClient;
import com.example.springhttpclientdatajpademo.infrastructure.client.GleanServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock implementation of Glean Platform Services client for development
 * Generates realistic responses without requiring external dependencies
 * 
 * This implementation provides:
 * - Configurable response delays to simulate network latency
 * - Realistic answer generation based on question keywords
 * - Citation generation with varied relevance
 * - Configurable failure rates for testing error handling
 * 
 * Following the Strategy pattern for different response types
 */
@Component
@ConditionalOnProperty(value = "app.mock.glean.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class MockGleanServiceClient implements GleanServiceClient {
    
    private final MockExternalServicesConfig config;
    
    // Predefined knowledge base for generating realistic answers
    private static final Map<String, String> KNOWLEDGE_BASE = Map.of(
        "artificial intelligence", "Artificial Intelligence (AI) refers to the simulation of human intelligence in machines that are programmed to think and learn like humans.",
        "machine learning", "Machine Learning is a subset of artificial intelligence that enables computers to learn and improve from experience without being explicitly programmed.",
        "deep learning", "Deep Learning is a machine learning technique that teaches computers to process data in a way that is inspired by the human brain.",
        "neural network", "A Neural Network is a computing system inspired by biological neural networks that constitute animal brains.",
        "python", "Python is a high-level, interpreted programming language with dynamic semantics and built-in data structures.",
        "java", "Java is a high-level, class-based, object-oriented programming language designed to have as few implementation dependencies as possible.",
        "spring boot", "Spring Boot is a framework that makes it easy to create stand-alone, production-grade Spring-based applications.",
        "database", "A database is an organized collection of structured information, or data, typically stored electronically in a computer system.",
        "api", "An Application Programming Interface (API) is a set of protocols, routines, and tools for building software applications."
    );
    
    private static final List<String> SAMPLE_CITATIONS = List.of(
        "https://en.wikipedia.org/wiki/Artificial_intelligence",
        "https://www.ibm.com/cloud/learn/what-is-artificial-intelligence",
        "https://aws.amazon.com/machine-learning/what-is-ai/",
        "https://www.microsoft.com/en-us/ai/ai-platform",
        "https://cloud.google.com/learn/what-is-artificial-intelligence",
        "https://www.oracle.com/artificial-intelligence/what-is-ai/",
        "https://www.tensorflow.org/learn",
        "https://pytorch.org/tutorials/",
        "https://scikit-learn.org/stable/",
        "https://keras.io/guides/"
    );
    
    @Override
    public GleanApiResponse askQuestion(String question) {
        log.debug("MockGleanServiceClient processing question: {}", question);
        
        final long startTime = System.currentTimeMillis();
        
        try {
            // Simulate processing delay
            simulateDelay();
            
            // Check for simulated failures
            if (shouldSimulateFailure()) {
                final long responseTime = System.currentTimeMillis() - startTime;
                throw new GleanServiceException("Simulated API timeout", 503, "TIMEOUT");
            }
            
            // Generate response
            final String answer = generateAnswer(question);
            final List<String> citations = generateCitations();
            final double confidence = generateConfidence();
            final long responseTime = System.currentTimeMillis() - startTime;
            
            final Map<String, Object> metadata = Map.of(
                "model", "mock-glean-v1.0",
                "tokens_used", ThreadLocalRandom.current().nextInt(50, 200),
                "processing_time_ms", responseTime
            );
            
            final GleanApiResponse response = GleanApiResponse.success(
                answer, citations, confidence, responseTime, metadata
            );
            
            log.debug("MockGleanServiceClient generated response in {}ms", responseTime);
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final long responseTime = System.currentTimeMillis() - startTime;
            throw new GleanServiceException("Request interrupted", e, 500, "INTERRUPTED");
        }
    }
    
    @Override
    public boolean isServiceAvailable() {
        return config.getGlean().isEnabled();
    }
    
    @Override
    public String getHealthStatus() {
        return config.getGlean().isEnabled() ? "Mock Glean Service - Healthy" : "Mock Glean Service - Disabled";
    }
    
    /**
     * Simulate network delay based on configuration
     */
    private void simulateDelay() throws InterruptedException {
        final int minDelay = config.getGlean().getMinDelayMs();
        final int maxDelay = config.getGlean().getMaxDelayMs();
        final int delay = ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1);
        Thread.sleep(delay);
    }
    
    /**
     * Determine if this request should simulate a failure
     */
    private boolean shouldSimulateFailure() {
        final double failureRate = config.getGlean().getFailureRate();
        return ThreadLocalRandom.current().nextDouble() < failureRate;
    }
    
    /**
     * Generate a realistic answer based on question keywords
     */
    private String generateAnswer(final String question) {
        if (!config.getGlean().isSmartAnswers()) {
            return "This is a mock response generated for development purposes.";
        }
        
        final String lowerQuestion = question.toLowerCase();
        
        // Try to find matching keywords in knowledge base
        for (Map.Entry<String, String> entry : KNOWLEDGE_BASE.entrySet()) {
            if (lowerQuestion.contains(entry.getKey())) {
                return entry.getValue() + " " + generateAdditionalContext();
            }
        }
        
        // Generate generic answer if no keywords match
        return generateGenericAnswer(question);
    }
    
    /**
     * Generate additional context for answers
     */
    private String generateAdditionalContext() {
        final List<String> contexts = List.of(
            "This technology has been rapidly evolving in recent years.",
            "Many companies are investing heavily in this area.",
            "It has applications across various industries including healthcare, finance, and technology.",
            "Research in this field continues to advance our understanding.",
            "Best practices in this domain are still being established."
        );
        
        return contexts.get(ThreadLocalRandom.current().nextInt(contexts.size()));
    }
    
    /**
     * Generate a generic answer when no specific knowledge is available
     */
    private String generateGenericAnswer(final String question) {
        return String.format(
            "Based on the question '%s', this appears to be related to a technical or domain-specific topic. " +
            "The answer would depend on the specific context and requirements of your use case. " +
            "For accurate information, please consult authoritative sources in the relevant field.",
            question.length() > 50 ? question.substring(0, 50) + "..." : question
        );
    }
    
    /**
     * Generate realistic citations
     */
    private List<String> generateCitations() {
        final int citationCount = Math.min(config.getGlean().getCitationCount(), SAMPLE_CITATIONS.size());
        final List<String> citations = new ArrayList<>(SAMPLE_CITATIONS);
        Collections.shuffle(citations);
        return citations.subList(0, citationCount);
    }
    
    /**
     * Generate confidence score for the response
     */
    private double generateConfidence() {
        // Generate confidence between 0.6 and 0.95 (realistic range)
        return 0.6 + (ThreadLocalRandom.current().nextDouble() * 0.35);
    }
} 