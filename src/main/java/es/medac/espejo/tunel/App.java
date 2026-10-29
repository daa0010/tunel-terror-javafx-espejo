package es.medac.espejo.tunel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class App extends Application {

    public static final String PROPIETARIO = "espejo";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/medac/espejo/tunel/sala.fxml"));
        Parent root = loader.load();
        SalaController controller = loader.getController();

        Scene scene = new Scene(root);

        // Atajo: Ctrl + Shift + Q para salir
        KeyCombination comboCerrar = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

        scene.setOnKeyPressed(event -> {
            if (comboCerrar.match(event)) {
                controller.detener();
                Platform.exit();
            } else if (event.getCode() == KeyCode.F12) {
                controller.reiniciar();
            }
        });

        stage.setTitle("SALA: El Espejo | Guardián: " + PROPIETARIO);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}