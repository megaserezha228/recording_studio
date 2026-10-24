package com.studio.model;

/**
 * Приоритет заказа.
 * name() (LOW, MEDIUM, HIGH) хранится в БД, title — показывается пользователю.
 */
public enum Priority {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий");

    private final String title;

    Priority(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /** toString() используется ComboBox и TableView для отображения значения. */
    @Override
    public String toString() {
        return title;
    }
}
