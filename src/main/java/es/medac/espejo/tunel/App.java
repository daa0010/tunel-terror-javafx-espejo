package es.medac.espejo.tunel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class App extends Application {

    // Constante obligatoria para el título de la ventana
    public static final String PROPIETARIO = "espejo";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/medac/espejo/tunel/sala.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("SALA: El Espejo | Guardián: " + PROPIETARIO);
        stage.setScene(scene);

        // Bloqueo estricto de pantalla completa (sin aviso ni salida accidental)
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}