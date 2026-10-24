package com.studio.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Форматирование значений для отображения в интерфейсе.
 */
public final class Formatters {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Formatters() {
    }

    public static String date(LocalDate date) {
        return date == null ? "—" : DATE.format(date);
    }

    public static String dateTime(LocalDateTime dateTime) {
        return dateTime == null ? "—" : DATE_TIME.format(dateTime);
    }

    public static String time(LocalDateTime dateTime) {
        return dateTime == null ? "" : TIME.format(dateTime);
    }

    /** 14400 → «14 400 ₽». */
    public static String money(int rubles) {
        return String.format("%,d ₽", rubles).replace(',', ' ');
    }

    /** 1 → «1 час», 3 → «3 часа», 6 → «6 часов». */
    public static String hours(int hours) {
        int mod100 = hours % 100;
        int mod10 = hours % 10;
        String word;
        if (mod100 >= 11 && mod100 <= 14) {
            word = "часов";
        } else if (mod10 == 1) {
            word = "час";
        } else if (mod10 >= 2 && mod10 <= 4) {
            word = "часа";
        } else {
            word = "часов";
        }
        return hours + " " + word;
    }

    public static String orDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
