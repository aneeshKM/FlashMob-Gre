package com.sample.flashMobGre.model;

import lombok.Data;

@Data
public class WordModel {
    private int id;
    private String word;
    private String marathiMeaning;
    private String englishMeaning;
    private String sampleSentence;

}
