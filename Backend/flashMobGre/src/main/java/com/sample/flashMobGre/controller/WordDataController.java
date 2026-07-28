package com.sample.flashMobGre.controller;

import com.sample.flashMobGre.model.AddWordRequest;
import com.sample.flashMobGre.model.WordImportResult;
import com.sample.flashMobGre.model.WordModel;
import com.sample.flashMobGre.service.DuplicateWordException;
import com.sample.flashMobGre.service.ExcelToJsonService;
import com.sample.flashMobGre.service.WordAdditionAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class WordDataController {

    @Autowired
    private ExcelToJsonService excelToJsonService;

    @Autowired
    private WordAdditionAuthService wordAdditionAuthService;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

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
    public ResponseEntity<?> addWord(
            @RequestHeader(name = WordAdditionAuthService.PASSWORD_HEADER, required = false) String password,
            @RequestBody AddWordRequest request) {
        ResponseEntity<?> authFailure = validateWordAdditionPassword(password);
        if (authFailure != null) {
            return authFailure;
        }

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
    public ResponseEntity<?> addWordsFile(
            @RequestHeader(name = WordAdditionAuthService.PASSWORD_HEADER, required = false) String password,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        ResponseEntity<?> authFailure = validateWordAdditionPassword(password);
        if (authFailure != null) {
            return authFailure;
        }

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

    private ResponseEntity<?> validateWordAdditionPassword(String password) {
        if (!wordAdditionAuthService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Word addition password is not configured."));
        }

        if (!wordAdditionAuthService.isAuthorized(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid word addition password."));
        }

        return null;
    }
}
