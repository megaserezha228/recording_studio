package com.studio.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Элемент выпадающего списка: подпись для пользователя + значение для программы.
 * <p>
 * Используется в фильтрах главного окна: первый пункт «Все ...» хранит значение null,
 * что означает «фильтр не применяется».
 *
 * @param label текст, который видит пользователь
 * @param value значение (например, OrderStatus.COMPLETED или null)
 */
public record ChoiceItem<T>(String label, T value) {

    /** Список для фильтра: «Все ...» + все значения перечисления. */
    @SafeVarargs
    public static <T> List<ChoiceItem<T>> withAll(String allLabel, T... values) {
        List<ChoiceItem<T>> items = new ArrayList<>();
        items.add(new ChoiceItem<>(allLabel, null));
        for (T value : values) {
            items.add(new ChoiceItem<>(value.toString(), value));
        }
        return items;
    }

    /** ComboBox показывает элемент через toString(). */
    @Override
    public String toString() {
        return label;
    }
}
