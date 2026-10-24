package com.studio.config;

import com.studio.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Подключение к PostgreSQL через JDBC.
 * <p>
 * Параметры (url, user, password) читаются из файла {@code database.properties}
 * в ресурсах проекта — пароль не хранится в Java-коде.
 * Каждый вызов {@link #getConnection()} открывает новое соединение,
 * поэтому соединение закрывается через try-with-resources в DAO.
 */
public final class DatabaseManager {

    private static final String CONFIG_FILE = "database.properties";

    private static Properties config;

    private DatabaseManager() {
        // утилитный класс
    }

    /** Ленивая загрузка database.properties (один раз за запуск). */
    private static synchronized Properties config() {
        if (config != null) {
            return config;
        }
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new DatabaseException("Не найден файл настроек " + CONFIG_FILE, null);
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать файл настроек " + CONFIG_FILE, e);
        }
        for (String key : new String[]{"db.url", "db.user", "db.password"}) {
            if (props.getProperty(key) == null) {
                throw new DatabaseException("В " + CONFIG_FILE + " не задан параметр " + key, null);
            }
        }
        config = props;
        return config;
    }

    /** Новое JDBC-соединение. Закрывать — через try-with-resources. */
    public static Connection getConnection() throws SQLException {
        Properties props = config();
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    /**
     * Проверка доступности БД при запуске и по кнопке «Обновить».
     *
     * @throws DatabaseException с понятным сообщением, если подключиться не удалось
     */
    public static void checkConnection() {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(3)) {
                throw new DatabaseException("Соединение с базой данных недоступно", null);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось подключиться к базе данных " + describe(), e);
        }
    }

    /** Короткое описание подключения для строки состояния: localhost:5432/recording_studio. */
    public static String describe() {
        String url = config().getProperty("db.url");
        String withoutPrefix = url.replaceFirst("^jdbc:postgresql://", "");
        int queryStart = withoutPrefix.indexOf('?');
        return queryStart >= 0 ? withoutPrefix.substring(0, queryStart) : withoutPrefix;
    }
}
