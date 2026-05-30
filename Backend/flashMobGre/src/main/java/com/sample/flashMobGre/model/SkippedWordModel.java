package com.sample.flashMobGre.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkippedWordModel {
    private int rowNumber;
    private String word;
    private String reason;
}
