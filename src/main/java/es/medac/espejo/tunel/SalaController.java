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

    // Silencio -> aparece la cara de golpe con un grito -> desaparece -> letra
    private void susto() {
        txtNumero.setDisable(true);
        piedad.stop();
        ambiente.pause();
        lblMensaje.setText("Ahora... date la vuelta.");
        PauseTransition silencio = new PauseTransition(Duration.seconds(2));
        silencio.setOnFinished(e -> {
            imgSusto.setVisible(true);
            efecto("grito.wav");
            ScaleTransition aparece = new ScaleTransition(Duration.millis(250), imgSusto);
            aparece.setFromX(0.2); aparece.setFromY(0.2);
            aparece.setToX(1.1); aparece.setToY(1.1);
            PauseTransition mirada = new PauseTransition(Duration.seconds(2));
            SequentialTransition todo = new SequentialTransition(aparece, mirada);
            todo.setOnFinished(ev -> {
                imgSusto.setVisible(false);
                ambiente.play();
                mostrarLetra();
            });
            todo.play();
        });
        silencio.play();
    }

    public void reiniciar() {
        autoReinicio.stop();
        txtNumero.clear();
        txtNumero.setDisable(false);
        imgSusto.setVisible(false);
        lblLetra.setVisible(false);
        lblAyuda.setText("");
        lblMensaje.setText("Hay alguien detrás de ti en el reflejo. ¿Cuál es su número?");
        ambiente.play();
        txtNumero.requestFocus();
        piedad.playFromStart();
    }

    private void mostrarAyuda() {
        lblAyuda.setText("El espejo susurra: tiene " + NUMERO.length() + " cifras y empieza por " + NUMERO.charAt(0));
    }

    // ---------- Comun a todas las salas ----------
    private void mostrarLetra() {
        piedad.stop();
        lblLetra.setText(LETRA);
        lblLetra.setVisible(true);
        ScaleTransition escala = new ScaleTransition(Duration.millis(700), lblLetra);
        escala.setFromX(0.1); escala.setFromY(0.1);
        escala.setToX(1); escala.setToY(1);
        FadeTransition aparicion = new FadeTransition(Duration.millis(700), lblLetra);
        aparicion.setFromValue(0); aparicion.setToValue(1);
        new ParallelTransition(escala, aparicion).play();
        efecto("acierto.wav");
        autoReinicio.playFromStart();   // la sala se reinicia sola para el siguiente grupo
    }

}