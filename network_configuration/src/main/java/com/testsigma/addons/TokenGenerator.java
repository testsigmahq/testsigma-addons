package com.testsigma.addons;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenGenerator {
    public static String generateBase64Token(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}
