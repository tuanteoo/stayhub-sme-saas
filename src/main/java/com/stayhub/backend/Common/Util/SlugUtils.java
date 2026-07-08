package com.stayhub.backend.Common.Util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String preProcessed = input.replace("Đ", "D").replace("đ", "d").replace("Ð", "d");
        String nowhitespace = WHITESPACE.matcher(preProcessed).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).
                replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
