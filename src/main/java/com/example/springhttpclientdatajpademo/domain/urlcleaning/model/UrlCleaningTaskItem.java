package com.example.springhttpclientdatajpademo.domain.urlcleaning.model;

import com.example.springhttpclientdatajpademo.domain.TaskItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "url_cleaning_task_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UrlCleaningTaskItem implements TaskItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
    @Column(name = "url", nullable = false)
    private String url;
    

    // TODO: Implement the logic to parse the Excel file and return a list of UrlCleaningInput objects
}
