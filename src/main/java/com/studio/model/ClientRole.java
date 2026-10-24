package com.studio.model;

/**
 * Роль клиента студии.
 * name() хранится в БД, title — показывается пользователю.
 */
public enum ClientRole {
    SOLO_ARTIST("Сольный исполнитель"),
    BAND("Группа"),
    PRODUCER("Продюсер"),
    LABEL("Лейбл");

    private final String title;

    ClientRole(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
