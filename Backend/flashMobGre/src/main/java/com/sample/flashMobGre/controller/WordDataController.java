package com.sample.flashMobGre.controller;

import com.sample.flashMobGre.model.WordModel;
import com.sample.flashMobGre.service.ExcelToJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WordDataController {

    @Autowired
    private ExcelToJsonService excelToJsonService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getWordData")
    public ResponseEntity<List<WordModel>> getWordData(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        List<WordModel> wordDataList = excelToJsonService.readExcel();

        if (offset == null && limit == null) {
            return ResponseEntity.ok(wordDataList);
        }

        int startIndex = Math.max(offset == null ? 0 : offset, 0);
        int requestedLimit = Math.max(limit == null ? wordDataList.size() : limit, 0);

        if (startIndex >= wordDataList.size() || requestedLimit == 0) {
            return ResponseEntity.ok(List.of());
        }

        int endIndex = Math.min(startIndex + requestedLimit, wordDataList.size());
        List<WordModel> selectedWords = wordDataList.subList(startIndex, endIndex);

        return ResponseEntity.ok(selectedWords);
    }
}
