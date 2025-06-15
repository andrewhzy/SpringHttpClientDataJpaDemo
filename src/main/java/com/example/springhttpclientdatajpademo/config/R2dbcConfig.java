package com.example.springhttpclientdatajpademo.config;

import com.example.springhttpclientdatajpademo.enums.TaskStatus;
import com.example.springhttpclientdatajpademo.enums.TaskType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.example.springhttpclientdatajpademo.repository")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converters = Arrays.asList(
            new TaskStatusReadingConverter(),
            new TaskStatusWritingConverter(),
            new TaskTypeReadingConverter(),
            new TaskTypeWritingConverter(),
            new JsonNodeReadingConverter(),
            new JsonNodeWritingConverter()
        );
        return new R2dbcCustomConversions(getStoreConversions(), converters);
    }

    @ReadingConverter
    public static class TaskStatusReadingConverter implements Converter<String, TaskStatus> {
        @Override
        public TaskStatus convert(String source) {
            return source != null ? TaskStatus.fromValue(source) : null;
        }
    }

    @WritingConverter
    public static class TaskStatusWritingConverter implements Converter<TaskStatus, String> {
        @Override
        public String convert(TaskStatus source) {
            return source != null ? source.getValue() : null;
        }
    }

    @ReadingConverter
    public static class TaskTypeReadingConverter implements Converter<String, TaskType> {
        @Override
        public TaskType convert(String source) {
            return source != null ? TaskType.fromValue(source) : null;
        }
    }

    @WritingConverter
    public static class TaskTypeWritingConverter implements Converter<TaskType, String> {
        @Override
        public String convert(TaskType source) {
            return source != null ? source.getValue() : null;
        }
    }

    @ReadingConverter
    public class JsonNodeReadingConverter implements Converter<String, JsonNode> {
        @Override
        public JsonNode convert(String source) {
            try {
                return source != null ? objectMapper.readTree(source) : null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert JSON string to JsonNode", e);
            }
        }
    }

    @WritingConverter
    public class JsonNodeWritingConverter implements Converter<JsonNode, String> {
        @Override
        public String convert(JsonNode source) {
            try {
                return source != null ? objectMapper.writeValueAsString(source) : null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert JsonNode to JSON string", e);
            }
        }
    }
} 