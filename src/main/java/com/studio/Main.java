package com.studio;

import com.studio.repository.ClientRepository;
import com.studio.repository.RecordingOrderRepository;
import com.studio.repository.impl.ClientRepositoryImpl;
import com.studio.repository.impl.RecordingOrderRepositoryImpl;
import com.studio.service.ClientService;
import com.studio.service.RecordingOrderService;
import com.studio.ui.ConsoleUI;
import com.studio.util.DatabaseManager;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Точка входа приложения «Студия звукозаписи».
 *
 * Слои:
 *   ConsoleUI → Service → Repository → JDBC → PostgreSQL
 */
public class Main {

    /** Определяем ОС: на Windows используем Cp866, на остальных — UTF-8. */
    private static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase().contains("win");

    private static final Charset CONSOLE_CHARSET = IS_WINDOWS
            ? Charset.forName("Cp866")
            : StandardCharsets.UTF_8;

    public static void main(String[] args) {
        // Настраиваем вывод консоли в правильной кодировке,
        // чтобы русские буквы отображались корректно
        // и в cmd/PowerShell с chcp 866, и на Linux/macOS.
        try {
            System.setOut(new PrintStream(
                    new FileOutputStream(FileDescriptor.out),
                    true,
                    CONSOLE_CHARSET));

            System.setErr(new PrintStream(
                    new FileOutputStream(FileDescriptor.err),
                    true,
                    CONSOLE_CHARSET));
        } catch (Exception e) {
            // Если не удалось — работаем со стандартными настройками
            // (не критично для работы программы)
        }

        System.out.println("Подключение к БД...");
        System.out.println("URL: " + DatabaseManager.getUrl());

        if (!DatabaseManager.testConnection()) {
            System.err.println("Не удалось подключиться к базе данных.");
            System.err.println("Проверьте параметры в src/main/resources/db.properties");
            System.err.println("и убедитесь, что PostgreSQL запущен, а БД recording_studio создана.");
            return;
        }
        System.out.println("Подключение к БД успешно.");

        // ===== Сборка слоёв (Dependency Injection вручную) =====

        // Repository (слой доступа к данным)
        ClientRepository clientRepository = new ClientRepositoryImpl();
        RecordingOrderRepository orderRepository = new RecordingOrderRepositoryImpl();

        // Service (бизнес-логика)
        ClientService clientService = new ClientService(clientRepository);
        RecordingOrderService orderService = new RecordingOrderService(orderRepository, clientRepository);

        // UI (слой представления)
        ConsoleUI ui = new ConsoleUI(clientService, orderService);

        // ===== Запуск =====
        ui.start();
    }
}