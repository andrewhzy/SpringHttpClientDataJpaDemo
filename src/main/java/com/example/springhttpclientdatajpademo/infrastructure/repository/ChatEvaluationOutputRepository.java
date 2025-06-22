package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ChatEvaluationOutput entity
 * Provides data access operations for chat evaluation results
 * 
 * Following Spring Data JPA conventions and best practices
 */
@Repository
public interface ChatEvaluationOutputRepository extends JpaRepository<ChatEvaluationOutput, Long> {
    
    /**
     * Find all outputs for a specific task ID
     * @param taskId the task ID to filter by
     * @return list of chat evaluation outputs for the task
     */
    @Query("SELECT o FROM ChatEvaluationOutput o JOIN o.input i WHERE i.task.id = :taskId ORDER BY i.id")
    List<ChatEvaluationOutput> findByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find output by input ID
     * @param inputId the input ID
     * @return optional chat evaluation output
     */
    Optional<ChatEvaluationOutput> findByInputId(Long inputId);
    
    /**
     * Count outputs for a specific task
     * @param taskId the task ID
     * @return count of outputs for the task
     */
    @Query("SELECT COUNT(o) FROM ChatEvaluationOutput o JOIN o.input i WHERE i.task.id = :taskId")
    long countByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Check if output exists for a specific input
     * @param inputId the input ID
     * @return true if output exists
     */
    boolean existsByInputId(Long inputId);
    
    /**
     * Find outputs with good matches for a task (similarity > 0.7)
     * @param taskId the task ID
     * @return list of high-quality outputs
     */
    @Query("SELECT o FROM ChatEvaluationOutput o JOIN o.input i " +
           "WHERE i.task.id = :taskId AND o.answerSimilarity >= 0.7 AND o.citationSimilarity >= 0.7 " +
           "ORDER BY o.answerSimilarity DESC, o.citationSimilarity DESC")
    List<ChatEvaluationOutput> findGoodMatchesByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find outputs with poor matches for a task (similarity < 0.5)
     * @param taskId the task ID
     * @return list of low-quality outputs
     */
    @Query("SELECT o FROM ChatEvaluationOutput o JOIN o.input i " +
           "WHERE i.task.id = :taskId AND (o.answerSimilarity < 0.5 OR o.citationSimilarity < 0.5) " +
           "ORDER BY o.answerSimilarity ASC, o.citationSimilarity ASC")
    List<ChatEvaluationOutput> findPoorMatchesByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Delete all outputs for a specific task
     * @param taskId the task ID
     * @return number of deleted records
     */
    @Query("DELETE FROM ChatEvaluationOutput o WHERE o.input.task.id = :taskId")
    int deleteByTaskId(@Param("taskId") Long taskId);
} 