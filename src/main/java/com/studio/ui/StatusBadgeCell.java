package com.studio.ui;

import com.studio.model.OrderStatus;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Region;

/**
 * Ячейка таблицы со статусом заказа в виде цветной «плашки».
 * Используется в таблице заказов и в таблице заказов клиента.
 *
 * @param <S> тип строки таблицы
 */
public class StatusBadgeCell<S> extends TableCell<S, OrderStatus> {

    private final Label badge = new Label();

    public StatusBadgeCell() {
        // Плашка не сжимается с многоточием, а занимает ширину по тексту.
        badge.setMinWidth(Region.USE_PREF_SIZE);
    }

    /** CSS-класс цвета статуса: COMPLETED → status-completed, IN_PROGRESS → status-in-progress. */
    public static String styleClass(OrderStatus status) {
        return "status-" + status.name().toLowerCase().replace('_', '-');
    }

    @Override
    protected void updateItem(OrderStatus status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) {
            setGraphic(null);
            setText(null);
            return;
        }
        badge.setText(status.getTitle());
        badge.getStyleClass().setAll("label", "status-badge", styleClass(status));
        setGraphic(badge);
        setText(null);
    }
}
