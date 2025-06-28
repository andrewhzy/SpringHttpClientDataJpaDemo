package com.example.springhttpclientdatajpademo.infrastructure.repository;

import com.example.springhttpclientdatajpademo.domain.urlcleaning.model.UrlCleaningTaskItem;
import com.example.springhttpclientdatajpademo.domain.task.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UrlCleaningTaskItem entities
 * Provides data access operations for URL cleaning task items
 * 
 * Following Effective Java Item 64: Refer to objects by their interfaces
 * 
 * Note: The UrlCleaningTaskItem entity currently lacks a Task relationship.
 * Task-based query methods will be added once the entity relationship is established.
 */
@Repository
public interface UrlCleaningTaskItemRepository extends JpaRepository<UrlCleaningTaskItem, Long> {
    
    // JpaRepository provides saveAll() method needed for batch saving task items
    // Additional methods can be added when other endpoints are implemented
    
    /**
     * Find all task items by URL pattern
     * @param url the URL to search for
     * @return list of task items with matching URL
     */
    List<UrlCleaningTaskItem> findByUrl(String url);
    
    /**
     * Find all task items ordered by ID for consistent processing
     * @return list of all task items ordered by ID
     */
    List<UrlCleaningTaskItem> findAllByOrderByIdAsc();
    
    // TODO: Add task-based methods once Task relationship is added to UrlCleaningTaskItem
    // /**
    //  * Find all task items for a task, ordered by ID for consistent processing
    //  * Used by background processor to get task items in deterministic order
    //  * 
    //  * @param task the task to find task items for
    //  * @return list of task items ordered by ID
    //  */
    // List<UrlCleaningTaskItem> findByTaskOrderByIdAsc(Task task);
} 