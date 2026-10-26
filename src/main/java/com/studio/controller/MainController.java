package com.studio.controller;

import com.studio.config.DatabaseManager;
import com.studio.model.Client;
import com.studio.model.ClientRole;
import com.studio.model.OrderStatus;
import com.studio.model.Priority;
import com.studio.model.RecordingOrder;
import com.studio.model.RecordingType;
import com.studio.service.ClientService;
import com.studio.service.OrderQuery;
import com.studio.service.OrderSortField;
import com.studio.service.RecordingOrderService;
import com.studio.ui.ChoiceItem;
import com.studio.ui.Dialogs;
import com.studio.ui.Formatters;
import com.studio.ui.OrderFormWindow;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Контроллер главного окна (main-view.fxml).
 * <p>
 * Отвечает только за интерфейс: принимает действия пользователя, собирает
 * параметры из элементов управления, вызывает сервис и показывает результат.
 * SQL здесь нет — работа с БД в репозитории, бизнес-правила в сервисе.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final RecordingOrderService orderService;
    private final ClientService clientService;

    /**
     * Все заказы, загруженные из БД при последнем обновлении.
     * Поиск и фильтры применяются к этому списку без повторного запроса к БД.
     */
    private List<RecordingOrder> loadedOrders = List.of();

    /**
     * Список, который показывает таблица. ObservableList сообщает TableView
     * о каждом изменении, и таблица перерисовывается автоматически.
     */
    private final ObservableList<RecordingOrder> visibleOrders = FXCollections.observableArrayList();

    /** Клиенты для таблицы на вкладке «Клиенты» (тоже ObservableList — таблица обновляется сама). */
    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    /** Количество заказов у каждого клиента: id клиента → число заказов. */
    private Map<Integer, Long> ordersPerClient = Map.of();

    // ===== Элементы интерфейса из FXML (связаны по fx:id) =====
    @FXML private Label connectionLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<ChoiceItem<OrderStatus>> statusFilter;
    @FXML private ComboBox<ChoiceItem<RecordingType>> typeFilter;
    @FXML private ComboBox<ChoiceItem<Priority>> priorityFilter;
    @FXML private ComboBox<OrderSortField> sortFieldBox;
    @FXML private ComboBox<ChoiceItem<Boolean>> sortDirectionBox;

    @FXML private TableView<RecordingOrder> ordersTable;
    @FXML private TableColumn<RecordingOrder, Integer> idColumn;
    @FXML private TableColumn<RecordingOrder, String> titleColumn;
    @FXML private TableColumn<RecordingOrder, String> clientColumn;
    @FXML private TableColumn<RecordingOrder, RecordingType> typeColumn;
    @FXML private TableColumn<RecordingOrder, String> dateColumn;
    @FXML private TableColumn<RecordingOrder, String> hoursColumn;
    @FXML private TableColumn<RecordingOrder, String> priceColumn;
    @FXML private TableColumn<RecordingOrder, Priority> priorityColumn;
    @FXML private TableColumn<RecordingOrder, OrderStatus> statusColumn;

    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Integer> clientIdColumn;
    @FXML private TableColumn<Client, String> clientNameColumn;
    @FXML private TableColumn<Client, ClientRole> clientRoleColumn;
    @FXML private TableColumn<Client, String> clientEmailColumn;
    @FXML private TableColumn<Client, String> clientPhoneColumn;
    @FXML private TableColumn<Client, Long> clientOrdersColumn;
    @FXML private Label clientsCountLabel;

    @FXML private Label detailsPlaceholder;
    @FXML private VBox detailsContent;
    @FXML private Label detailsTitle;
    @FXML private Label detailsStatus;
    @FXML private Label detailsDescription;
    @FXML private Label detailsClient;
    @FXML private Label detailsPhone;
    @FXML private Label detailsType;
    @FXML private Label detailsPriority;
    @FXML private Label detailsDate;
    @FXML private Label detailsHours;
    @FXML private Label detailsPrice;
    @FXML private Label detailsCreated;
    @FXML private Label detailsUpdated;

    @FXML private Label countLabel;
    @FXML private Label messageLabel;
    @FXML private Label lastUpdatedLabel;

    /** Пока элементы фильтра сбрасываются программно, не пересчитываем список на каждое изменение. */
    private boolean updatingFilters;

    public MainController(RecordingOrderService orderService, ClientService clientService) {
        this.orderService = orderService;
        this.clientService = clientService;
    }

    /**
     * Вызывается FXMLLoader автоматически после того, как все поля @FXML заполнены.
     * Здесь настраиваются таблица, фильтры и обработчики, которые удобнее задать в коде.
     */
    @FXML
    private void initialize() {
        setupTable();
        setupClientsTable();
        setupFilters();

        // Кнопки «Изменить» и «Удалить» доступны, только когда выбрана строка.
        var noSelection = ordersTable.getSelectionModel().selectedItemProperty().isNull();
        editButton.disableProperty().bind(noSelection);
        deleteButton.disableProperty().bind(noSelection);

        // При выборе строки обновляется карточка заказа справа.
        ordersTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldOrder, newOrder) -> showDetails(newOrder));

        // Поиск срабатывает при каждом изменении текста.
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());

        showDetails(null);
        updateCount();
    }

    /** Первая загрузка данных (вызывается приложением после показа окна). */
    public void loadData() {
        reloadFromDatabase();
    }

    // =====================================================================
    // НАСТРОЙКА ТАБЛИЦЫ
    // =====================================================================

    private void setupTable() {
        ordersTable.setItems(visibleOrders);
        ordersTable.setPlaceholder(new Label("Загрузка данных…"));

        // cellValueFactory: какое значение из объекта RecordingOrder показать в колонке.
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        titleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitle()));
        clientColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getClientName()));
        typeColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getType()));
        dateColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Formatters.date(c.getValue().getRecordingDate())));
        hoursColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.valueOf(c.getValue().getHours())));
        priceColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Formatters.money(c.getValue().getPrice())));
        priorityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPriority()));
        statusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));

        // cellFactory: как нарисовать ячейку (цвет статуса и приоритета, выравнивание).
        idColumn.getStyleClass().add("column-center");
        hoursColumn.getStyleClass().add("column-center");
        priceColumn.getStyleClass().add("column-right");
        statusColumn.setCellFactory(column -> new StatusCell());
        priorityColumn.setCellFactory(column -> new PriorityCell());

        // Двойной щелчок по строке — редактирование заказа.
        ordersTable.setRowFactory(table -> {
            TableRow<RecordingOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });
    }

    /** Ячейка статуса в виде цветной «плашки». */
    private static final class StatusCell extends TableCell<RecordingOrder, OrderStatus> {
        private final Label badge = new Label();

        StatusCell() {
            // Плашка не сжимается с многоточием, а занимает ширину по тексту.
            badge.setMinWidth(Region.USE_PREF_SIZE);
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
            badge.getStyleClass().setAll("label", "status-badge", statusStyle(status));
            setGraphic(badge);
            setText(null);
        }
    }

    /** Ячейка приоритета: высокий выделяется цветом. */
    private static final class PriorityCell extends TableCell<RecordingOrder, Priority> {
        @Override
        protected void updateItem(Priority priority, boolean empty) {
            super.updateItem(priority, empty);
            getStyleClass().removeAll("priority-high", "priority-low");
            if (empty || priority == null) {
                setText(null);
                return;
            }
            setText(priority.getTitle());
            if (priority == Priority.HIGH) {
                getStyleClass().add("priority-high");
            } else if (priority == Priority.LOW) {
                getStyleClass().add("priority-low");
            }
        }
    }

    private static String statusStyle(OrderStatus status) {
        return "status-" + status.name().toLowerCase().replace('_', '-');
    }

    /**
     * Таблица клиентов на вкладке «Клиенты». Здесь включена встроенная сортировка
     * TableView: щелчок по заголовку колонки сортирует строки (повторный — в обратном порядке).
     */
    private void setupClientsTable() {
        clientsTable.setItems(clients);
        clientsTable.setPlaceholder(new Label("Загрузка данных…"));

        clientIdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        clientNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFullName()));
        clientRoleColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getRole()));
        clientEmailColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        clientPhoneColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPhone()));
        clientOrdersColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(ordersPerClient.getOrDefault(c.getValue().getId(), 0L)));

        clientIdColumn.getStyleClass().add("column-center");
        clientOrdersColumn.getStyleClass().add("column-center");
    }

    // =====================================================================
    // ПОИСК, ФИЛЬТРЫ, СОРТИРОВКА
    // =====================================================================

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                ChoiceItem.withAll("Все статусы", OrderStatus.values())));
        typeFilter.setItems(FXCollections.observableArrayList(
                ChoiceItem.withAll("Все типы", RecordingType.values())));
        priorityFilter.setItems(FXCollections.observableArrayList(
                ChoiceItem.withAll("Любой", Priority.values())));
        sortFieldBox.setItems(FXCollections.observableArrayList(OrderSortField.values()));
        sortDirectionBox.setItems(FXCollections.observableArrayList(List.of(
                new ChoiceItem<>("По возрастанию", true),
                new ChoiceItem<>("По убыванию", false))));
        resetFilterControls();
    }

    private void resetFilterControls() {
        updatingFilters = true;
        try {
            searchField.clear();
            statusFilter.getSelectionModel().selectFirst();
            typeFilter.getSelectionModel().selectFirst();
            priorityFilter.getSelectionModel().selectFirst();
            sortFieldBox.getSelectionModel().select(OrderSortField.RECORDING_DATE);
            sortDirectionBox.getSelectionModel().selectFirst();
        } finally {
            updatingFilters = false;
        }
    }

    /** Собирает параметры из элементов управления в один объект запроса. */
    private OrderQuery buildQuery() {
        return new OrderQuery(
                searchField.getText(),
                selectedValue(statusFilter),
                selectedValue(typeFilter),
                selectedValue(priorityFilter),
                sortFieldBox.getValue(),
                !Boolean.FALSE.equals(selectedValue(sortDirectionBox)));
    }

    private static <T> T selectedValue(ComboBox<ChoiceItem<T>> box) {
        ChoiceItem<T> item = box.getValue();
        return item == null ? null : item.value();
    }

    /**
     * Применяет поиск, фильтры и сортировку к загруженным заказам
     * и показывает результат в таблице. Выбранная строка сохраняется, если она осталась в списке.
     */
    private void applyFilters() {
        if (updatingFilters) {
            return;
        }
        RecordingOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        Integer selectedId = selected == null ? null : selected.getId();

        visibleOrders.setAll(orderService.applyQuery(loadedOrders, buildQuery()));

        restoreSelection(selectedId);
        updateCount();
        if (!loadedOrders.isEmpty() && visibleOrders.isEmpty()) {
            ordersTable.setPlaceholder(new Label("Нет заказов, подходящих под условия поиска и фильтров."));
        }
    }

    private void restoreSelection(Integer orderId) {
        if (orderId == null) {
            ordersTable.getSelectionModel().clearSelection();
            return;
        }
        visibleOrders.stream()
                .filter(order -> order.getId() == orderId)
                .findFirst()
                .ifPresentOrElse(
                        order -> ordersTable.getSelectionModel().select(order),
                        () -> ordersTable.getSelectionModel().clearSelection());
    }

    /** Выделяет заказ и прокручивает таблицу к нему, если он виден при текущих фильтрах. */
    private void selectOrder(Integer orderId) {
        restoreSelection(orderId);
        int index = ordersTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            ordersTable.scrollTo(index);
        }
    }

    @FXML
    private void onFilterChanged() {
        applyFilters();
    }

    @FXML
    private void onResetFilters() {
        resetFilterControls();
        applyFilters();
        setMessage("Фильтры сброшены");
    }

    // =====================================================================
    // ЗАГРУЗКА ДАННЫХ ИЗ БД
    // =====================================================================

    @FXML
    private void onRefresh() {
        if (reloadFromDatabase()) {
            setMessage("Данные обновлены из базы");
        }
    }

    /**
     * Перечитывает данные из БД; выбранный в таблице заказ остаётся выбранным.
     *
     * @return true — данные загружены успешно
     */
    private boolean reloadFromDatabase() {
        RecordingOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        return reloadFromDatabase(selected == null ? null : selected.getId());
    }

    /**
     * Читает все заказы и клиентов из PostgreSQL (через сервисы и репозитории)
     * и заново применяет текущие фильтры. Вызывается после каждого добавления,
     * изменения и удаления, поэтому таблицы всегда показывают актуальное состояние БД.
     *
     * @param selectOrderId заказ, который нужно выделить и показать после загрузки
     *                      (например, только что добавленный или изменённый); null — без выделения
     * @return true — данные загружены успешно
     */
    private boolean reloadFromDatabase(Integer selectOrderId) {
        try {
            loadedOrders = orderService.getAll();
            List<Client> loadedClients = clientService.getAll();
            setConnected(true);
            lastUpdatedLabel.setText("Обновлено в " + Formatters.time(LocalDateTime.now()));
            ordersTable.setPlaceholder(new Label("В базе пока нет заказов."));
            log.info("Загружено заказов: {}, клиентов: {}", loadedOrders.size(), loadedClients.size());

            // Счётчики заказов задаются до setAll, чтобы новые строки сразу показали верные значения.
            ordersPerClient = orderService.countOrdersByClient(loadedOrders);
            clients.setAll(loadedClients);
            clientsTable.sort();   // сохранить сортировку, выбранную щелчком по заголовку
            clientsTable.setPlaceholder(new Label("В базе пока нет клиентов."));
            updateClientsCount();

            applyFilters();
            selectOrder(selectOrderId);
            return true;
        } catch (RuntimeException e) {
            log.error("Не удалось загрузить данные", e);
            loadedOrders = List.of();
            visibleOrders.clear();
            ordersPerClient = Map.of();
            clients.clear();
            setConnected(false);
            ordersTable.setPlaceholder(new Label(
                    "Нет данных: не удалось подключиться к базе. Проверьте подключение и нажмите «Обновить»."));
            clientsTable.setPlaceholder(new Label(
                    "Нет данных: не удалось подключиться к базе. Проверьте подключение и нажмите «Обновить»."));
            updateCount();
            updateClientsCount();
            setMessage("Ошибка загрузки данных");
            Dialogs.showError(window(), "Не удалось загрузить заказы", e);
            return false;
        }
    }

    // =====================================================================
    // ДЕЙСТВИЯ С ЗАПИСЯМИ
    // =====================================================================

    @FXML
    private void onAdd() {
        openOrderForm(null).ifPresent(saved -> {
            // Новый заказ может не подходить под текущие фильтры — сбрасываем их, чтобы он был виден.
            resetFilterControls();
            reloadFromDatabase(saved.getId());
            setMessage("Заказ «" + saved.getTitle() + "» добавлен");
        });
    }

    @FXML
    private void onEdit() {
        RecordingOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.showInfo(window(), "Изменение заказа", "Заказ не выбран",
                    "Выберите заказ в таблице, который нужно изменить.");
            return;
        }
        openOrderForm(selected).ifPresent(saved -> {
            reloadFromDatabase(saved.getId());
            setMessage("Заказ «" + saved.getTitle() + "» изменён");
        });
    }

    /**
     * Открывает модальную форму заказа и ждёт её закрытия.
     *
     * @param order заказ для редактирования или null для добавления
     * @return сохранённый заказ; пусто — форму закрыли без сохранения или её не удалось открыть
     */
    private Optional<RecordingOrder> openOrderForm(RecordingOrder order) {
        try {
            Optional<RecordingOrder> saved = OrderFormWindow.show(window(), orderService, clientService, order);
            if (saved.isEmpty()) {
                setMessage(order == null ? "Добавление отменено" : "Изменение отменено");
            }
            return saved;
        } catch (RuntimeException e) {
            // Например, не удалось прочитать список клиентов из БД.
            log.error("Не удалось открыть форму заказа", e);
            Dialogs.showError(window(), "Не удалось открыть форму заказа", e);
            return Optional.empty();
        }
    }

    @FXML
    private void onDelete() {
        RecordingOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.showInfo(window(), "Удаление заказа", "Заказ не выбран",
                    "Выберите заказ в таблице, который нужно удалить.");
            return;
        }

        boolean confirmed = Dialogs.confirm(window(),
                "Удаление заказа",
                "Удалить заказ «" + selected.getTitle() + "»?",
                "Клиент: " + Formatters.orDash(selected.getClientName())
                        + "\nДата записи: " + Formatters.date(selected.getRecordingDate())
                        + "\nСтоимость: " + Formatters.money(selected.getPrice())
                        + "\n\nЗаказ будет удалён из базы данных. Это действие нельзя отменить.",
                "Удалить");
        if (!confirmed) {
            setMessage("Удаление отменено");
            return;
        }

        try {
            orderService.delete(selected.getId());
            log.info("Удалён заказ id={}", selected.getId());
            reloadFromDatabase();
            setMessage("Заказ «" + selected.getTitle() + "» удалён");
        } catch (RuntimeException e) {
            log.error("Ошибка удаления заказа id={}", selected.getId(), e);
            Dialogs.showError(window(), "Не удалось удалить заказ", e);
        }
    }

    @FXML
    private void onStatistics() {
        // TODO Раздел «Статистика»: открыть окно statistics-view.fxml с показателями
        //  из RecordingOrderService.getStatistics() (не менее 5 показателей).
        showNotImplemented("Статистика",
                "Окно статистики будет сделано в разделе «Статистика».");
    }

    @FXML
    private void onExport() {
        // TODO Раздел «Экспорт данных»: FileChooser → сохранить visibleOrders в .csv или .xlsx.
        showNotImplemented("Экспорт",
                "Экспорт текущего списка (" + visibleOrders.size()
                        + " заказов) будет сделан в разделе «Экспорт данных».");
    }

    private void showNotImplemented(String title, String message) {
        Dialogs.showInfo(window(), title, "Раздел в разработке", message);
    }

    // =====================================================================
    // КАРТОЧКА ЗАКАЗА И СТРОКА СОСТОЯНИЯ
    // =====================================================================

    private void showDetails(RecordingOrder order) {
        boolean hasOrder = order != null;
        detailsContent.setVisible(hasOrder);
        detailsContent.setManaged(hasOrder);
        detailsPlaceholder.setVisible(!hasOrder);
        detailsPlaceholder.setManaged(!hasOrder);
        if (!hasOrder) {
            return;
        }
        detailsTitle.setText("№" + order.getId() + " · " + order.getTitle());
        detailsStatus.setText(order.getStatus().getTitle());
        detailsStatus.getStyleClass().setAll("label", "status-badge", statusStyle(order.getStatus()));
        detailsDescription.setText(Formatters.orDash(order.getDescription()));
        detailsClient.setText(Formatters.orDash(order.getClientName()));
        detailsPhone.setText(Formatters.orDash(order.getClientPhone()));
        detailsType.setText(order.getType().getTitle()
                + " (" + Formatters.money(order.getType().getHourlyRate()) + "/ч)");
        detailsPriority.setText(order.getPriority().getTitle());
        detailsDate.setText(Formatters.date(order.getRecordingDate()));
        detailsHours.setText(Formatters.hours(order.getHours()));
        detailsPrice.setText(Formatters.money(order.getPrice()));
        detailsCreated.setText(Formatters.dateTime(order.getCreatedAt()));
        detailsUpdated.setText(Formatters.dateTime(order.getUpdatedAt()));
    }

    private void updateCount() {
        countLabel.setText("Показано: " + visibleOrders.size() + " из " + loadedOrders.size());
    }

    private void updateClientsCount() {
        clientsCountLabel.setText("Всего: " + clients.size());
    }

    private void setMessage(String text) {
        messageLabel.setText(text);
    }

    private void setConnected(boolean connected) {
        String target;
        try {
            target = DatabaseManager.describe();
        } catch (RuntimeException e) {
            target = "database.properties";
        }
        connectionLabel.setText(connected ? "● PostgreSQL: " + target : "● Нет подключения: " + target);
        connectionLabel.getStyleClass().removeAll("connection-ok", "connection-error");
        connectionLabel.getStyleClass().add(connected ? "connection-ok" : "connection-error");
    }

    private Window window() {
        return ordersTable.getScene() == null ? null : ordersTable.getScene().getWindow();
    }
}
