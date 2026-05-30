package com.sample.flashMobGre.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WordImportResult {
    private int totalRows;
    private List<WordModel> addedWords = new ArrayList<>();
    private List<SkippedWordModel> skippedWords = new ArrayList<>();

    public int getAddedCount() {
        return addedWords.size();
    }

    public int getSkippedCount() {
        return skippedWords.size();
    }
}
