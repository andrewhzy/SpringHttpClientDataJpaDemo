package com.example.springhttpclientdatajpademo.application.excel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.springhttpclientdatajpademo.domain.urlCleaning.model.UrlCleaningInput;

@Service
public class UrlCleaningExcelParsingService implements ExcelParsingService {

    @Override
    public List<UrlCleaningInput> parseExcelFile(MultipartFile file) {
        // TODO: Implement the logic to parse the Excel file and return a list of UrlCleaningInput objects
        return new ArrayList<>();
    }

    @Override
    public void validateExcelFile(MultipartFile file) {
        // TODO Auto-generated method stub
    }
}
