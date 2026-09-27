package com.studio.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;



public class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value < min || value > max) {
                System.out.println("Ошибка: число должно быть в диапазоне "
                        + min + ".." + max + ".");
                continue;
            }
            return value;
        }
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("Ошибка: значение не может быть пустым.");
                continue;
            }
            return line;
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (формат ГГГГ-ММ-ДД): ");
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: неверный формат даты.");
            }
        }
    }

    public <E extends Enum<E>> E readEnum(String prompt, Class<E> enumClass) {
        E[] values = enumClass.getEnumConstants();
        while (true) {
            System.out.println(prompt);
            for (int i = 0; i < values.length; i++) {
                System.out.println("  " + (i + 1) + ". " + values[i]);
            }
            int choice = readInt("Выберите номер: ");
            if (choice < 1 || choice > values.length) {
                System.out.println("Ошибка: неверный пункт.");
                continue;
            }
            return values[choice - 1];
        }
    }

    public String readLine() {
        return scanner.nextLine();
    }
}