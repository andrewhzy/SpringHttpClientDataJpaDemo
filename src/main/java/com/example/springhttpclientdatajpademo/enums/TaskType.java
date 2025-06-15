package com.example.springhttpclientdatajpademo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskType {
    CHAT_EVALUATION("chat-evaluation");

    private final String value;

    TaskType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskType fromValue(String value) {
        for (TaskType type : TaskType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid task type: " + value);
    }
} 