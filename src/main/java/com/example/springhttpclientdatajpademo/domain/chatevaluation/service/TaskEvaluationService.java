package com.example.springhttpclientdatajpademo.domain.chatevaluation.service;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.task.model.Task;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service for task evaluation business logic
 * Contains complex business rules that don't naturally fit in entities
 */
public interface TaskEvaluationService {
    
    /**
     * Evaluate a single input against an actual answer
     * 
     * @param input the evaluation input with question and golden answer
     * @param actualAnswer the actual answer to evaluate
     * @return evaluation output with similarity score
     */
    ChatEvaluationOutput evaluateInput(ChatEvaluationInput input, String actualAnswer);
    
    /**
     * Evaluate multiple inputs for a task
     * 
     * @param task the task to evaluate
     * @param actualAnswers list of actual answers corresponding to inputs
     * @return list of evaluation outputs
     */
    List<ChatEvaluationOutput> evaluateTask(Task task, List<String> actualAnswers);
    
    /**
     * Calculate overall task score based on individual evaluations
     * 
     * @param outputs list of evaluation outputs
     * @return overall task score (average of individual scores)
     */
    BigDecimal calculateOverallScore(List<ChatEvaluationOutput> outputs);
    
    /**
     * Check if task evaluation meets quality threshold
     * 
     * @param outputs list of evaluation outputs
     * @param threshold minimum acceptable score (0.0 to 1.0)
     * @return true if task meets quality threshold
     */
    boolean meetsQualityThreshold(List<ChatEvaluationOutput> outputs, BigDecimal threshold);
    
    /**
     * Generate evaluation summary report
     * 
     * @param task the evaluated task
     * @param outputs list of evaluation outputs
     * @return evaluation summary as string
     */
    String generateEvaluationSummary(Task task, List<ChatEvaluationOutput> outputs);
} 