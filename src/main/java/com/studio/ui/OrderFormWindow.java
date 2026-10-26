package com.studio.ui;

import com.studio.controller.OrderFormController;
import com.studio.model.RecordingOrder;
import com.studio.service.ClientService;
import com.studio.service.RecordingOrderService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

/**
 * Открывает форму заказа (order-form.fxml) в отдельном модальном окне.
 * <p>
 * Модальность WINDOW_MODAL: пока форма открыта, главное окно недоступно,
 * а {@code showAndWait()} возвращает управление только после закрытия формы.
 */
public final class OrderFormWindow {

    private static final String FORM_VIEW = "/com/studio/view/order-form.fxml";
    private static final String STYLESHEET = "/com/studio/css/styles.css";

    private OrderFormWindow() {
    }

    /**
     * @param order заказ для редактирования или null для добавления
     * @return сохранённый заказ; пусто, если пользователь закрыл форму без сохранения
     */
    public static Optional<RecordingOrder> show(Window owner, RecordingOrderService orderService,
                                                ClientService clientService, RecordingOrder order) {
        FXMLLoader loader = new FXMLLoader(resource(FORM_VIEW));
        loader.setControllerFactory(type -> new OrderFormController(orderService, clientService));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить форму заказа", e);
        }
        OrderFormController controller = loader.getController();
        controller.init(order);   // список клиентов читается из БД здесь, до показа окна

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(order == null ? "Новый заказ" : "Изменение заказа");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(resource(STYLESHEET).toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(520);
        stage.setMinHeight(560);
        stage.showAndWait();

        return controller.getSavedOrder();
    }

    private static URL resource(String path) {
        return Objects.requireNonNull(OrderFormWindow.class.getResource(path), "Не найден ресурс " + path);
    }
}
