package com.sample.flashMobGre.controller;

import com.sample.flashMobGre.model.AddWordRequest;
import com.sample.flashMobGre.model.WordImportResult;
import com.sample.flashMobGre.model.WordModel;
import com.sample.flashMobGre.service.DuplicateWordException;
import com.sample.flashMobGre.service.ExcelToJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class WordDataController {

    @Autowired
    private ExcelToJsonService excelToJsonService;

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

    @PostMapping("/addWord")
    public ResponseEntity<?> addWord(@RequestBody AddWordRequest request) {
        try {
            WordModel addedWord = excelToJsonService.addWord(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedWord);
        } catch (DuplicateWordException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/addWordsFile")
    public ResponseEntity<?> addWordsFile(@RequestParam(name = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is required."));
        }

        try {
            WordImportResult result = excelToJsonService.addWordsFromFile(
                    file.getOriginalFilename(),
                    file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to import words from file."));
        }
    }
}
