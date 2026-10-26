package com.studio.service;

import com.studio.exception.BusinessException;
import com.studio.model.Client;
import com.studio.model.OrderStatus;
import com.studio.model.Priority;
import com.studio.model.RecordingOrder;
import com.studio.model.RecordingType;
import com.studio.repository.ClientRepository;
import com.studio.repository.RecordingOrderRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Проверка добавления и редактирования заказа (Часть 3) без базы данных:
 * вместо JDBC-репозиториев используются простые реализации в памяти.
 */
class RecordingOrderServiceSaveTest {

    private final InMemoryOrders orders = new InMemoryOrders();
    private final RecordingOrderService service = new RecordingOrderService(orders, new OneClient());

    @Test
    void createCalculatesPriceAndAssignsId() {
        RecordingOrder order = newOrder(LocalDate.now().plusDays(3), Priority.HIGH);

        service.create(order);

        assertThat(order.getId()).isPositive();
        assertThat(order.getPrice()).isEqualTo(14400);   // 4 ч × 3000 ₽ (сингл) + 20% за высокий приоритет
        assertThat(orders.findById(order.getId())).isPresent();
    }

    @Test
    void createRejectsPastDateAndUnknownClient() {
        assertThatThrownBy(() -> service.create(newOrder(LocalDate.now().minusDays(1), Priority.LOW)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("в прошлом");

        RecordingOrder withoutClient = newOrder(LocalDate.now().plusDays(1), Priority.LOW);
        withoutClient.setClientId(99);
        assertThatThrownBy(() -> service.create(withoutClient))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Клиента");
    }

    @Test
    void updateAllowsPastDateIfItWasNotChanged() {
        RecordingOrder old = stored(OrderStatus.IN_PROGRESS, LocalDate.now().minusDays(10));

        RecordingOrder edited = copyOf(old);
        edited.setStatus(OrderStatus.COMPLETED);
        service.update(edited);

        assertThat(orders.findById(old.getId())).get()
                .extracting(RecordingOrder::getStatus).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void updateRejectsChangedDateInPastAndForbiddenStatusTransition() {
        RecordingOrder old = stored(OrderStatus.CREATED, LocalDate.now().plusDays(5));

        RecordingOrder pastDate = copyOf(old);
        pastDate.setRecordingDate(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> service.update(pastDate)).hasMessageContaining("в прошлом");

        RecordingOrder jump = copyOf(old);
        jump.setStatus(OrderStatus.COMPLETED);   // CREATED → COMPLETED запрещён
        assertThatThrownBy(() -> service.update(jump)).hasMessageContaining("переход статуса");
    }

    @Test
    void allowedStatusesContainCurrentAndPermittedTransitions() {
        assertThat(service.allowedStatuses(OrderStatus.CREATED))
                .containsExactly(OrderStatus.CREATED, OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
        assertThat(service.allowedStatuses(OrderStatus.COMPLETED)).containsExactly(OrderStatus.COMPLETED);
    }

    private RecordingOrder stored(OrderStatus status, LocalDate date) {
        RecordingOrder order = newOrder(date, Priority.MEDIUM);
        order.setStatus(status);
        return orders.save(order);
    }

    private static RecordingOrder newOrder(LocalDate date, Priority priority) {
        return new RecordingOrder("Запись сингла", "Вокал", OrderStatus.CREATED,
                RecordingType.SINGLE, priority, 1, date, 4, 0);
    }

    private static RecordingOrder copyOf(RecordingOrder o) {
        RecordingOrder copy = new RecordingOrder(o.getTitle(), o.getDescription(), o.getStatus(),
                o.getType(), o.getPriority(), o.getClientId(), o.getRecordingDate(), o.getHours(), 0);
        copy.setId(o.getId());
        return copy;
    }

    /** Заказы в памяти: findById возвращает копию, как при чтении из БД. */
    private static final class InMemoryOrders implements RecordingOrderRepository {
        private final Map<Integer, RecordingOrder> rows = new HashMap<>();
        private int nextId = 1;

        @Override public RecordingOrder save(RecordingOrder order) {
            order.setId(nextId++);
            rows.put(order.getId(), copyOf(order));
            return order;
        }
        @Override public Optional<RecordingOrder> findById(int id) {
            return Optional.ofNullable(rows.get(id)).map(RecordingOrderServiceSaveTest::copyOf);
        }
        @Override public List<RecordingOrder> findAll() { return new ArrayList<>(rows.values()); }
        @Override public boolean update(RecordingOrder order) {
            return rows.replace(order.getId(), copyOf(order)) != null;
        }
        @Override public boolean deleteById(int id) { return rows.remove(id) != null; }
        @Override public List<RecordingOrder> findByStatus(OrderStatus status) { return List.of(); }
        @Override public List<RecordingOrder> findByType(RecordingType type) { return List.of(); }
        @Override public List<RecordingOrder> findByPriority(Priority priority) { return List.of(); }
        @Override public List<RecordingOrder> findByClientId(int clientId) { return List.of(); }
    }

    /** В «базе» есть только клиент с id=1. */
    private static final class OneClient implements ClientRepository {
        @Override public Client save(Client client) { return client; }
        @Override public Optional<Client> findById(int id) { return Optional.empty(); }
        @Override public Optional<Client> findByEmail(String email) { return Optional.empty(); }
        @Override public List<Client> findAll() { return List.of(); }
        @Override public boolean update(Client client) { return false; }
        @Override public boolean deleteById(int id) { return false; }
        @Override public boolean existsById(int id) { return id == 1; }
    }
}
