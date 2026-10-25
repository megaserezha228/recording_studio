package com.studio.service;

import com.studio.model.OrderStatus;
import com.studio.model.Priority;
import com.studio.model.RecordingOrder;
import com.studio.model.RecordingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Проверка поиска, фильтрации и сортировки без базы данных:
 * applyQuery работает с уже загруженным списком.
 */
class RecordingOrderServiceQueryTest {

    // Репозитории для applyQuery не нужны.
    private final RecordingOrderService service = new RecordingOrderService(null, null);

    private final List<RecordingOrder> orders = List.of(
            order(1, "Запись сингла", "Вокальная партия", "Иван Морозов",
                    OrderStatus.CREATED, RecordingType.SINGLE, Priority.HIGH, "2026-02-15", 14400),
            order(2, "Альбом группы", "Запись 10 треков", "Группа Звёзды",
                    OrderStatus.IN_PROGRESS, RecordingType.ALBUM, Priority.HIGH, "2026-02-20", 30000),
            order(3, "Реклама кофе", "Озвучка ролика", "Ольга Светлова",
                    OrderStatus.COMPLETED, RecordingType.VOICEOVER, Priority.MEDIUM, "2026-01-10", 4000),
            order(4, "Аудиокнига", "Озвучка главы книги", "Ольга Светлова",
                    OrderStatus.COMPLETED, RecordingType.VOICEOVER, Priority.LOW, "2026-01-20", 10000));

    @Test
    void emptyQueryReturnsAllSortedByRecordingDate() {
        List<RecordingOrder> result = service.applyQuery(orders, OrderQuery.all());

        assertThat(result).extracting(RecordingOrder::getId).containsExactly(3, 4, 1, 2);
    }

    @Test
    void searchLooksIntoTitleDescriptionAndClientIgnoringCase() {
        assertThat(ids(query("АЛЬБОМ", null, null, null))).containsExactly(2);
        assertThat(ids(query("озвучка", null, null, null))).containsExactly(3, 4);
        assertThat(ids(query("светлова", null, null, null))).containsExactly(3, 4);
    }

    @Test
    void filtersCombineWithSearch() {
        assertThat(ids(query("", OrderStatus.COMPLETED, null, null))).containsExactly(3, 4);
        assertThat(ids(query("", null, RecordingType.SINGLE, null))).containsExactly(1);
        assertThat(ids(query("", null, null, Priority.HIGH))).containsExactly(1, 2);
        assertThat(ids(query("книг", OrderStatus.COMPLETED, RecordingType.VOICEOVER, Priority.LOW)))
                .containsExactly(4);
        assertThat(ids(query("книг", OrderStatus.CREATED, null, null))).isEmpty();
    }

    @Test
    void sortsByPriceInBothDirections() {
        OrderQuery asc = new OrderQuery("", null, null, null, OrderSortField.PRICE, true);
        OrderQuery desc = new OrderQuery("", null, null, null, OrderSortField.PRICE, false);

        assertThat(service.applyQuery(orders, asc)).extracting(RecordingOrder::getPrice)
                .containsExactly(4000, 10000, 14400, 30000);
        assertThat(service.applyQuery(orders, desc)).extracting(RecordingOrder::getPrice)
                .containsExactly(30000, 14400, 10000, 4000);
    }

    @Test
    void sortsByClientNameThenById() {
        OrderQuery byClient = new OrderQuery("", null, null, null, OrderSortField.CLIENT, true);

        assertThat(service.applyQuery(orders, byClient)).extracting(RecordingOrder::getId)
                .containsExactly(2, 1, 3, 4);
    }

    @Test
    void nullSourceGivesEmptyList() {
        assertThat(service.applyQuery(null, OrderQuery.all())).isEmpty();
    }

    @Test
    void countsOrdersPerClient() {
        List<RecordingOrder> withRepeatClient = List.of(orders.get(0), orders.get(1), orders.get(2),
                order(5, "Ещё сингл", "Вокал", "Иван Морозов",
                        OrderStatus.CREATED, RecordingType.SINGLE, Priority.LOW, "2026-03-01", 3000, 1));

        assertThat(service.countOrdersByClient(withRepeatClient))
                .containsOnly(entry(1, 2L), entry(2, 1L), entry(3, 1L));
        assertThat(service.countOrdersByClient(null)).isEmpty();
    }

    private List<Integer> ids(OrderQuery query) {
        return service.applyQuery(orders, query).stream().map(RecordingOrder::getId).toList();
    }

    private static OrderQuery query(String keyword, OrderStatus status, RecordingType type, Priority priority) {
        return new OrderQuery(keyword, status, type, priority, OrderSortField.RECORDING_DATE, true);
    }

    private static RecordingOrder order(int id, String title, String description, String client,
                                        OrderStatus status, RecordingType type, Priority priority,
                                        String date, int price) {
        return order(id, title, description, client, status, type, priority, date, price, id);
    }

    private static RecordingOrder order(int id, String title, String description, String client,
                                        OrderStatus status, RecordingType type, Priority priority,
                                        String date, int price, int clientId) {
        RecordingOrder order = new RecordingOrder(id, title, description, status, type, priority,
                clientId, LocalDate.parse(date), 2, price, LocalDateTime.now(), LocalDateTime.now());
        order.setClientName(client);
        return order;
    }
}
