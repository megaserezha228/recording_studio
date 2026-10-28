package com.studio.service;

/**
 * Итоги по заказам одного клиента для карточки клиента.
 *
 * @param total      всего заказов
 * @param active     активных (создан, подтверждён, в работе)
 * @param totalPrice общая стоимость заказов без отменённых, руб.
 */
public record ClientOrdersSummary(int total, long active, long totalPrice) {
}
