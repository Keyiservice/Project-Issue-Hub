package com.company.opl.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class IssueNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private IssueNoGenerator() {
    }

    public static String generate() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ISS" + LocalDate.now().format(FORMATTER) + suffix;
    }
}
