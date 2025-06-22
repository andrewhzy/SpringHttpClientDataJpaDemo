package com.example.springhttpclientdatajpademo.infrastructure.client.mock;

import com.example.springhttpclientdatajpademo.config.MockExternalServicesConfig;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.LlmSimilarityResponse;
import com.example.springhttpclientdatajpademo.infrastructure.client.LlmSimilarityServiceClient;
import com.example.springhttpclientdatajpademo.infrastructure.client.LlmSimilarityServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Mock implementation of LLM Similarity Service client for development
 * Calculates realistic similarity scores using basic text comparison heuristics
 * 
 * This implementation provides:
 * - Intelligent text similarity calculation using various heuristics
 * - Citation overlap analysis for reference comparison
 * - Configurable response delays and failure rates
 * - Realistic confidence scores
 * 
 * Following the Strategy pattern for different similarity calculation methods
 */
@Component
@ConditionalOnProperty(value = "app.mock.llm.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class MockLlmSimilarityServiceClient implements LlmSimilarityServiceClient {
    
    private final MockExternalServicesConfig config;
    
    @Override
    public LlmSimilarityResponse calculateSimilarity(String text1, String text2) {
        log.debug("MockLlmSimilarityServiceClient calculating similarity between texts");
        
        final long startTime = System.currentTimeMillis();
        
        try {
            // Simulate processing delay
            simulateDelay();
            
            // Check for simulated failures
            if (shouldSimulateFailure()) {
                final long responseTime = System.currentTimeMillis() - startTime;
                throw new LlmSimilarityServiceException("Simulated API failure", 503, "SERVICE_UNAVAILABLE");
            }
            
            // Calculate similarity
            final BigDecimal similarity = calculateTextSimilarity(text1, text2);
            final long responseTime = System.currentTimeMillis() - startTime;
            final double confidence = generateConfidence(similarity);
            
            final Map<String, Object> metadata = Map.of(
                "method", "mock-semantic-similarity-v1.0",
                "text1_length", text1.length(),
                "text2_length", text2.length(),
                "processing_time_ms", responseTime,
                "algorithm", config.getLlm().isIntelligentSimilarity() ? "heuristic" : "random"
            );
            
            final LlmSimilarityResponse response = LlmSimilarityResponse.success(
                similarity, responseTime, "mock-semantic", confidence, metadata
            );
            
            log.debug("MockLlmSimilarityServiceClient calculated similarity: {} in {}ms", 
                     similarity, responseTime);
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final long responseTime = System.currentTimeMillis() - startTime;
            throw new LlmSimilarityServiceException("Request interrupted", e, 500, "INTERRUPTED");
        }
    }
    
    @Override
    public LlmSimilarityResponse calculateCitationSimilarity(List<String> citations1, List<String> citations2) {
        log.debug("MockLlmSimilarityServiceClient calculating citation similarity");
        
        final long startTime = System.currentTimeMillis();
        
        try {
            // Simulate processing delay
            simulateDelay();
            
            // Check for simulated failures
            if (shouldSimulateFailure()) {
                final long responseTime = System.currentTimeMillis() - startTime;
                throw new LlmSimilarityServiceException("Simulated API failure", 503, "SERVICE_UNAVAILABLE");
            }
            
            // Calculate citation similarity
            final BigDecimal similarity = calculateCitationOverlap(citations1, citations2);
            final long responseTime = System.currentTimeMillis() - startTime;
            final double confidence = generateConfidence(similarity);
            
            final Map<String, Object> metadata = Map.of(
                "method", "mock-citation-overlap-v1.0",
                "citations1_count", citations1.size(),
                "citations2_count", citations2.size(),
                "processing_time_ms", responseTime,
                "overlap_method", "url_domain_matching"
            );
            
            final LlmSimilarityResponse response = LlmSimilarityResponse.success(
                similarity, responseTime, "mock-citation-overlap", confidence, metadata
            );
            
            log.debug("MockLlmSimilarityServiceClient calculated citation similarity: {} in {}ms", 
                     similarity, responseTime);
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final long responseTime = System.currentTimeMillis() - startTime;
            throw new LlmSimilarityServiceException("Request interrupted", e, 500, "INTERRUPTED");
        }
    }
    
    @Override
    public boolean isServiceAvailable() {
        return config.getLlm().isEnabled();
    }
    
    @Override
    public String getHealthStatus() {
        return config.getLlm().isEnabled() ? "Mock LLM Similarity Service - Healthy" : "Mock LLM Similarity Service - Disabled";
    }
    
    /**
     * Simulate network delay based on configuration
     */
    private void simulateDelay() throws InterruptedException {
        final int minDelay = config.getLlm().getMinDelayMs();
        final int maxDelay = config.getLlm().getMaxDelayMs();
        final int delay = ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1);
        Thread.sleep(delay);
    }
    
    /**
     * Determine if this request should simulate a failure
     */
    private boolean shouldSimulateFailure() {
        final double failureRate = config.getLlm().getFailureRate();
        return ThreadLocalRandom.current().nextDouble() < failureRate;
    }
    
    /**
     * Calculate text similarity using various heuristics
     */
    private BigDecimal calculateTextSimilarity(final String text1, final String text2) {
        if (!config.getLlm().isIntelligentSimilarity()) {
            return generateRandomSimilarity();
        }
        
        // Handle null or empty texts
        if (text1 == null || text2 == null || text1.trim().isEmpty() || text2.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Exact match
        if (text1.equals(text2)) {
            return BigDecimal.valueOf(config.getLlm().getIdenticalTextSimilarity());
        }
        
        // Case-insensitive exact match
        if (text1.equalsIgnoreCase(text2)) {
            return BigDecimal.valueOf(config.getLlm().getIdenticalTextSimilarity() - 0.05);
        }
        
        // Calculate combined similarity score
        final double lengthSimilarity = calculateLengthSimilarity(text1, text2);
        final double wordOverlap = calculateWordOverlap(text1, text2);
        final double characterSimilarity = calculateJaccardSimilarity(text1, text2);
        
        // Weighted combination of different similarity measures
        final double combinedScore = (lengthSimilarity * 0.2) + (wordOverlap * 0.5) + (characterSimilarity * 0.3);
        
        return BigDecimal.valueOf(combinedScore).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate similarity based on text length difference
     */
    private double calculateLengthSimilarity(final String text1, final String text2) {
        final int len1 = text1.length();
        final int len2 = text2.length();
        final int maxLen = Math.max(len1, len2);
        
        if (maxLen == 0) return 1.0;
        
        final double lengthDiff = Math.abs(len1 - len2);
        return 1.0 - (lengthDiff / maxLen);
    }
    
    /**
     * Calculate word overlap similarity
     */
    private double calculateWordOverlap(final String text1, final String text2) {
        final Set<String> words1 = Arrays.stream(text1.toLowerCase().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
        final Set<String> words2 = Arrays.stream(text2.toLowerCase().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
        
        final Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        final Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate Jaccard similarity for character-level comparison
     */
    private double calculateJaccardSimilarity(final String text1, final String text2) {
        final Set<Character> chars1 = text1.toLowerCase().chars()
                .mapToObj(c -> (char) c)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        
        final Set<Character> chars2 = text2.toLowerCase().chars()
                .mapToObj(c -> (char) c)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        
        final Set<Character> intersection = new HashSet<>(chars1);
        intersection.retainAll(chars2);
        
        final Set<Character> union = new HashSet<>(chars1);
        union.addAll(chars2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate citation overlap based on domain matching
     */
    private BigDecimal calculateCitationOverlap(final List<String> citations1, final List<String> citations2) {
        if (citations1.isEmpty() && citations2.isEmpty()) {
            return BigDecimal.ONE; // Both empty, consider as perfect match
        }
        
        if (citations1.isEmpty() || citations2.isEmpty()) {
            return BigDecimal.ZERO; // One empty, no overlap
        }
        
        // Extract domains for comparison
        final Set<String> domains1 = extractDomains(citations1);
        final Set<String> domains2 = extractDomains(citations2);
        
        // Calculate Jaccard similarity on domains
        final Set<String> intersection = new HashSet<>(domains1);
        intersection.retainAll(domains2);
        
        final Set<String> union = new HashSet<>(domains1);
        union.addAll(domains2);
        
        final double similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
        
        return BigDecimal.valueOf(similarity).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Extract domains from citation URLs
     */
    private Set<String> extractDomains(final List<String> citations) {
        return citations.stream()
                .map(this::extractDomain)
                .filter(Objects::nonNull)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    /**
     * Extract domain from a single URL
     */
    private String extractDomain(final String url) {
        try {
            // Simple domain extraction (for mock purposes)
            if (url.startsWith("http://") || url.startsWith("https://")) {
                final int startIndex = url.indexOf("://") + 3;
                final int endIndex = url.indexOf("/", startIndex);
                return endIndex == -1 ? url.substring(startIndex) : url.substring(startIndex, endIndex);
            }
            return url; // Return as-is if not a proper URL
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Generate random similarity for non-intelligent mode
     */
    private BigDecimal generateRandomSimilarity() {
        final double min = config.getLlm().getMinRandomSimilarity();
        final double max = config.getLlm().getMaxRandomSimilarity();
        final double random = min + (ThreadLocalRandom.current().nextDouble() * (max - min));
        return BigDecimal.valueOf(random).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Generate confidence score based on similarity
     */
    private double generateConfidence(final BigDecimal similarity) {
        // Higher similarity generally means higher confidence
        final double baseConfidence = 0.7;
        final double similarityBonus = similarity.doubleValue() * 0.25;
        final double noise = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1; // ±5% noise
        
        return Math.max(0.5, Math.min(0.98, baseConfidence + similarityBonus + noise));
    }
} 