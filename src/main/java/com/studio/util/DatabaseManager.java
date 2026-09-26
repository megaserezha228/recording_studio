package com.studio.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Менеджер подключения к БД.
 * Загружает параметры подключения из db.properties
 * и предоставляет соединения JDBC.
 */
public final class DatabaseManager {

    private static final String CONFIG_FILE = "db.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (in == null) {
                throw new IllegalStateException(
                        "Файл конфигурации не найден в classpath: " + CONFIG_FILE);
            }
            props.load(in);

            String driver = props.getProperty("db.driver");
            if (driver != null && !driver.isBlank()) {
                Class.forName(driver);
            }

            URL      = props.getProperty("db.url");
            USER     = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            if (URL == null || USER == null || PASSWORD == null) {
                throw new IllegalStateException(
                        "В db.properties должны быть заданы db.url, db.user, db.password");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Не удалось инициализировать DatabaseManager: " + e.getMessage());
        }
    }

    private DatabaseManager() {
        // утилитный класс
    }

    /**
     * Возвращает новое соединение с БД.
     * Каждый вызов открывает новое соединение — закрывать
     * нужно через try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Проверка доступности БД (используется при старте приложения).
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }
}