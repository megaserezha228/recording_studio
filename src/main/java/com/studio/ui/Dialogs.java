package com.studio.ui;

import com.studio.exception.BusinessException;
import com.studio.exception.DatabaseException;
import com.studio.exception.EntityNotFoundException;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Стандартные диалоговые окна приложения: ошибка, информация, подтверждение.
 * <p>
 * Пользователь видит понятный текст, а не stack trace.
 * Технические подробности пишутся в журнал (slf4j) там, где ошибка перехвачена.
 */
public final class Dialogs {

    private static final String STYLESHEET = "/com/studio/css/styles.css";

    private Dialogs() {
    }

    public static void showError(Window owner, String header, Throwable error) {
        showError(owner, header, userMessage(error));
    }

    public static void showError(Window owner, String header, String message) {
        Alert alert = create(Alert.AlertType.ERROR, owner, "Ошибка", header, message);
        alert.showAndWait();
    }

    public static void showInfo(Window owner, String title, String header, String message) {
        Alert alert = create(Alert.AlertType.INFORMATION, owner, title, header, message);
        alert.showAndWait();
    }

    /**
     * Окно подтверждения опасного действия (например, удаления).
     * По умолчанию выделена кнопка «Отмена», чтобы случайный Enter ничего не удалил.
     *
     * @return true — пользователь подтвердил действие
     */
    public static boolean confirm(Window owner, String title, String header,
                                  String message, String confirmButtonText) {
        ButtonType confirmType = new ButtonType(confirmButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = create(Alert.AlertType.CONFIRMATION, owner, title, header, message);
        alert.getButtonTypes().setAll(confirmType, cancelType);

        Button confirmButton = (Button) alert.getDialogPane().lookupButton(confirmType);
        confirmButton.setDefaultButton(false);
        confirmButton.getStyleClass().add("button-danger");
        ((Button) alert.getDialogPane().lookupButton(cancelType)).setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmType;
    }

    /** Превращает исключение в текст, понятный пользователю. */
    public static String userMessage(Throwable error) {
        if (error instanceof BusinessException || error instanceof EntityNotFoundException) {
            return error.getMessage();
        }
        if (error instanceof DatabaseException) {
            StringBuilder text = new StringBuilder(error.getMessage());
            Throwable cause = error.getCause();
            if (cause != null && cause.getMessage() != null) {
                text.append("\n\nПричина: ").append(cause.getMessage().lines().findFirst().orElse(""));
            }
            text.append("\n\nПроверьте, что PostgreSQL запущен, а параметры подключения "
                    + "в файле database.properties указаны верно.");
            return text.toString();
        }
        return "Произошла непредвиденная ошибка. Подробности записаны в журнал приложения.";
    }

    private static Alert create(Alert.AlertType type, Window owner,
                                String title, String header, String message) {
        Alert alert = new Alert(type);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(460);
        var css = Dialogs.class.getResource(STYLESHEET);
        if (css != null) {
            alert.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        return alert;
    }
}
