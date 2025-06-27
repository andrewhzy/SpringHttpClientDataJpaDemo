package com.example.springhttpclientdatajpademo.config;

import com.example.springhttpclientdatajpademo.domain.task.Task;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for custom converters and formatters
 * Manages type conversion for request parameters from kebab-case to UPPER_CASE enums
 */
@Configuration
@Slf4j
public class ConverterConfig implements WebMvcConfigurer {

    /**
     * Create TaskTypeConverter bean for automatic string to TaskType conversion
     * Converts kebab-case strings (e.g., "chat-evaluation") to UPPER_CASE enums (e.g., CHAT_EVALUATION)
     * 
     * @return TaskTypeConverter instance
     */
    @Bean
    public Converter<String, Task.TaskType> taskTypeConverter() {
        log.info("Creating TaskTypeConverter bean for kebab-case to UPPER_CASE conversion");
        return new Converter<String, Task.TaskType>() {
            @Override
            public Task.TaskType convert(@NonNull String source) {
                // Convert kebab-case to UPPER_CASE enum name
                String upperCaseName = source.toUpperCase().replace('-', '_');
                
                try {
                    return Task.TaskType.valueOf(upperCaseName);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid task type: " + source + 
                        ". Valid values: " + java.util.Arrays.toString(Task.TaskType.values()));
                }
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
        log.info("TaskTypeConverter registered for automatic kebab-case to UPPER_CASE conversion");
    }
} 