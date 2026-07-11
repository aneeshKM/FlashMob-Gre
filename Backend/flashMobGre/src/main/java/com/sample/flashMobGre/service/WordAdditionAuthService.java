package com.sample.flashMobGre.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class WordAdditionAuthService {

    public static final String PASSWORD_HEADER = "X-Word-Addition-Password";

    private final String configuredPassword;

    public WordAdditionAuthService(@Value("${app.word-addition-password:}") String configuredPassword) {
        this.configuredPassword = configuredPassword;
    }

    public boolean isConfigured() {
        return configuredPassword != null && !configuredPassword.isBlank();
    }

    public boolean isAuthorized(String providedPassword) {
        if (!isConfigured() || providedPassword == null) {
            return false;
        }

        byte[] expectedPassword = configuredPassword.getBytes(StandardCharsets.UTF_8);
        byte[] actualPassword = providedPassword.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expectedPassword, actualPassword);
    }
}
