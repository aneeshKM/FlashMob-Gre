package com.sample.flashMobGre.service;

import com.sample.flashMobGre.model.AddWordRequest;
import com.sample.flashMobGre.model.WordImportResult;
import com.sample.flashMobGre.model.WordModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExcelToJsonServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void addWordAppendsToSheetAndRejectsNormalizedDuplicate() throws Exception {
        Path sourceSheet = Path.of("src/main/resources/Words.xlsx");
        Path testSheet = tempDir.resolve("Words.xlsx");
        Files.copy(sourceSheet, testSheet);

        ExcelToJsonService service = new ExcelToJsonService(testSheet.toString());
        List<WordModel> existingWords = service.readExcel();
        AddWordRequest request = new AddWordRequest();
        request.setWord("  Codex Test Word  ");
        request.setMarathiMeaning("चाचणी अर्थ");
        request.setEnglishMeaning("Test English meaning");
        request.setSampleSentence("This is a test sample sentence.");

        WordModel addedWord = service.addWord(request);
        List<WordModel> updatedWords = service.readExcel();

        assertEquals("Codex Test Word", addedWord.getWord());
        assertEquals(existingWords.size() + 1, updatedWords.size());
        assertEquals("Codex Test Word", updatedWords.get(updatedWords.size() - 1).getWord());

        AddWordRequest duplicateRequest = new AddWordRequest();
        duplicateRequest.setWord("codex test word");
        duplicateRequest.setMarathiMeaning("दुसरा अर्थ");
        duplicateRequest.setSampleSentence("Duplicate sentence.");

        assertThrows(DuplicateWordException.class, () -> service.addWord(duplicateRequest));
    }

    @Test
    void addWordRejectsNonDevanagariMarathiMeaning() throws Exception {
        Path sourceSheet = Path.of("src/main/resources/Words.xlsx");
        Path testSheet = tempDir.resolve("Words.xlsx");
        Files.copy(sourceSheet, testSheet);

        ExcelToJsonService service = new ExcelToJsonService(testSheet.toString());
        AddWordRequest request = new AddWordRequest();
        request.setWord("Codex Language Check Word");
        request.setMarathiMeaning("Not Marathi");
        request.setEnglishMeaning("Test English meaning");
        request.setSampleSentence("This is a test sample sentence.");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.addWord(request));

        assertEquals("Marathi meaning must be written in Devanagari script.", exception.getMessage());
    }

    @Test
    void addWordsFromCsvAddsOnlyUniqueRows() throws Exception {
        Path sourceSheet = Path.of("src/main/resources/Words.xlsx");
        Path testSheet = tempDir.resolve("Words.xlsx");
        Files.copy(sourceSheet, testSheet);

        ExcelToJsonService service = new ExcelToJsonService(testSheet.toString());
        List<WordModel> existingWords = service.readExcel();
        String csv = """
                Word,Marathi Meaning,English Meaning,Sample Sentence
                Intrigue,कुतूहल,Duplicate English,Duplicate sentence
                Codex Bulk Word,चाचणी अर्थ,Bulk English,Bulk sentence
                codex bulk word,दुसरा अर्थ,Bulk English 2,Bulk sentence 2
                """;

        WordImportResult result = service.addWordsFromFile(
                "words.csv",
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        List<WordModel> updatedWords = service.readExcel();

        assertEquals(3, result.getTotalRows());
        assertEquals(1, result.getAddedCount());
        assertEquals(2, result.getSkippedCount());
        assertEquals(existingWords.size() + 1, updatedWords.size());
        assertEquals("Codex Bulk Word", updatedWords.get(updatedWords.size() - 1).getWord());
    }

    @Test
    void addWordsFromCsvSkipsNonDevanagariMarathiMeaning() throws Exception {
        Path sourceSheet = Path.of("src/main/resources/Words.xlsx");
        Path testSheet = tempDir.resolve("Words.xlsx");
        Files.copy(sourceSheet, testSheet);

        ExcelToJsonService service = new ExcelToJsonService(testSheet.toString());
        String csv = """
                Word,Marathi Meaning,English Meaning,Sample Sentence
                Codex Invalid Marathi,Not Marathi,Invalid English,Invalid sentence
                Codex Valid Marathi,वैध अर्थ,Valid English,Valid sentence
                """;

        WordImportResult result = service.addWordsFromFile(
                "words.csv",
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getAddedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals("Marathi meaning must be written in Devanagari script.",
                result.getSkippedWords().get(0).getReason());
    }
}
