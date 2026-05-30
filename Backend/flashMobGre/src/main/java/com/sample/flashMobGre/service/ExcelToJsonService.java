package com.sample.flashMobGre.service;

import com.sample.flashMobGre.model.WordModel;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Service
public class ExcelToJsonService {

    public List<WordModel> readExcel() {

        List<WordModel> wordModelList = new ArrayList<>();
        int id = 1; // Initialize ID counter

        try (InputStream inputStream = new ClassPathResource("Words.xlsx").getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            DataFormatter formatter = new DataFormatter();

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip the header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                WordModel wordModel = new WordModel();

                String word = getCellValue(formatter, row, 0);
                if (word.isBlank()) {
                    continue;
                }

                wordModel.setId(id++);
                wordModel.setWord(word);
                wordModel.setMarathiMeaning(getCellValue(formatter, row, 1));
                wordModel.setEnglishMeaning(getCellValue(formatter, row, 2));
                wordModel.setSampleSentence(getCellValue(formatter, row, 3));

                wordModelList.add(wordModel);
            }
            id = 1;
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }

        return wordModelList;
    }

    private String getCellValue(DataFormatter formatter, Row row, int cellIndex) {
        return formatter.formatCellValue(row.getCell(cellIndex)).trim();
    }
}
