package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.urlcleaning.model.UrlCleaningTaskResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UrlCleaningTaskResult entity
 * Provides data access operations for URL cleaning task results
 * 
 * Following Spring Data JPA conventions and best practices
 * 
 * Note: The current UrlCleaningTaskResult model lacks a relationship to UrlCleaningTaskItem.
 * Some methods below assume this relationship will be added in the future.
 */
@Repository
public interface UrlCleaningTaskResultRepository extends JpaRepository<UrlCleaningTaskResult, Long> {
    
    /**
     * Find results by URL pattern
     * @param url the URL to search for
     * @return list of URL cleaning results matching the URL
     */
    List<UrlCleaningTaskResult> findByUrl(String url);
    
    /**
     * Find results by URL containing a specific pattern
     * @param urlPattern the URL pattern to search for
     * @return list of URL cleaning results containing the pattern
     */
    List<UrlCleaningTaskResult> findByUrlContainingIgnoreCase(String urlPattern);
    
    /**
     * Find all results ordered by URL
     * @return list of all URL cleaning results ordered by URL
     */
    List<UrlCleaningTaskResult> findAllByOrderByUrlAsc();
    
    /**
     * Count results by URL
     * @param url the URL to count
     * @return count of results for the URL
     */
    long countByUrl(String url);
    
    /**
     * Check if result exists for a specific URL
     * @param url the URL to check
     * @return true if result exists for the URL
     */
    boolean existsByUrl(String url);
    
    /**
     * Find results by URL starting with a specific prefix
     * @param urlPrefix the URL prefix to search for
     * @return list of URL cleaning results with URLs starting with the prefix
     */
    List<UrlCleaningTaskResult> findByUrlStartingWithIgnoreCase(String urlPrefix);
    
    /**
     * Delete results by URL
     * @param url the URL to delete results for
     * @return number of deleted records
     */
    int deleteByUrl(String url);
    
    // TODO: Add methods that work with taskItem relationship once it's added to the model
    // Example methods that would be available after adding taskItem relationship:
    //
    // /**
    //  * Find all results for a specific task ID
    //  * @param taskId the task ID to filter by
    //  * @return list of URL cleaning results for the task
    //  */
    // @Query("SELECT r FROM UrlCleaningTaskResult r JOIN r.taskItem i WHERE i.task.id = :taskId ORDER BY i.id")
    // List<UrlCleaningTaskResult> findByTaskId(@Param("taskId") Long taskId);
    //
    // /**
    //  * Find result by task item ID
    //  * @param taskItemId the task item ID
    //  * @return optional URL cleaning result
    //  */
    // Optional<UrlCleaningTaskResult> findByTaskItemId(Long taskItemId);
    //
    // /**
    //  * Count results for a specific task
    //  * @param taskId the task ID
    //  * @return count of results for the task
    //  */
    // @Query("SELECT COUNT(r) FROM UrlCleaningTaskResult r JOIN r.taskItem i WHERE i.task.id = :taskId")
    // long countByTaskId(@Param("taskId") Long taskId);
} 