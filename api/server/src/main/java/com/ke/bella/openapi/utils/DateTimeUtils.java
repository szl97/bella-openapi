package com.ke.bella.openapi.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    
    public static String getCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.format(MONTH_FORMATTER);
    }
    
}