package com.example.springhttpclientdatajpademo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling JWT token operations
 * Handles token validation and user extraction
 */
@Slf4j
@Service
public class JwtService {

    /**
     * Extract user ID from JWT token
     * This is a simplified implementation - in real app, use proper JWT validation
     * 
     * @param authHeader Authorization header containing Bearer token
     * @return User ID extracted from token
     * @throws IllegalArgumentException if token is invalid or missing
     */
    public String extractUserIdFromToken(String authHeader) {
        log.debug("Extracting user ID from authorization header");
        
        // TODO: Implement proper JWT token validation
        // - Verify token signature
        // - Extract user claims
        // - Validate token expiration
        // - Return user_id from token claims
        
        // For demo purposes, return a mock user ID
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String userId = "demo-user-123"; // Mock user ID
            log.debug("Extracted user ID: {}", userId);
            return userId;
        }
        
        log.warn("Invalid or missing Authorization header: {}", authHeader);
        throw new IllegalArgumentException("Invalid or missing Authorization header");
    }

    /**
     * Validate JWT token format and structure
     * 
     * @param authHeader Authorization header to validate
     * @return true if token format is valid
     */
    public boolean isValidTokenFormat(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7;
    }
}