package com.studio.controller;

import com.studio.model.Client;
import com.studio.model.ClientRole;
import com.studio.model.OrderStatus;
import com.studio.model.RecordingOrder;
import com.studio.service.ClientOrdersSummary;
import com.studio.service.RecordingOrderService;
import com.studio.ui.Formatters;
import com.studio.ui.StatusBadgeCell;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Контроллер вкладки «Клиенты» (clients-tab.fxml).
 * <p>
 * Показывает таблицу клиентов, а для выбранного клиента — карточку и связанные с ним заказы.
 * Данные из БД сюда передаёт {@link MainController} после каждой загрузки ({@link #setData}),
 * поэтому вкладка всегда показывает то же состояние, что и вкладка «Заказы».
 */
public class ClientsTabController {

    private final RecordingOrderService orderService;

    /** Все заказы последней загрузки: из них выбираются заказы клиента. */
    private List<RecordingOrder> allOrders = List.of();

    /** Количество заказов у каждого клиента: id клиента → число заказов. */
    private Map<Integer, Long> ordersPerClient = Map.of();

    /** Клиенты для таблицы (ObservableList — таблица обновляется сама). */
    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    /** Заказы выбранного клиента для таблицы в карточке. */
    private final ObservableList<RecordingOrder> clientOrders = FXCollections.observableArrayList();

    /** Что сделать по кнопке «Обновить» и при открытии заказа — задаёт главное окно. */
    private Runnable refreshAction = () -> { };
    private IntConsumer openOrderAction = orderId -> { };

    @FXML private Label clientsCountLabel;

    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Integer> clientIdColumn;
    @FXML private TableColumn<Client, String> clientNameColumn;
    @FXML private TableColumn<Client, ClientRole> clientRoleColumn;
    @FXML private TableColumn<Client, String> clientEmailColumn;
    @FXML private TableColumn<Client, String> clientPhoneColumn;
    @FXML private TableColumn<Client, Long> clientOrdersColumn;

    @FXML private Label clientPlaceholder;
    @FXML private VBox clientDetails;
    @FXML private Label clientName;
    @FXML private Label clientRole;
    @FXML private Label clientEmail;
    @FXML private Label clientPhone;
    @FXML private Label clientSince;
    @FXML private Label clientOrdersSummary;

    @FXML private TableView<RecordingOrder> clientOrdersTable;
    @FXML private TableColumn<RecordingOrder, String> orderTitleColumn;
    @FXML private TableColumn<RecordingOrder, String> orderDateColumn;
    @FXML private TableColumn<RecordingOrder, OrderStatus> orderStatusColumn;
    @FXML private TableColumn<RecordingOrder, String> orderPriceColumn;
    @FXML private Button openOrderButton;

    public ClientsTabController(RecordingOrderService orderService) {
        this.orderService = orderService;
    }

    @FXML
    private void initialize() {
        setupClientsTable();
        setupClientOrdersTable();

        clientsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldClient, newClient) -> showClient(newClient));
        openOrderButton.disableProperty()
                .bind(clientOrdersTable.getSelectionModel().selectedItemProperty().isNull());

        showClient(null);
        updateCount();
    }

    /**
     * Таблица клиентов. Здесь включена встроенная сортировка TableView:
     * щелчок по заголовку колонки сортирует строки (повторный — в обратном порядке).
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

    /** Таблица заказов выбранного клиента; двойной щелчок открывает заказ на вкладке «Заказы». */
    private void setupClientOrdersTable() {
        clientOrdersTable.setItems(clientOrders);
        clientOrdersTable.setPlaceholder(new Label("У клиента пока нет заказов."));

        orderTitleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitle()));
        orderDateColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Formatters.date(c.getValue().getRecordingDate())));
        orderStatusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        orderPriceColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Formatters.money(c.getValue().getPrice())));
        orderStatusColumn.setCellFactory(column -> new StatusBadgeCell<>());
        orderPriceColumn.getStyleClass().add("column-right");

        clientOrdersTable.setRowFactory(table -> {
            TableRow<RecordingOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2 && !row.isEmpty()) {
                    openOrderAction.accept(row.getItem().getId());
                }
            });
            return row;
        });
    }

    // =====================================================================
    // СВЯЗЬ С ГЛАВНЫМ ОКНОМ
    // =====================================================================

    /**
     * @param refreshAction   перечитать данные из БД (кнопка «Обновить»)
     * @param openOrderAction показать заказ с данным id на вкладке «Заказы»
     */
    public void setActions(Runnable refreshAction, IntConsumer openOrderAction) {
        this.refreshAction = refreshAction;
        this.openOrderAction = openOrderAction;
    }

    /**
     * Принимает свежие данные из БД. Выбранный клиент остаётся выбранным,
     * его карточка и список заказов пересчитываются.
     */
    public void setData(List<Client> loadedClients, List<RecordingOrder> loadedOrders) {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();

        allOrders = loadedOrders;
        // Счётчики заказов задаются до setAll, чтобы новые строки сразу показали верные значения.
        ordersPerClient = orderService.countOrdersByClient(loadedOrders);
        clients.setAll(loadedClients);
        clientsTable.sort();   // сохранить сортировку, выбранную щелчком по заголовку
        clientsTable.setPlaceholder(new Label("В базе пока нет клиентов."));
        updateCount();

        if (selected != null) {
            selectClient(selected.getId());
        }
        showClient(clientsTable.getSelectionModel().getSelectedItem());
    }

    /** Данные не загрузились: таблицы пусты, вместо них — подсказка. */
    public void showLoadError() {
        allOrders = List.of();
        ordersPerClient = Map.of();
        clients.clear();
        clientsTable.setPlaceholder(new Label(
                "Нет данных: не удалось подключиться к базе. Проверьте подключение и нажмите «Обновить»."));
        updateCount();
        showClient(null);
    }

    /** Выделяет клиента и прокручивает таблицу к нему (переход из карточки заказа). */
    public void selectClient(int clientId) {
        clients.stream()
                .filter(c -> c.getId() == clientId)
                .findFirst()
                .ifPresentOrElse(client -> {
                    clientsTable.getSelectionModel().select(client);
                    clientsTable.scrollTo(client);
                }, () -> clientsTable.getSelectionModel().clearSelection());
    }

    // =====================================================================
    // ОБРАБОТЧИКИ
    // =====================================================================

    @FXML
    private void onRefresh() {
        refreshAction.run();
    }

    @FXML
    private void onOpenOrder() {
        RecordingOrder order = clientOrdersTable.getSelectionModel().getSelectedItem();
        if (order != null) {
            openOrderAction.accept(order.getId());
        }
    }

    // =====================================================================
    // КАРТОЧКА КЛИЕНТА
    // =====================================================================

    private void showClient(Client client) {
        boolean hasClient = client != null;
        clientDetails.setVisible(hasClient);
        clientDetails.setManaged(hasClient);
        clientPlaceholder.setVisible(!hasClient);
        clientPlaceholder.setManaged(!hasClient);
        if (!hasClient) {
            clientOrders.clear();
            return;
        }
        clientName.setText(client.getFullName());
        clientRole.setText(client.getRole().getTitle());
        clientEmail.setText(Formatters.orDash(client.getEmail()));
        clientPhone.setText(Formatters.orDash(client.getPhone()));
        clientSince.setText(client.getCreatedAt() == null ? "—" : Formatters.date(client.getCreatedAt().toLocalDate()));

        List<RecordingOrder> orders = orderService.ordersOfClient(allOrders, client.getId());
        clientOrders.setAll(orders);
        ClientOrdersSummary summary = orderService.summarize(orders);
        clientOrdersSummary.setText(summary.total() == 0
                ? "Заказов нет."
                : "Всего: " + summary.total() + " · активных: " + summary.active()
                        + " · на сумму " + Formatters.money((int) summary.totalPrice()) + " (без отменённых)");
    }

    private void updateCount() {
        clientsCountLabel.setText("Всего: " + clients.size());
    }
}
