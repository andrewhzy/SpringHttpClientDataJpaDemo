package com.example.springhttpclientdatajpademo.config;

import com.example.springhttpclientdatajpademo.application.service.TaskTypeValidationService;
import com.example.springhttpclientdatajpademo.domain.task.Task;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for custom converters and formatters
 * Manages type conversion for request parameters with configuration-based validation
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ConverterConfig implements WebMvcConfigurer {
    
    private final TaskTypeValidationService taskTypeValidationService;

        /**
     * Create TaskTypeConverter bean for automatic string to TaskType conversion
     * Uses configuration-based validation to ensure only enabled task types are accepted
     * 
     * @return TaskTypeConverter instance
     */
    @Bean
    public Converter<String, Task.TaskType> taskTypeConverter() {
        log.info("Creating TaskTypeConverter bean with configuration-based validation");
        return new Converter<String, Task.TaskType>() {
            @Override
            public Task.TaskType convert(@NonNull String source) {
                // First validate against configuration (enabled types)
                taskTypeValidationService.validateTaskType(source);
                
                // Then convert to enum
                return Task.TaskType.fromValue(source);
            }
        };
    }

    /**
     * Register the TaskTypeConverter with Spring's FormatterRegistry
     * This enables automatic conversion for @RequestParam TaskType parameters
     *
     * @param registry Spring's formatter registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(taskTypeConverter());
        log.info("TaskTypeConverter registered for automatic string to TaskType conversion");
    }
} 