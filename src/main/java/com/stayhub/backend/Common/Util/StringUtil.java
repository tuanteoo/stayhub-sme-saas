package com.stayhub.backend.Common.Util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtil {
    public static String normalizeForSearch(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String preProcessed = input
                .replace("Đ", "d")
                .replace("đ", "d")
                .replace("Ð", "d");

        String normalized = Normalizer.normalize(preProcessed, Normalizer.Form.NFD);
        String noAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized)
                .replaceAll("");

        return noAccents.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public static String maskString(String input, int startKeep, int endKeep) {
        if (input == null || input.length() <= (startKeep + endKeep)) {
            return input;
        }
        String start = input.substring(0, startKeep);
        String end = input.substring(input.length() - endKeep);
        String masked = "*".repeat(input.length() - startKeep - endKeep);
        return start + masked + end;
    }
}
