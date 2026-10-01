package com.studio.exception;

/**
 * Исключение: ошибка при работе с базой данных (подключение, SQL).
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}