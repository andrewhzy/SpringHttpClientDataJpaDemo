package com.example.springhttpclientdatajpademo.repository;

import com.example.springhttpclientdatajpademo.entity.ChatEvaluationOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ChatEvaluationOutput entity operations
 */
@Repository
public interface ChatEvaluationOutputRepository extends JpaRepository<ChatEvaluationOutput, Long> {
    
    /**
     * Find all output data for a specific task ordered by input row number
     */
    @Query("SELECT ceo FROM ChatEvaluationOutput ceo " +
           "JOIN ceo.input cei " +
           "WHERE cei.task.id = :taskId " +
           "ORDER BY cei.rowNumber")
    List<ChatEvaluationOutput> findByTaskIdOrderByRowNumber(@Param("taskId") UUID taskId);
    
    /**
     * Find output data by input ID
     */
    ChatEvaluationOutput findByInputId(Long inputId);
    
    /**
     * Count output rows for a specific task
     */
    @Query("SELECT COUNT(ceo) FROM ChatEvaluationOutput ceo " +
           "JOIN ceo.input cei " +
           "WHERE cei.task.id = :taskId")
    long countByTaskId(@Param("taskId") UUID taskId);
    
    /**
     * Delete all output data for a specific task
     */
    @Query("DELETE FROM ChatEvaluationOutput ceo " +
           "WHERE ceo.input.id IN (SELECT cei.id FROM ChatEvaluationInput cei WHERE cei.task.id = :taskId)")
    void deleteByTaskId(@Param("taskId") UUID taskId);
    
    /**
     * Check if output exists for a specific input
     */
    boolean existsByInputId(Long inputId);
    
    /**
     * Get average similarity scores for a task
     */
    @Query("SELECT AVG(ceo.answerSimilarity), AVG(ceo.citationSimilarity) " +
           "FROM ChatEvaluationOutput ceo " +
           "JOIN ceo.input cei " +
           "WHERE cei.task.id = :taskId")
    Object[] getAverageSimilarityScores(@Param("taskId") UUID taskId);
} 