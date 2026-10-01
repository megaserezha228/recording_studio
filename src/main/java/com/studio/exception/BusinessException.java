package com.studio.exception;

/**
 * Исключение: нарушено бизнес-правило предметной области.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}