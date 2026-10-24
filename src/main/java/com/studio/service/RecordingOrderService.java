package com.studio.service;

import com.studio.exception.BusinessException;
import com.studio.exception.EntityNotFoundException;
import com.studio.model.*;
import com.studio.repository.ClientRepository;
import com.studio.repository.RecordingOrderRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordingOrderService {

    private final RecordingOrderRepository repository;
    private final ClientRepository clientRepository;

    public RecordingOrderService(RecordingOrderRepository repository,
                                 ClientRepository clientRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
    }

    // ============ CRUD ============

    public RecordingOrder create(RecordingOrder order) {
        validate(order);
        order.setPrice(calculatePrice(order));
        return repository.save(order);
    }

    public RecordingOrder getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с id=" + id + " не найден"));
    }

    public List<RecordingOrder> getAll() {
        return repository.findAll();
    }

    public void update(RecordingOrder newOrder) {
        RecordingOrder old = getById(newOrder.getId());
        validate(newOrder);

        // Бизнес-правило: запрет некорректного перехода статуса
        if (old.getStatus() != newOrder.getStatus()
                && !old.getStatus().canTransitionTo(newOrder.getStatus())) {
            throw new BusinessException(
                    "Недопустимый переход статуса: "
                            + old.getStatus() + " → " + newOrder.getStatus());
        }

        newOrder.setPrice(calculatePrice(newOrder));
        repository.update(newOrder);
    }

    public void delete(int id) {
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException("Заказ с id=" + id + " не найден");
        }
    }

    // ============ БИЗНЕС-ПРАВИЛА ЗАКАЗА ============

    /**
     * Правило 1: название обязательно, длина 3..200.
     * Правило 2: описание обязательно.
     * Правило 3: дата записи не может быть в прошлом.
     * Правило 4: длительность от 1 до 12 часов.
     * Правило 5: клиент должен существовать в БД.
     * Правило 6: статус, приоритет и тип записи обязательны (enum).
     */
    private void validate(RecordingOrder order) {
        if (order == null) {
            throw new BusinessException("Заказ не может быть null");
        }
        if (order.getTitle() == null || order.getTitle().isBlank()) {
            throw new BusinessException("Название заказа обязательно");
        }
        if (order.getTitle().length() < 3 || order.getTitle().length() > 200) {
            throw new BusinessException("Название должно быть от 3 до 200 символов");
        }
        if (order.getDescription() == null || order.getDescription().isBlank()) {
            throw new BusinessException("Описание заказа обязательно");
        }
        if (order.getRecordingDate() == null) {
            throw new BusinessException("Дата записи обязательна");
        }
        if (order.getRecordingDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Дата записи не может быть в прошлом");
        }
        if (order.getHours() < 1 || order.getHours() > 12) {
            throw new BusinessException("Длительность записи должна быть от 1 до 12 часов");
        }
        if (order.getStatus() == null) {
            throw new BusinessException("Статус заказа обязателен");
        }
        if (order.getPriority() == null) {
            throw new BusinessException("Приоритет заказа обязателен");
        }
        if (order.getType() == null) {
            throw new BusinessException("Тип записи обязателен");
        }
        if (!clientRepository.existsById(order.getClientId())) {
            throw new BusinessException(
                    "Клиента с id=" + order.getClientId() + " не существует");
        }
    }

    /**
     * Правило 7 (расчётное): стоимость = часы × ставка типа записи,
     * надбавка 20% при HIGH-приоритете.
     */
    private int calculatePrice(RecordingOrder order) {
        int base = order.getHours() * order.getType().getHourlyRate();
        if (order.getPriority() == Priority.HIGH) {
            base = (int) Math.round(base * 1.2);
        }
        return base;
    }

    // ============ ПОИСК ============

    public List<RecordingOrder> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException("Ключевое слово для поиска не задано");
        }
        String kw = keyword.toLowerCase();
        return getAll().stream()
                .filter(o -> o.getTitle().toLowerCase().contains(kw))
                .toList();
    }

    public List<RecordingOrder> searchByDescription(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException("Ключевое слово для поиска не задано");
        }
        String kw = keyword.toLowerCase();
        return getAll().stream()
                .filter(o -> o.getDescription().toLowerCase().contains(kw))
                .toList();
    }

    // ============ ФИЛЬТРАЦИЯ ============

    public List<RecordingOrder> filterByStatus(OrderStatus status) {
        if (status == null) throw new BusinessException("Статус не задан");
        return repository.findByStatus(status);
    }

    public List<RecordingOrder> filterByType(RecordingType type) {
        if (type == null) throw new BusinessException("Тип записи не задан");
        return repository.findByType(type);
    }

    public List<RecordingOrder> filterByPriority(Priority priority) {
        if (priority == null) throw new BusinessException("Приоритет не задан");
        return repository.findByPriority(priority);
    }

    public List<RecordingOrder> filterByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("Диапазон дат должен быть задан полностью");
        }
        if (from.isAfter(to)) {
            throw new BusinessException("Начало диапазона позже его конца");
        }
        return getAll().stream()
                .filter(o -> !o.getRecordingDate().isBefore(from)
                        && !o.getRecordingDate().isAfter(to))
                .toList();
    }

    // ============ СОРТИРОВКА ============

    public List<RecordingOrder> sortByRecordingDate(boolean asc) {
        Comparator<RecordingOrder> cmp = Comparator.comparing(RecordingOrder::getRecordingDate);
        if (!asc) cmp = cmp.reversed();
        return getAll().stream().sorted(cmp).toList();
    }

    public List<RecordingOrder> sortByPrice(boolean asc) {
        Comparator<RecordingOrder> cmp = Comparator.comparingInt(RecordingOrder::getPrice);
        if (!asc) cmp = cmp.reversed();
        return getAll().stream().sorted(cmp).toList();
    }

    public List<RecordingOrder> sortByCreatedAt(boolean asc) {
        Comparator<RecordingOrder> cmp = Comparator.comparing(RecordingOrder::getCreatedAt);
        if (!asc) cmp = cmp.reversed();
        return getAll().stream().sorted(cmp).toList();
    }

    // ============ ПОИСК + ФИЛЬТРЫ + СОРТИРОВКА ДЛЯ ГЛАВНОГО ОКНА ============

    /**
     * Применяет к списку заказов поиск, фильтры и сортировку из {@link OrderQuery}.
     * <p>
     * Метод не обращается к БД: список уже загружен ({@link #getAll()}),
     * поэтому смена фильтра в интерфейсе работает мгновенно,
     * а кнопка «Обновить» заново читает данные из PostgreSQL.
     */
    public List<RecordingOrder> applyQuery(List<RecordingOrder> source, OrderQuery query) {
        if (source == null) {
            return List.of();
        }
        if (query == null) {
            query = OrderQuery.all();
        }
        String keyword = query.keyword() == null
                ? ""
                : query.keyword().trim().toLowerCase(Locale.ROOT);
        OrderStatus status = query.status();
        RecordingType type = query.type();
        Priority priority = query.priority();

        return source.stream()
                .filter(o -> keyword.isEmpty() || matchesKeyword(o, keyword))
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> type == null || o.getType() == type)
                .filter(o -> priority == null || o.getPriority() == priority)
                .sorted(comparatorFor(query.sortField(), query.ascending()))
                .toList();
    }

    /** Поиск по нескольким смысловым полям: название, описание, имя клиента. */
    private boolean matchesKeyword(RecordingOrder order, String keyword) {
        return containsIgnoreCase(order.getTitle(), keyword)
                || containsIgnoreCase(order.getDescription(), keyword)
                || containsIgnoreCase(order.getClientName(), keyword);
    }

    private static boolean containsIgnoreCase(String text, String lowerKeyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private Comparator<RecordingOrder> comparatorFor(OrderSortField field, boolean ascending) {
        OrderSortField sortField = field == null ? OrderSortField.RECORDING_DATE : field;
        Comparator<RecordingOrder> cmp = switch (sortField) {
            case RECORDING_DATE -> Comparator.comparing(RecordingOrder::getRecordingDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case PRICE -> Comparator.comparingInt(RecordingOrder::getPrice);
            case TITLE -> Comparator.comparing(RecordingOrder::getTitle, String.CASE_INSENSITIVE_ORDER);
            case CLIENT -> Comparator.comparing(
                    o -> o.getClientName() == null ? "" : o.getClientName(), String.CASE_INSENSITIVE_ORDER);
            case CREATED_AT -> Comparator.comparing(RecordingOrder::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if (!ascending) {
            cmp = cmp.reversed();
        }
        // При равных значениях порядок стабилен — по номеру заказа.
        return cmp.thenComparingInt(RecordingOrder::getId);
    }

    // ============ СТАТИСТИКА ============

    public Map<String, Long> getStatistics() {
        List<RecordingOrder> all = getAll();
        long clients = clientRepository.findAll().size();
        long total = all.size();
        long active = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED
                        || o.getStatus() == OrderStatus.CONFIRMED
                        || o.getStatus() == OrderStatus.IN_PROGRESS)
                .count();
        long completed = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelled = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long highPriority = all.stream()
                .filter(o -> o.getPriority() == Priority.HIGH).count();
        long revenue = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToLong(RecordingOrder::getPrice).sum();

        return Map.of(
                "Всего клиентов", clients,
                "Всего заказов", total,
                "Активных заказов", active,
                "Завершённых", completed,
                "Отменённых", cancelled,
                "Высокий приоритет", highPriority,
                "Выручка (завершённые), руб.", revenue
        );
    }

    public Map<OrderStatus, Long> countByStatus() {
        return getAll().stream()
                .collect(Collectors.groupingBy(RecordingOrder::getStatus, Collectors.counting()));
    }
}