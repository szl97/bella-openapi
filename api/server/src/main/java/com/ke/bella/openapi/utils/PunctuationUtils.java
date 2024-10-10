package com.ke.bella.openapi.utils;

public class PunctuationUtils {
    public static boolean endsWithPunctuation(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        String regex = ".*[，；。,.?？！!]$";
        return input.matches(regex);
    }
}
