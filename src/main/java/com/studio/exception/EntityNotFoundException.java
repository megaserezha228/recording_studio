package com.studio.exception;

/**
 * Исключение: запись не найдена по идентификатору.
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}