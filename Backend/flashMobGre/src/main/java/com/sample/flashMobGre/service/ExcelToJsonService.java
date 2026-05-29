package com.sample.flashMobGre.service;

import com.sample.flashMobGre.model.WordModel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelToJsonService {


    private int id = 1;
    public List<WordModel> readExcel(String filePath){
        List<WordModel> WordModelList = new ArrayList<>();


        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip the header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                WordModel WordModel = new WordModel();

                WordModel.setId(id++);
                WordModel.setWord(row.getCell(0).getStringCellValue());
                WordModel.setMarathiMeaning(row.getCell(1).getStringCellValue());
                WordModel.setEnglishMeaning(row.getCell(2).getStringCellValue());
                WordModel.setSampleSentence(row.getCell(3).getStringCellValue());

                WordModelList.add(WordModel);
            }
            id = 1;
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }

        return WordModelList;
    }

    public String generateJsonFromWordModel(List<WordModel> WordModelList) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (WordModel WordModel : WordModelList) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("word", WordModel.getWord())
                    .add("Marathi Meaning", WordModel.getMarathiMeaning())
                    .add("English Meaning",WordModel.getEnglishMeaning())
                    .add("Sample Sentence", WordModel.getSampleSentence());

            jsonArrayBuilder.add(jsonObjectBuilder);
        }

        return jsonArrayBuilder.build().toString();
    }
}
