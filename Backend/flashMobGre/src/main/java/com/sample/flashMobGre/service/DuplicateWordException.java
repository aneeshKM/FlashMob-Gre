package com.sample.flashMobGre.service;

public class DuplicateWordException extends RuntimeException {

    public DuplicateWordException(String word) {
        super("The word '" + word + "' already exists in the sheet.");
    }
}
