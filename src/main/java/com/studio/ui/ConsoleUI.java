package com.studio.ui;

import com.studio.exception.BusinessException;
import com.studio.model.*;
import com.studio.service.ClientService;
import com.studio.service.RecordingOrderService;
import com.studio.util.ExcelExporter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Консольный интерфейс информационной системы «Студия звукозаписи».
 * Слой представления: меню, ввод, вывод.
 * Вся бизнес-логика — в сервисах.
 */
public class ConsoleUI {

    // Scanner читает stdin в кодировке консоли:
    //   Windows → Cp866 (OEM-кириллица)
    //   Linux/macOS → UTF-8
    private final Scanner scanner = new Scanner(
            System.in,
            System.getProperty("os.name").toLowerCase().contains("win")
                    ? "Cp866"
                    : "UTF-8");

    private final InputReader in = new InputReader(scanner);

    private final ClientService clientService;
    private final RecordingOrderService orderService;

    public ConsoleUI(ClientService clientService,
                     RecordingOrderService orderService) {
        this.clientService = clientService;
        this.orderService = orderService;
    }

    // ============================================================
    // ГЛАВНОЕ МЕНЮ
    // ============================================================

    public void start() {
        while (true) {
            printMainMenu();
            int choice = in.readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> ExceptionHandler.run(this::clientsMenu);
                case 2 -> ExceptionHandler.run(this::ordersMenu);
                case 3 -> ExceptionHandler.run(this::searchMenu);
                case 4 -> ExceptionHandler.run(this::filterMenu);
                case 5 -> ExceptionHandler.run(this::sortMenu);
                case 6 -> ExceptionHandler.run(this::printStatistics);
                case 7 -> ExceptionHandler.run(this::exportMenu);
                case 8 -> ExceptionHandler.run(this::printDatabaseTables);
                case 0 -> {
                    System.out.println("Выход из программы. До свидания!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("=================================================");
        System.out.println("          СТУДИЯ ЗВУКОЗАПИСИ");
        System.out.println("=================================================");
        System.out.println("1. Клиенты");
        System.out.println("2. Заказы на запись");
        System.out.println("3. Поиск");
        System.out.println("4. Фильтрация");
        System.out.println("5. Сортировка");
        System.out.println("6. Статистика");
        System.out.println("7. Экспорт данных");
        System.out.println("8. Вывести таблицы базы данных");
        System.out.println("0. Выход");
        System.out.println("=================================================");
    }

    // ============================================================
    // МЕНЮ КЛИЕНТОВ
    // ============================================================

    private void clientsMenu() {
        while (true) {
            System.out.println();
            System.out.println("---------- КЛИЕНТЫ ----------");
            System.out.println("1. Создать клиента");
            System.out.println("2. Список клиентов");
            System.out.println("3. Найти клиента по ID");
            System.out.println("4. Изменить клиента");
            System.out.println("5. Удалить клиента");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> createClient();
                case 2 -> printClients(clientService.getAll());
                case 3 -> findClientById();
                case 4 -> updateClient();
                case 5 -> deleteClient();
                case 0 -> { return; }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    private void createClient() {
        String fullName = in.readNonEmptyString("ФИО: ");
        String email = in.readNonEmptyString("Email: ");
        String phone = in.readNonEmptyString("Телефон (+7XXXXXXXXXX): ");
        ClientRole role = in.readEnum("Роль клиента:", ClientRole.class);

        Client created = clientService.create(new Client(fullName, email, phone, role));
        System.out.println("Создан клиент: " + created);
    }

    private void findClientById() {
        int id = in.readInt("ID клиента: ");
        Client client = clientService.getById(id);
        System.out.println(client);
    }

    private void updateClient() {
        int id = in.readInt("ID клиента для изменения: ");
        Client client = clientService.getById(id);

        String fullName = in.readNonEmptyString("Новое ФИО (" + client.getFullName() + "): ");
        String email = in.readNonEmptyString("Новый email (" + client.getEmail() + "): ");
        String phone = in.readNonEmptyString("Новый телефон (" + client.getPhone() + "): ");
        ClientRole role = in.readEnum("Новая роль:", ClientRole.class);

        client.setFullName(fullName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setRole(role);
        clientService.update(client);
        System.out.println("Клиент обновлён.");
    }

    private void deleteClient() {
        int id = in.readInt("ID клиента для удаления: ");
        clientService.delete(id);
        System.out.println("Клиент удалён.");
    }

    private void printClients(List<Client> clients) {
        if (clients.isEmpty()) {
            System.out.println("Список клиентов пуст.");
            return;
        }
        System.out.println("----------------- КЛИЕНТЫ -----------------");
        clients.forEach(System.out::println);
    }

    // ============================================================
    // МЕНЮ ЗАКАЗОВ
    // ============================================================

    private void ordersMenu() {
        while (true) {
            System.out.println();
            System.out.println("---------- ЗАКАЗЫ НА ЗАПИСЬ ----------");
            System.out.println("1. Создать заказ");
            System.out.println("2. Список заказов");
            System.out.println("3. Найти заказ по ID");
            System.out.println("4. Изменить заказ");
            System.out.println("5. Изменить статус заказа");
            System.out.println("6. Удалить заказ");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> createOrder();
                case 2 -> printOrders(orderService.getAll());
                case 3 -> findOrderById();
                case 4 -> updateOrder();
                case 5 -> changeOrderStatus();
                case 6 -> deleteOrder();
                case 0 -> { return; }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    private void createOrder() {
        String title = in.readNonEmptyString("Название заказа: ");
        String description = in.readNonEmptyString("Описание: ");
        LocalDate date = in.readDate("Дата записи");
        int hours = in.readIntInRange("Часы записи", 1, 12);
        RecordingType type = in.readEnum("Тип записи:", RecordingType.class);
        Priority priority = in.readEnum("Приоритет:", Priority.class);
        int clientId = in.readInt("ID клиента: ");

        RecordingOrder order = new RecordingOrder(
                title, description, OrderStatus.CREATED,
                type, priority, clientId, date, hours, 0);
        RecordingOrder saved = orderService.create(order);
        System.out.println("Создан заказ: " + saved);
    }

    private void findOrderById() {
        int id = in.readInt("ID заказа: ");
        RecordingOrder order = orderService.getById(id);
        System.out.println(order);
    }

    private void updateOrder() {
        int id = in.readInt("ID заказа для изменения: ");
        RecordingOrder order = orderService.getById(id);

        String title = in.readNonEmptyString("Новое название (" + order.getTitle() + "): ");
        String description = in.readNonEmptyString("Новое описание: ");
        LocalDate date = in.readDate("Новая дата записи (" + order.getRecordingDate() + ")");
        int hours = in.readIntInRange("Часы записи", 1, 12);
        RecordingType type = in.readEnum("Тип записи:", RecordingType.class);
        Priority priority = in.readEnum("Приоритет:", Priority.class);
        int clientId = in.readInt("ID клиента: ");

        order.setTitle(title);
        order.setDescription(description);
        order.setRecordingDate(date);
        order.setHours(hours);
        order.setType(type);
        order.setPriority(priority);
        order.setClientId(clientId);
        orderService.update(order);
        System.out.println("Заказ обновлён.");
    }

    private void changeOrderStatus() {
        int id = in.readInt("ID заказа: ");
        RecordingOrder order = orderService.getById(id);
        System.out.println("Текущий статус: " + order.getStatus());
        OrderStatus status = in.readEnum("Новый статус:", OrderStatus.class);
        order.setStatus(status);
        orderService.update(order);
        System.out.println("Статус обновлён.");
    }

    private void deleteOrder() {
        int id = in.readInt("ID заказа для удаления: ");
        orderService.delete(id);
        System.out.println("Заказ удалён.");
    }

    private void printOrders(List<RecordingOrder> orders) {
        if (orders.isEmpty()) {
            System.out.println("Список заказов пуст.");
            return;
        }
        System.out.println("----------------- ЗАКАЗЫ -----------------");
        orders.forEach(System.out::println);
    }

    // ============================================================
    // МЕНЮ ПОИСКА
    // ============================================================

    private void searchMenu() {
        while (true) {
            System.out.println();
            System.out.println("---------- ПОИСК ----------");
            System.out.println("1. Поиск по названию");
            System.out.println("2. Поиск по описанию");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> {
                    String kw = in.readNonEmptyString("Ключевое слово: ");
                    printOrders(orderService.searchByTitle(kw));
                }
                case 2 -> {
                    String kw = in.readNonEmptyString("Ключевое слово: ");
                    printOrders(orderService.searchByDescription(kw));
                }
                case 0 -> { return; }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    // ============================================================
    // МЕНЮ ФИЛЬТРАЦИИ
    // ============================================================

    private void filterMenu() {
        while (true) {
            System.out.println();
            System.out.println("---------- ФИЛЬТРАЦИЯ ----------");
            System.out.println("1. По статусу");
            System.out.println("2. По типу записи");
            System.out.println("3. По приоритету");
            System.out.println("4. По диапазону дат");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> {
                    OrderStatus status = in.readEnum("Статус:", OrderStatus.class);
                    printOrders(orderService.filterByStatus(status));
                }
                case 2 -> {
                    RecordingType type = in.readEnum("Тип записи:", RecordingType.class);
                    printOrders(orderService.filterByType(type));
                }
                case 3 -> {
                    Priority pr = in.readEnum("Приоритет:", Priority.class);
                    printOrders(orderService.filterByPriority(pr));
                }
                case 4 -> {
                    LocalDate from = in.readDate("Дата С");
                    LocalDate to = in.readDate("Дата ПО");
                    printOrders(orderService.filterByDateRange(from, to));
                }
                case 0 -> { return; }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    // ============================================================
    // МЕНЮ СОРТИРОВКИ
    // ============================================================

    private void sortMenu() {
        while (true) {
            System.out.println();
            System.out.println("---------- СОРТИРОВКА ----------");
            System.out.println("1. По дате записи");
            System.out.println("2. По стоимости");
            System.out.println("3. По дате создания");
            System.out.println("0. Назад");
            int choice = in.readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> {
                    boolean asc = readAsc();
                    printOrders(orderService.sortByRecordingDate(asc));
                }
                case 2 -> {
                    boolean asc = readAsc();
                    printOrders(orderService.sortByPrice(asc));
                }
                case 3 -> {
                    boolean asc = readAsc();
                    printOrders(orderService.sortByCreatedAt(asc));
                }
                case 0 -> { return; }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    private boolean readAsc() {
        System.out.println("1. По возрастанию");
        System.out.println("2. По убыванию");
        int c = in.readInt("Выберите направление: ");
        return c != 2;
    }

    // ============================================================
    // СТАТИСТИКА
    // ============================================================

    private void printStatistics() {
        Map<String, Long> stats = orderService.getStatistics();
        System.out.println();
        System.out.println("=========== СТАТИСТИКА СТУДИИ ===========");
        stats.forEach((key, value) ->
                System.out.printf("%-35s %s%n", key + ":", value));
        System.out.println("=========================================");
    }

    // ============================================================
    // ЭКСПОРТ
    // ============================================================

    private void exportMenu() {
        System.out.println();
        System.out.println("---------- ЭКСПОРТ ДАННЫХ ----------");
        System.out.println("1. Экспорт заказов в Excel (.xlsx)");
        System.out.println("0. Назад");
        int choice = in.readInt("Выберите действие: ");
        switch (choice) {
            case 1 -> {
                String fileName = in.readNonEmptyString("Имя файла (например, orders): ");
                ExcelExporter.exportOrders(orderService.getAll(), fileName);
            }
            case 0 -> { /* просто выход */ }
            default -> System.out.println("Неверный пункт меню.");
        }
    }

    // ============================================================
    // ВЫВОД ТАБЛИЦ БД
    // ============================================================

    private void printDatabaseTables() {
        System.out.println();
        System.out.println("===== ТАБЛИЦА: clients =====");
        printClients(clientService.getAll());
        System.out.println();
        System.out.println("===== ТАБЛИЦА: recording_orders =====");
        printOrders(orderService.getAll());
    }
}