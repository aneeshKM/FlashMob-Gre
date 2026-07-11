package com.sample.flashMobGre.service;

import com.sample.flashMobGre.model.AddWordRequest;
import com.sample.flashMobGre.model.SkippedWordModel;
import com.sample.flashMobGre.model.WordImportResult;
import com.sample.flashMobGre.model.WordModel;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ExcelToJsonService {

    private static final String MARATHI_LANGUAGE_ERROR = "Marathi meaning must be written in Devanagari script.";

    private final Path writableExcelPath;

    public ExcelToJsonService(@Value("${words.excel.path:src/main/resources/Words.xlsx}") String excelPath) {
        this.writableExcelPath = Path.of(excelPath);
    }

    @PostConstruct
    void initializeWritableExcel() {
        if (Files.exists(writableExcelPath)) {
            return;
        }

        try {
            Path parent = writableExcelPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (InputStream source = new ClassPathResource("Words.xlsx").getInputStream()) {
                Files.copy(source, writableExcelPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize the writable word sheet.", e);
        }
    }

    public synchronized List<WordModel> readExcel() {

        List<WordModel> wordModelList = new ArrayList<>();
        int id = 1; // Initialize ID counter

        try (InputStream inputStream = openWordsInputStream();
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

    public synchronized WordModel addWord(AddWordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Word details are required.");
        }

        WordImportResult result = addUniqueRows(List.of(new ImportWordRow(1, request)));

        if (!result.getAddedWords().isEmpty()) {
            return result.getAddedWords().get(0);
        }

        SkippedWordModel skippedWord = result.getSkippedWords().get(0);
        if ("Already exists in sheet.".equals(skippedWord.getReason())) {
            throw new DuplicateWordException(normalizeWord(request.getWord()));
        }

        throw new IllegalArgumentException(skippedWord.getReason());
    }

    public synchronized WordImportResult addWordsFromFile(String fileName, InputStream inputStream) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required.");
        }

        try {
            String normalizedFileName = fileName.toLowerCase(Locale.ROOT);

            if (normalizedFileName.endsWith(".csv")) {
                return addUniqueRows(readCsvRows(inputStream));
            }

            if (normalizedFileName.endsWith(".xlsx") || normalizedFileName.endsWith(".xls")) {
                return addUniqueRows(readWorkbookRows(inputStream));
            }

            throw new IllegalArgumentException("Only .xlsx, .xls, and .csv files are supported.");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read uploaded file.", e);
        }
    }

    private WordImportResult addUniqueRows(List<ImportWordRow> importRows) {
        WordImportResult result = new WordImportResult();
        result.setTotalRows(importRows.size());

        List<WordModel> existingWords = readExcel();
        Set<String> knownWordKeys = new HashSet<>();

        for (WordModel existingWord : existingWords) {
            knownWordKeys.add(normalizeWordKey(existingWord.getWord()));
        }

        List<AddWordRequest> acceptedRequests = new ArrayList<>();

        for (ImportWordRow importRow : importRows) {
            AddWordRequest normalizedRequest = normalizeRequest(importRow.request());
            String validationError = getValidationError(normalizedRequest);

            if (validationError != null) {
                result.getSkippedWords().add(new SkippedWordModel(
                        importRow.rowNumber(),
                        normalizedRequest.getWord(),
                        validationError));
                continue;
            }

            String wordKey = normalizeWordKey(normalizedRequest.getWord());
            if (knownWordKeys.contains(wordKey)) {
                result.getSkippedWords().add(new SkippedWordModel(
                        importRow.rowNumber(),
                        normalizedRequest.getWord(),
                        "Already exists in sheet."));
                continue;
            }

            knownWordKeys.add(wordKey);
            acceptedRequests.add(normalizedRequest);
        }

        if (!acceptedRequests.isEmpty()) {
            result.setAddedWords(appendRows(acceptedRequests, existingWords.size() + 1));
        }

        return result;
    }

    private List<WordModel> appendRows(List<AddWordRequest> requests, int startingId) {
        if (!Files.exists(writableExcelPath)) {
            throw new IllegalStateException("Writable word sheet was not found at " + writableExcelPath);
        }

        Workbook workbook;

        try (InputStream inputStream = Files.newInputStream(writableExcelPath)) {
            workbook = WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to open the word sheet.", e);
        }

        List<WordModel> addedWords = new ArrayList<>();

        try (workbook) {
            Sheet sheet = workbook.getSheetAt(0);
            int nextRowNumber = sheet.getLastRowNum() + 1;
            int nextId = startingId;

            for (AddWordRequest request : requests) {
                Row row = sheet.createRow(nextRowNumber++);
                row.createCell(0).setCellValue(request.getWord());
                row.createCell(1).setCellValue(request.getMarathiMeaning());
                row.createCell(2).setCellValue(request.getEnglishMeaning());
                row.createCell(3).setCellValue(request.getSampleSentence());

                WordModel addedWord = new WordModel();
                addedWord.setId(nextId++);
                addedWord.setWord(request.getWord());
                addedWord.setMarathiMeaning(request.getMarathiMeaning());
                addedWord.setEnglishMeaning(request.getEnglishMeaning());
                addedWord.setSampleSentence(request.getSampleSentence());
                addedWords.add(addedWord);
            }

            try (OutputStream outputStream = Files.newOutputStream(writableExcelPath)) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add words to the sheet.", e);
        }

        return addedWords;
    }

    private List<ImportWordRow> readWorkbookRows(InputStream inputStream) throws IOException {
        List<ImportWordRow> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AddWordRequest request = new AddWordRequest();
                request.setWord(getCellValue(formatter, row, 0));
                request.setMarathiMeaning(getCellValue(formatter, row, 1));
                request.setEnglishMeaning(getCellValue(formatter, row, 2));
                request.setSampleSentence(getCellValue(formatter, row, 3));

                if (isBlankRow(request)) {
                    continue;
                }

                rows.add(new ImportWordRow(row.getRowNum() + 1, request));
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read uploaded spreadsheet.", e);
        }

        return rows;
    }

    private List<ImportWordRow> readCsvRows(InputStream inputStream) throws IOException {
        List<ImportWordRow> rows = new ArrayList<>();
        List<List<String>> records = parseCsvRecords(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));

        for (int index = 1; index < records.size(); index += 1) {
            List<String> record = records.get(index);
            AddWordRequest request = new AddWordRequest();
            request.setWord(getCsvColumn(record, 0));
            request.setMarathiMeaning(getCsvColumn(record, 1));
            request.setEnglishMeaning(getCsvColumn(record, 2));
            request.setSampleSentence(getCsvColumn(record, 3));

            if (isBlankRow(request)) {
                continue;
            }

            rows.add(new ImportWordRow(index + 1, request));
        }

        return rows;
    }

    private List<List<String>> parseCsvRecords(String csvContent) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < csvContent.length(); index += 1) {
            char currentChar = csvContent.charAt(index);

            if (inQuotes) {
                if (currentChar == '"') {
                    if (index + 1 < csvContent.length() && csvContent.charAt(index + 1) == '"') {
                        field.append('"');
                        index += 1;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(currentChar);
                }
                continue;
            }

            if (currentChar == '"') {
                inQuotes = true;
            } else if (currentChar == ',') {
                record.add(field.toString());
                field.setLength(0);
            } else if (currentChar == '\n') {
                record.add(field.toString());
                records.add(record);
                record = new ArrayList<>();
                field.setLength(0);
            } else if (currentChar != '\r') {
                field.append(currentChar);
            }
        }

        if (!record.isEmpty() || !field.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }

        return records;
    }

    private String getCsvColumn(List<String> record, int columnIndex) {
        if (columnIndex >= record.size()) {
            return "";
        }

        return record.get(columnIndex);
    }

    private AddWordRequest normalizeRequest(AddWordRequest request) {
        AddWordRequest normalizedRequest = new AddWordRequest();

        if (request == null) {
            return normalizedRequest;
        }

        normalizedRequest.setWord(normalizeWord(request.getWord()));
        normalizedRequest.setMarathiMeaning(normalizeText(request.getMarathiMeaning()));
        normalizedRequest.setEnglishMeaning(normalizeText(request.getEnglishMeaning()));
        normalizedRequest.setSampleSentence(normalizeText(request.getSampleSentence()));

        return normalizedRequest;
    }

    private String getValidationError(AddWordRequest request) {
        if (request.getWord().isBlank()) {
            return "Word is required.";
        }

        if (request.getMarathiMeaning().isBlank()) {
            return "Marathi meaning is required.";
        }

        if (!isDevanagariMeaning(request.getMarathiMeaning())) {
            return MARATHI_LANGUAGE_ERROR;
        }

        if (request.getSampleSentence().isBlank()) {
            return "Sample sentence is required.";
        }

        return null;
    }

    private boolean isDevanagariMeaning(String value) {
        boolean hasDevanagariLetter = false;

        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);

            if (!Character.isLetter(codePoint)) {
                continue;
            }

            if (Character.UnicodeScript.of(codePoint) != Character.UnicodeScript.DEVANAGARI) {
                return false;
            }

            hasDevanagariLetter = true;
        }

        return hasDevanagariLetter;
    }

    private boolean isBlankRow(AddWordRequest request) {
        return normalizeText(request.getWord()).isBlank()
                && normalizeText(request.getMarathiMeaning()).isBlank()
                && normalizeText(request.getEnglishMeaning()).isBlank()
                && normalizeText(request.getSampleSentence()).isBlank();
    }

    private InputStream openWordsInputStream() throws Exception {
        if (Files.exists(writableExcelPath)) {
            return Files.newInputStream(writableExcelPath);
        }

        return new ClassPathResource("Words.xlsx").getInputStream();
    }

    private String getCellValue(DataFormatter formatter, Row row, int cellIndex) {
        return formatter.formatCellValue(row.getCell(cellIndex)).trim();
    }

    private String normalizeWord(String value) {
        return normalizeText(value).replaceAll("\\s+", " ");
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").trim();
    }

    private String normalizeWordKey(String value) {
        return Normalizer.normalize(normalizeWord(value), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
    }

    private record ImportWordRow(int rowNumber, AddWordRequest request) {
    }
}
