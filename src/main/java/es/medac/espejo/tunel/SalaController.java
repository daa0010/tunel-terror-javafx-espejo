package es.medac.espejo.tunel;

import java.util.*;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class SalaController {

    private static final String LETRA = "D";
    private static final String NUMERO = "13";   // pista acordada con 1.º

    @FXML private Rectangle espejo;
    @FXML private TextField txtNumero;
    @FXML private ImageView imgSusto;
    @FXML private Label lblMensaje, lblLetra, lblAyuda;

    private FadeTransition respiracion;
    private MediaPlayer ambiente;
    private final PauseTransition piedad = new PauseTransition(Duration.seconds(60));
    private final PauseTransition autoReinicio = new PauseTransition(Duration.seconds(12));

    @FXML
    private void initialize() {
        // El espejo "respira": cambia de opacidad muy despacio (nada de destellos)
        respiracion = new FadeTransition(Duration.seconds(3), espejo);
        respiracion.setFromValue(0.55);
        respiracion.setToValue(1.0);
        respiracion.setAutoReverse(true);
        respiracion.setCycleCount(Animation.INDEFINITE);
        respiracion.play();
        ambiente = new MediaPlayer(new Media(recurso("sonidos/ambiente.mp3")));
        ambiente.setCycleCount(MediaPlayer.INDEFINITE);
        ambiente.setVolume(0.4);
        ambiente.play();
        piedad.setOnFinished(e -> mostrarAyuda());
        autoReinicio.setOnFinished(e -> reiniciar());
        reiniciar();
    }

    @FXML
    private void comprobar() {
        if (txtNumero.isDisabled()) return;
        if (txtNumero.getText().trim().equals(NUMERO)) {
            susto();
        } else {
            efecto("fallo.wav");
            lblMensaje.setText("El espejo no te reconoce...");
            temblar(espejo);
            txtNumero.clear();
        }
    }

    public void detener() {
        ambiente.stop();
    }
}