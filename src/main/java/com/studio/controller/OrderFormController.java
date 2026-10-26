package com.studio.controller;

import com.studio.exception.BusinessException;
import com.studio.exception.EntityNotFoundException;
import com.studio.model.Client;
import com.studio.model.OrderStatus;
import com.studio.model.Priority;
import com.studio.model.RecordingOrder;
import com.studio.model.RecordingType;
import com.studio.service.ClientService;
import com.studio.service.RecordingOrderService;
import com.studio.ui.Dialogs;
import com.studio.ui.Formatters;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Контроллер формы заказа (order-form.fxml) — добавление и редактирование.
 * <p>
 * Форма только собирает значения из полей в объект {@link RecordingOrder}
 * и передаёт его сервису. Бизнес-правила (длина названия, дата, переходы статусов,
 * расчёт стоимости) проверяет {@link RecordingOrderService}; если правило нарушено,
 * сервис бросает исключение, а форма показывает его текст и остаётся открытой.
 */
public class OrderFormController {

    private static final Logger log = LoggerFactory.getLogger(OrderFormController.class);

    private final RecordingOrderService orderService;
    private final ClientService clientService;

    /** Редактируемый заказ; null — режим добавления. */
    private RecordingOrder editedOrder;

    /** Сохранённый заказ (с id из БД); пусто, если пользователь нажал «Отмена». */
    private RecordingOrder savedOrder;

    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Client> clientBox;
    @FXML private ComboBox<RecordingType> typeBox;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private ComboBox<OrderStatus> statusBox;
    @FXML private Label statusHint;
    @FXML private Label pricePreview;

    public OrderFormController(RecordingOrderService orderService, ClientService clientService) {
        this.orderService = orderService;
        this.clientService = clientService;
    }

    @FXML
    private void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(RecordingType.values()));
        priorityBox.setItems(FXCollections.observableArrayList(Priority.values()));
        hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 2));
        // Дата выбирается в календаре, часы — стрелками: ввести «не число» нельзя.
        datePicker.setEditable(false);
        hoursSpinner.setEditable(false);

        // В списке клиентов показываем имя и роль, а не id.
        clientBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getFullName() + " — " + client.getRole();
            }

            @Override
            public Client fromString(String text) {
                return null;   // список не редактируемый, обратное преобразование не нужно
            }
        });

        // Стоимость пересчитывается при каждом изменении типа, приоритета или часов.
        typeBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePricePreview());
        priorityBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePricePreview());
        hoursSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePricePreview());
    }

    /**
     * Подготавливает форму. Вызывается окном {@code OrderFormWindow} после загрузки FXML.
     *
     * @param order заказ для редактирования или null для добавления нового
     */
    public void init(RecordingOrder order) {
        this.editedOrder = order;
        clientBox.setItems(FXCollections.observableArrayList(clientService.getAll()));

        if (order == null) {
            formTitle.setText("Новый заказ");
            formSubtitle.setText("Заполните данные заказа на студийную запись.");
            typeBox.setValue(RecordingType.SINGLE);
            priorityBox.setValue(Priority.MEDIUM);
            datePicker.setValue(LocalDate.now().plusDays(1));
            statusBox.setItems(FXCollections.observableArrayList(OrderStatus.CREATED));
            statusBox.setValue(OrderStatus.CREATED);
            statusBox.setDisable(true);
            statusHint.setText("Новый заказ всегда создаётся со статусом «Создан».");
        } else {
            formTitle.setText("Изменение заказа №" + order.getId());
            formSubtitle.setText("Создан " + Formatters.dateTime(order.getCreatedAt())
                    + ", изменён " + Formatters.dateTime(order.getUpdatedAt()) + ".");
            titleField.setText(order.getTitle());
            descriptionArea.setText(order.getDescription());
            clientBox.getItems().stream()
                    .filter(c -> c.getId() == order.getClientId())
                    .findFirst()
                    .ifPresent(clientBox::setValue);
            typeBox.setValue(order.getType());
            priorityBox.setValue(order.getPriority());
            datePicker.setValue(order.getRecordingDate());
            hoursSpinner.getValueFactory().setValue(order.getHours());
            statusBox.setItems(FXCollections.observableArrayList(
                    orderService.allowedStatuses(order.getStatus())));
            statusBox.setValue(order.getStatus());
            statusHint.setText(order.getStatus().isActive()
                    ? "Доступны только разрешённые переходы из статуса «" + order.getStatus() + "»."
                    : "Статус «" + order.getStatus() + "» финальный и больше не меняется.");
        }
        updatePricePreview();
    }

    /** Результат работы формы: сохранённый заказ или пусто, если сохранения не было. */
    public Optional<RecordingOrder> getSavedOrder() {
        return Optional.ofNullable(savedOrder);
    }

    @FXML
    private void onSave() {
        if (clientBox.getValue() == null) {
            Dialogs.showError(window(), "Заказ не сохранён", "Поле «Клиент»: выберите клиента из списка.");
            clientBox.requestFocus();
            return;
        }
        RecordingOrder order = collectOrder();
        try {
            if (editedOrder == null) {
                orderService.create(order);
                log.info("Добавлен заказ id={}", order.getId());
            } else {
                orderService.update(order);
                log.info("Изменён заказ id={}", order.getId());
            }
            savedOrder = order;
            close();
        } catch (BusinessException | EntityNotFoundException e) {
            // Нарушено бизнес-правило: форма остаётся открытой, введённые данные не теряются.
            log.info("Заказ не сохранён: {}", e.getMessage());
            Dialogs.showError(window(), "Заказ не сохранён", e);
        } catch (RuntimeException e) {
            log.error("Ошибка сохранения заказа", e);
            Dialogs.showError(window(), "Заказ не сохранён", e);
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    /**
     * Собирает значения полей в новый объект. Заказ из таблицы не изменяется,
     * поэтому при «Отмене» или ошибке сохранения таблица показывает прежние данные.
     */
    private RecordingOrder collectOrder() {
        RecordingOrder order = new RecordingOrder(
                titleField.getText() == null ? null : titleField.getText().trim(),
                descriptionArea.getText() == null ? null : descriptionArea.getText().trim(),
                statusBox.getValue(),
                typeBox.getValue(),
                priorityBox.getValue(),
                clientBox.getValue().getId(),
                datePicker.getValue(),
                hoursSpinner.getValue(),
                0);   // стоимость рассчитает сервис
        if (editedOrder != null) {
            order.setId(editedOrder.getId());
        }
        order.setClientName(clientBox.getValue().getFullName());
        return order;
    }

    private void updatePricePreview() {
        RecordingType type = typeBox.getValue();
        Priority priority = priorityBox.getValue();
        Integer hours = hoursSpinner.getValue();
        if (type == null || priority == null || hours == null) {
            pricePreview.setText("—");
            return;
        }
        pricePreview.setText(Formatters.money(orderService.calculatePrice(type, priority, hours))
                + "  (" + Formatters.hours(hours) + " × " + Formatters.money(type.getHourlyRate()) + ")");
    }

    private Window window() {
        return titleField.getScene().getWindow();
    }

    private void close() {
        ((Stage) window()).close();
    }
}
