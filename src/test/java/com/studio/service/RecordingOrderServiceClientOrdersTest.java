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

/**
 * Связанные данные (Часть 4): заказы клиента и итоги по ним.
 * Методы работают с уже загруженным списком, поэтому БД не нужна.
 */
class RecordingOrderServiceClientOrdersTest {

    private final RecordingOrderService service = new RecordingOrderService(null, null);

    private final List<RecordingOrder> orders = List.of(
            order(1, 1, "2026-02-15", OrderStatus.CREATED, 14400),
            order(2, 2, "2026-02-20", OrderStatus.IN_PROGRESS, 30000),
            order(3, 1, "2026-03-10", OrderStatus.COMPLETED, 6000),
            order(4, 1, "2026-01-05", OrderStatus.CANCELLED, 9000),
            order(5, 3, "2026-01-10", OrderStatus.COMPLETED, 4000));

    @Test
    void ordersOfClientAreFilteredByClientIdAndNewestFirst() {
        assertThat(service.ordersOfClient(orders, 1)).extracting(RecordingOrder::getId)
                .containsExactly(3, 1, 4);
        assertThat(service.ordersOfClient(orders, 2)).extracting(RecordingOrder::getId)
                .containsExactly(2);
        assertThat(service.ordersOfClient(orders, 99)).isEmpty();
        assertThat(service.ordersOfClient(null, 1)).isEmpty();
    }

    @Test
    void summaryCountsActiveOrdersAndExcludesCancelledFromTotal() {
        ClientOrdersSummary summary = service.summarize(service.ordersOfClient(orders, 1));

        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.active()).isEqualTo(1);              // только «Создан»
        assertThat(summary.totalPrice()).isEqualTo(14400 + 6000); // отменённый не считается
    }

    @Test
    void summaryOfNoOrdersIsZero() {
        assertThat(service.summarize(List.of())).isEqualTo(new ClientOrdersSummary(0, 0, 0));
        assertThat(service.summarize(null)).isEqualTo(new ClientOrdersSummary(0, 0, 0));
    }

    private static RecordingOrder order(int id, int clientId, String date, OrderStatus status, int price) {
        return new RecordingOrder(id, "Заказ " + id, "Описание", status, RecordingType.SINGLE,
                Priority.MEDIUM, clientId, LocalDate.parse(date), 2, price,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
