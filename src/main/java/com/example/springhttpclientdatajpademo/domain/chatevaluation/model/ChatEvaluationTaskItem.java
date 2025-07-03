package com.example.springhttpclientdatajpademo.domain.chatevaluation.model;

import com.example.springhttpclientdatajpademo.domain.TaskItem;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing input data for chat evaluation tasks
 * Contains questions, golden answers, and citations
 * <p>
 * Following Effective Java principles:
 * - Item 11: Override equals and hashCode properly for JPA entities        
 * - Item 17: Minimize mutability where possible
 */
@Entity
@Table(name = "chat_evaluation_task_items", indexes = {
        @Index(name = "idx_chat_eval_task_item_task_id", columnList = "task_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requirement
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder usage only
@Builder
public class ChatEvaluationTaskItem implements TaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Task task;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "expected_answer", nullable = false, columnDefinition = "TEXT")
    private String expectedAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_docs", columnDefinition = "json")
    private List<String> expectedDocs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}