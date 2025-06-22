package com.example.springhttpclientdatajpademo.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ListTasksCommand command
 * Demonstrates consistent command pattern usage for queries
 */
class ListTasksQueryTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid query with all required fields")
    void shouldCreateValidQuery() {
        // Given
        ListTasksCommand query = ListTasksCommand.builder()
                .userId("user-123")
                .perPage(20)
                .taskType("chat-evaluation")
                .cursor(null)
                .build();

        // When
        Set<ConstraintViolation<ListTasksCommand>> violations = validator.validate(query);

        // Then
        assertTrue(violations.isEmpty());
        assertTrue(query.isFirstPage());
    }

    @Test
    @DisplayName("Should create valid query with cursor for subsequent pages")
    void shouldCreateValidQueryWithCursor() {
        // Given
        ListTasksCommand query = ListTasksCommand.builder()
                .userId("user-123")
                .perPage(10)
                .taskType("chat-evaluation")
                .cursor(123L)
                .build();

        // When
        Set<ConstraintViolation<ListTasksCommand>> violations = validator.validate(query);

        // Then
        assertTrue(violations.isEmpty());
        assertFalse(query.isFirstPage());
        assertEquals(123L, query.getCursor());
    }

    @Test
    @DisplayName("Should use static factory method for first page")
    void shouldUseStaticFactoryForFirstPage() {
        // When
        ListTasksCommand query = ListTasksCommand.firstPage("user-123", 20, "chat-evaluation");

        // Then
        assertEquals("user-123", query.getUserId());
        assertEquals(20, query.getPerPage());
        assertEquals("chat-evaluation", query.getTaskType());
        assertNull(query.getCursor());
        assertTrue(query.isFirstPage());
    }

    @Test
    @DisplayName("Should use static factory method for next page")
    void shouldUseStaticFactoryForNextPage() {
        // When
        ListTasksCommand query = ListTasksCommand.nextPage("user-123", 20, "chat-evaluation", 456L);

        // Then
        assertEquals("user-123", query.getUserId());
        assertEquals(20, query.getPerPage());
        assertEquals("chat-evaluation", query.getTaskType());
        assertEquals(456L, query.getCursor());
        assertFalse(query.isFirstPage());
    }

    @Test
    @DisplayName("Should fail validation when userId is blank")
    void shouldFailValidationWhenUserIdIsBlank() {
        // Given
        ListTasksCommand query = ListTasksCommand.builder()
                .userId("")
                .perPage(20)
                .taskType("chat-evaluation")
                .build();

        // When
        Set<ConstraintViolation<ListTasksCommand>> violations = validator.validate(query);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("User ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when taskType is blank")
    void shouldFailValidationWhenTaskTypeIsBlank() {
        // Given
        ListTasksCommand query = ListTasksCommand.builder()
                .userId("user-123")
                .perPage(20)
                .taskType("")
                .build();

        // When
        Set<ConstraintViolation<ListTasksCommand>> violations = validator.validate(query);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Task type is required")));
    }

    @Test
    @DisplayName("Should fail validation when perPage is too small")
    void shouldFailValidationWhenPerPageTooSmall() {
        // Given
        ListTasksCommand query = ListTasksCommand.builder()
                .userId("user-123")
                .perPage(0)
                .taskType("chat-evaluation")
                .build();

        // When
        Set<ConstraintViolation<ListTasksCommand>> violations = validator.validate(query);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Per page must be at least 1")));
    }

    @Test
    @DisplayName("Should fail validation when perPage is too large")
    void shouldFailValidationWhenPerPageTooLarge() {
        // Given
        ListTasksCommand query = ListTasksCommand.builder()
                .userId("user-123")
                .perPage(101)
                .taskType("chat-evaluation")
                .build();

        // When
        Set<ConstraintViolation<ListTasksCommand>> violations = validator.validate(query);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Per page cannot exceed 100")));
    }
} 