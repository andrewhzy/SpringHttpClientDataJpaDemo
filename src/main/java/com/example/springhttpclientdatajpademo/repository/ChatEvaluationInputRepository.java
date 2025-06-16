package com.example.springhttpclientdatajpademo.repository;

import com.example.springhttpclientdatajpademo.entity.ChatEvaluationInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ChatEvaluationInput entity operations
 */
@Repository
public interface ChatEvaluationInputRepository extends JpaRepository<ChatEvaluationInput, Long> {
    
    /**
     * Find all input data for a specific task ordered by row number
     */
    List<ChatEvaluationInput> findByTaskIdOrderByRowNumber(UUID taskId);
    
    /**
     * Find input data by task ID and row number
     */
    ChatEvaluationInput findByTaskIdAndRowNumber(UUID taskId, Integer rowNumber);
    
    /**
     * Count input rows for a specific task
     */
    long countByTaskId(UUID taskId);
    
    /**
     * Delete all input data for a specific task
     */
    void deleteByTaskId(UUID taskId);
    
    /**
     * Find input data that doesn't have corresponding output data
     */
    @Query("SELECT cei FROM ChatEvaluationInput cei LEFT JOIN ChatEvaluationOutput ceo ON cei.id = ceo.input.id " +
           "WHERE cei.task.id = :taskId AND ceo.id IS NULL ORDER BY cei.rowNumber")
    List<ChatEvaluationInput> findUnprocessedInputByTaskId(@Param("taskId") UUID taskId);
    
    /**
     * Get the next row number for processing in a task
     */
    @Query("SELECT MIN(cei.rowNumber) FROM ChatEvaluationInput cei " +
           "LEFT JOIN ChatEvaluationOutput ceo ON cei.id = ceo.input.id " +
           "WHERE cei.task.id = :taskId AND ceo.id IS NULL")
    Integer findNextUnprocessedRowNumber(@Param("taskId") UUID taskId);
} 