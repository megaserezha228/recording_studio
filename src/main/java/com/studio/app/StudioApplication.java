package com.studio.app;

import com.studio.controller.MainController;
import com.studio.repository.ClientRepository;
import com.studio.repository.RecordingOrderRepository;
import com.studio.repository.impl.ClientRepositoryImpl;
import com.studio.repository.impl.RecordingOrderRepositoryImpl;
import com.studio.service.RecordingOrderService;
import com.studio.ui.Dialogs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * JavaFX-приложение «Студия звукозаписи».
 * <p>
 * Stage — окно, Scene — содержимое окна, Node — любой элемент интерфейса
 * (кнопка, таблица, панель). Интерфейс главного окна описан в main-view.fxml,
 * а его поведение — в {@link MainController}.
 * <p>
 * Слои: FXML (View) → Controller → Service → Repository (DAO, JDBC) → PostgreSQL.
 */
public class StudioApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(StudioApplication.class);

    private static final String MAIN_VIEW = "/com/studio/view/main-view.fxml";
    private static final String STYLESHEET = "/com/studio/css/styles.css";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        installGlobalErrorHandler();

        // ===== Сборка слоёв (внедрение зависимостей вручную) =====
        ClientRepository clientRepository = new ClientRepositoryImpl();
        RecordingOrderRepository orderRepository = new RecordingOrderRepositoryImpl();
        RecordingOrderService orderService = new RecordingOrderService(orderRepository, clientRepository);

        // ===== Загрузка интерфейса из FXML =====
        FXMLLoader loader = new FXMLLoader(resource(MAIN_VIEW));
        // FXMLLoader сам создаёт контроллер, указанный в fx:controller.
        // Фабрика контроллеров позволяет передать в него сервис через конструктор.
        loader.setControllerFactory(type -> {
            if (type == MainController.class) {
                return new MainController(orderService);
            }
            throw new IllegalArgumentException("Неизвестный контроллер: " + type.getName());
        });
        Parent root = loader.load();
        MainController controller = loader.getController();

        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add(resource(STYLESHEET).toExternalForm());

        stage.setTitle("Студия звукозаписи — заказы на запись");
        stage.setMinWidth(1040);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();

        // Данные загружаем после показа окна, чтобы возможное сообщение
        // об ошибке подключения появилось поверх главного окна.
        Platform.runLater(controller::loadData);
        log.info("Главное окно открыто");
    }

    /**
     * Любое неперехваченное исключение в потоке JavaFX показывается пользователю
     * понятным сообщением, а подробности уходят в журнал.
     */
    private void installGlobalErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            log.error("Необработанная ошибка в потоке {}", thread.getName(), error);
            if (Platform.isFxApplicationThread()) {
                Dialogs.showError(null, "Что-то пошло не так", error);
            }
        });
    }

    private static URL resource(String path) {
        return Objects.requireNonNull(StudioApplication.class.getResource(path),
                "Не найден ресурс " + path);
    }
}
