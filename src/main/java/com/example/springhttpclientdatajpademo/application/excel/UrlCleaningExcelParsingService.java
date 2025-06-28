package com.example.springhttpclientdatajpademo.application.excel;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import com.example.springhttpclientdatajpademo.domain.urlcleaning.model.UrlCleaningTaskItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UrlCleaningExcelParsingService implements ExcelParsingService<UrlCleaningTaskItem> {

    private final TaskType TASK_TYPE = TaskType.URL_CLEANING;

    @Override
    public Map<String, List<UrlCleaningTaskItem>> parseExcelFile(MultipartFile file) {
        // TODO: Implement the logic to parse the Excel file and return a list of UrlCleaningInput objects
        return new HashMap<>();
    }

    @Override
    public void validateExcelFile(MultipartFile file) {
        // TODO Auto-generated method stub
    }

    @Override
    public TaskType getTaskType() {
        return TASK_TYPE;
    }
}
