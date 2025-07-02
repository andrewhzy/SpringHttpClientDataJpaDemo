package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationTaskResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ChatEvaluationTaskResult entity
 * Provides data access operations for chat evaluation results
 * 
 * Following Spring Data JPA conventions and best practices
 */
@Repository
public interface ChatEvaluationTaskResultRepository extends JpaRepository<ChatEvaluationTaskResult, Long> {
    
    /**
     * Find all outputs for a specific task ID
     * @param taskId the task ID to filter by
     * @return list of chat evaluation outputs for the task
     */
    @Query("SELECT o FROM ChatEvaluationTaskResult o JOIN o.taskItem i WHERE i.task.id = :taskId ORDER BY i.id")
    List<ChatEvaluationTaskResult> findByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find output by input ID
     * @param taskItemId the task item ID
     * @return optional chat evaluation output
     */
    Optional<ChatEvaluationTaskResult> findByTaskItemId(Long taskItemId);
    
    /**
     * Count outputs for a specific task
     * @param taskId the task ID
     * @return count of outputs for the task
     */
    @Query("SELECT COUNT(o) FROM ChatEvaluationTaskResult o JOIN o.taskItem i WHERE i.task.id = :taskId")
    long countByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Check if output exists for a specific input
     * @param taskItemId the task item ID
     * @return true if output exists
     */
    boolean existsByTaskItemId(Long taskItemId);
    
    /**
     * Find outputs with good matches for a task (similarity > 0.7)
     * @param taskId the task ID
     * @return list of high-quality outputs
     */
    @Query("SELECT o FROM ChatEvaluationTaskResult o JOIN o.taskItem i " +
           "WHERE i.task.id = :taskId AND o.matchedProp >= 0.7 AND o.minHit >= 0.7 " +
           "ORDER BY o.matchedProp DESC, o.minHit DESC")
    List<ChatEvaluationTaskResult> findGoodMatchesByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find outputs with poor matches for a task (similarity < 0.5)
     * @param taskId the task ID
     * @return list of low-quality outputs
     */
    @Query("SELECT o FROM ChatEvaluationTaskResult o JOIN o.taskItem i " +
           "WHERE i.task.id = :taskId AND (o.matchedProp < 0.5 OR o.minHit < 0.5) " +
           "ORDER BY o.matchedProp ASC, o.minHit ASC")
    List<ChatEvaluationTaskResult> findPoorMatchesByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Delete all outputs for a specific task
     * @param taskId the task ID
     * @return number of deleted records
     */
    @Query("DELETE FROM ChatEvaluationTaskResult o WHERE o.taskItem.task.id = :taskId")
    int deleteByTaskId(@Param("taskId") Long taskId);
} 