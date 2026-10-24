package com.studio.app;

/**
 * Точка входа для запуска из IntelliJ IDEA (Run 'Launcher.main()').
 * <p>
 * Проект не модульный (нет module-info.java). Если запускать напрямую класс,
 * унаследованный от {@link javafx.application.Application}, JVM может сообщить
 * «JavaFX runtime components are missing». Отдельный класс без наследования
 * от Application обходит эту проверку.
 * Через Maven ({@code mvn javafx:run}) запускается {@link StudioApplication}.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        StudioApplication.main(args);
    }
}
