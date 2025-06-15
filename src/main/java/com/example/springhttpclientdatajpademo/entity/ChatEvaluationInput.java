package com.example.springhttpclientdatajpademo.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_evaluation_input")
public class ChatEvaluationInput {
    
    @Id
    private Long id;
    
    @Column("task_id")
    private UUID taskId;
    
    @Column("row_number")
    private Integer rowNumber;
    
    @Column("question")
    private String question;
    
    @Column("golden_answer")
    private String goldenAnswer;
    
    @Column("golden_citations")
    private JsonNode goldenCitations;
    
    @Column("metadata")
    private JsonNode metadata;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
} 