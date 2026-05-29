package com.sample.flashMobGre.controller;

import com.sample.flashMobGre.model.WordModel;
import com.sample.flashMobGre.service.ExcelToJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WordDataController {

    @Autowired
    private ExcelToJsonService excelToJsonService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getWordData")
    public ResponseEntity<List<WordModel>> getWordData() {
        String filePath = "/Users/Aneesh/Aneesh/Code/FlashMob-Gre/Backend/flashMobGre/src/main/java/com/sample/flashMobGre/service/Words.xlsx"; // Replace with the actual file path

        List<WordModel> wordDataList = excelToJsonService.readExcel(filePath);
        return ResponseEntity.ok(wordDataList);
    }
}
