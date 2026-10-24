package com.studio.model;

/**
 * Статус заказа на запись.
 * name() хранится в БД, title — показывается пользователю.
 */
public enum OrderStatus {
    CREATED("Создан"),
    CONFIRMED("Подтверждён"),
    IN_PROGRESS("В работе"),
    COMPLETED("Завершён"),
    CANCELLED("Отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Бизнес-правило: разрешены только определённые переходы между статусами.
     * COMPLETED и CANCELLED — финальные состояния.
     */
    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) return false;
        return switch (this) {
            case CREATED     -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED   -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED   -> false;
            case CANCELLED   -> false;
        };
    }

    /** Заказ ещё не завершён и не отменён. */
    public boolean isActive() {
        return this == CREATED || this == CONFIRMED || this == IN_PROGRESS;
    }

    @Override
    public String toString() {
        return title;
    }
}
