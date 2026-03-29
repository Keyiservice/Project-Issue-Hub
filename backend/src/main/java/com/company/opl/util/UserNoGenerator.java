package com.company.opl.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class UserNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    private UserNoGenerator() {
    }

    public static String generate() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "U" + LocalDate.now().format(FORMATTER) + suffix;
    }
}
