package com.studio.service;

import com.studio.model.OrderStatus;
import com.studio.model.Priority;
import com.studio.model.RecordingType;

/**
 * Параметры поиска, фильтрации и сортировки списка заказов.
 * Контроллер собирает этот объект из элементов управления главного окна
 * и передаёт в {@link RecordingOrderService#applyQuery}.
 *
 * @param keyword   строка поиска (по названию, описанию и имени клиента); пусто — без поиска
 * @param status    фильтр по статусу; null — все статусы
 * @param type      фильтр по типу записи; null — все типы
 * @param priority  фильтр по приоритету; null — все приоритеты
 * @param sortField поле сортировки
 * @param ascending true — по возрастанию, false — по убыванию
 */
public record OrderQuery(String keyword,
                         OrderStatus status,
                         RecordingType type,
                         Priority priority,
                         OrderSortField sortField,
                         boolean ascending) {

    /** Запрос «без условий»: все заказы, по дате записи по возрастанию. */
    public static OrderQuery all() {
        return new OrderQuery("", null, null, null, OrderSortField.RECORDING_DATE, true);
    }
}
