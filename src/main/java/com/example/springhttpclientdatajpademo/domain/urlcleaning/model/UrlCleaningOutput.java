package com.example.springhttpclientdatajpademo.domain.urlcleaning.model;

import com.example.springhttpclientdatajpademo.domain.Output;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "urlCleaning_outputs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder        
public class UrlCleaningOutput implements Output {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
    @Column(name = "url", nullable = false)
    private String url;
    
    // TODO: Implement the logic to parse the Excel file and return a list of UrlCleaningOutput objects
}
