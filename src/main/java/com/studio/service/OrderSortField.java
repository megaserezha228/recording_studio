package com.studio.service;

/**
 * Поля, по которым можно отсортировать список заказов в главном окне.
 */
public enum OrderSortField {
    RECORDING_DATE("Дата записи"),
    PRICE("Стоимость"),
    TITLE("Название"),
    CLIENT("Клиент"),
    CREATED_AT("Дата создания");

    private final String title;

    OrderSortField(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
