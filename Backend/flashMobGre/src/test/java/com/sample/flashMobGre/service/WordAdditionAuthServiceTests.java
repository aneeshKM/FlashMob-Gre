package com.sample.flashMobGre.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordAdditionAuthServiceTests {

    @Test
    void rejectsAllPasswordsWhenPasswordIsNotConfigured() {
        WordAdditionAuthService service = new WordAdditionAuthService("");

        assertFalse(service.isConfigured());
        assertFalse(service.isAuthorized("anything"));
    }

    @Test
    void authorizesOnlyMatchingPassword() {
        WordAdditionAuthService service = new WordAdditionAuthService("correct horse battery staple");

        assertTrue(service.isConfigured());
        assertTrue(service.isAuthorized("correct horse battery staple"));
        assertFalse(service.isAuthorized("wrong password"));
        assertFalse(service.isAuthorized(null));
    }
}
