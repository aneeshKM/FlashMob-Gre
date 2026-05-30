package com.sample.flashMobGre.model;

import lombok.Data;

@Data
public class AddWordRequest {
    private String word;
    private String marathiMeaning;
    private String englishMeaning;
    private String sampleSentence;
}
