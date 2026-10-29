package es.medac.espejo.tunel;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class SalaController {

    private static final String LETRA = "D";
    private static final String NUMERO = "13"; // Pista acordada

    @FXML private StackPane raiz;
    @FXML private Rectangle espejo;
    @FXML private Button btnIniciarJuego;
    @FXML private VBox panelCodigo;
    @FXML private TextField txtNumero;
    @FXML private Pane panelMinijuego;
    @FXML private ImageView imgSusto;
    @FXML private Label lblMensaje, lblLetra;

    // Componentes del minijuego
    private Circle orbe;
    private AnimationTimer bucleMinijuego;
    private double ratonX = -100, ratonY = -100;
    private double orbeX = 400, orbeY = 300;
    private double velX = 140, velY = 110;
    private double tiempoSeguido = 0.0;
    private double tiempoFallo = 0.0;
    private long ultimoTiempoNano = 0;
    private boolean juegoCompletado = false;

    // Animaciones y multimedia
    private FadeTransition respiracion;
    private MediaPlayer ambiente;
    private AudioClip audioFallo;
    private MediaPlayer playerSusto;
    private MediaPlayer playerAcierto;

    private final PauseTransition autoReinicio = new PauseTransition(Duration.seconds(12));

    @FXML
    private void initialize() {
        // Carga de audios
        audioFallo = new AudioClip(recurso("sonidos/fallo.wav"));
        try {
            audioFallo.play(0.0);
        } catch (Exception ignored) {}

        Media mediaSusto = new Media(recurso("sonidos/susto.mp3"));
        playerSusto = new MediaPlayer(mediaSusto);
        playerSusto.setOnError(() -> System.err.println("Error en susto.mp3: " + playerSusto.getError()));

        Media mediaAcierto = new Media(recurso("sonidos/acierto.mp3"));
        playerAcierto = new MediaPlayer(mediaAcierto);
        playerAcierto.setOnError(() -> System.err.println("Error en acierto.mp3: " + playerAcierto.getError()));

        // Efecto respiración del espejo
        respiracion = new FadeTransition(Duration.seconds(3), espejo);
        respiracion.setFromValue(0.55);
        respiracion.setToValue(1.0);
        respiracion.setAutoReverse(true);
        respiracion.setCycleCount(Animation.INDEFINITE);
        respiracion.play();

        // Audio ambiental
        ambiente = new MediaPlayer(new Media(recurso("sonidos/ambiente.mp3")));
        ambiente.setCycleCount(MediaPlayer.INDEFINITE);
        ambiente.setVolume(0.3);
        ambiente.play();

        // Configuración visual del orbe
        orbe = new Circle(32);
        orbe.setFill(Color.web("#8b0000", 0.6));
        orbe.setStroke(Color.web("#ff3333"));
        orbe.setStrokeWidth(3);
        orbe.setEffect(new DropShadow(25, Color.RED));
        orbe.setVisible(false);
        panelMinijuego.getChildren().add(orbe);

        // Captura del ratón
        raiz.setOnMouseMoved(e -> {
            ratonX = e.getX();
            ratonY = e.getY();
        });

        configurarBucleMinijuego();

        autoReinicio.setOnFinished(e -> reiniciar());
        reiniciar();
    }

    private void configurarBucleMinijuego() {
        bucleMinijuego = new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                if (ultimoTiempoNano == 0) {
                    ultimoTiempoNano = ahora;
                    return;
                }
                double dt = (ahora - ultimoTiempoNano) / 1e9;
                ultimoTiempoNano = ahora;

                double ancho = raiz.getWidth() > 0 ? raiz.getWidth() : 1280;
                double alto = raiz.getHeight() > 0 ? raiz.getHeight() : 720;
                double radio = orbe.getRadius();

                orbeX += velX * dt;
                orbeY += velY * dt;

                if (orbeX - radio <= 40 || orbeX + radio >= ancho - 40) {
                    velX = -velX;
                }
                if (orbeY - radio <= 40 || orbeY + radio >= alto - 40) {
                    velY = -velY;
                }

                orbe.setCenterX(orbeX);
                orbe.setCenterY(orbeY);

                // Detección de proximidad del ratón
                double dist = Math.hypot(ratonX - orbeX, ratonY - orbeY);
                boolean sobreOrbe = dist <= (radio + 25);

                if (sobreOrbe) {
                    tiempoSeguido += dt;
                    tiempoFallo = 0.0;
                    orbe.setFill(Color.web("#ff2222", 0.85));
                    lblMensaje.setText("Sigue la presencia con tu reflejo...");
                } else {
                    tiempoFallo += dt;
                    orbe.setFill(Color.web("#8b0000", 0.5));
                    lblMensaje.setText("Acompaña el punto con el ratón");

                    // Tolerancia de 2 segundos: si se excede, se reinicia el progreso desde cero
                    if (tiempoFallo > 2.0 && tiempoSeguido > 0.0) {
                        tiempoSeguido = 0.0;
                        temblar(espejo);
                    }
                }

                // Superar los 12 segundos
                if (tiempoSeguido >= 12.0) {
                    finalizarMinijuego();
                }
            }
        };
    }

    @FXML
    public void empezarMinijuego() {
        btnIniciarJuego.setVisible(false);
        btnIniciarJuego.setManaged(false);
        iniciarMinijuego();
    }

    private void iniciarMinijuego() {
        juegoCompletado = false;
        tiempoSeguido = 0.0;
        tiempoFallo = 0.0;
        ultimoTiempoNano = 0;
        panelCodigo.setVisible(false);
        panelCodigo.setManaged(false);
        orbe.setVisible(true);
        orbeX = 400;
        orbeY = 300;
        bucleMinijuego.start();
    }

    private void finalizarMinijuego() {
        if (bucleMinijuego != null) bucleMinijuego.stop();
        juegoCompletado = true;
        orbe.setVisible(false);
        btnIniciarJuego.setVisible(false);
        btnIniciarJuego.setManaged(false);

        lblMensaje.setText("Parece que hay alguien detrás de ti. Quizá sepa algo...");
        panelCodigo.setVisible(true);
        panelCodigo.setManaged(true);
        txtNumero.requestFocus();
    }

    @FXML
    private void comprobar() {
        if (!juegoCompletado || txtNumero.isDisabled()) return;

        if (txtNumero.getText().trim().equals(NUMERO)) {
            susto();
        } else {
            audioFallo.play();
            lblMensaje.setText("El espejo no te reconoce...");
            temblar(espejo);
            txtNumero.clear();
        }
    }

    // Jumpscare
    public void susto() {
        txtNumero.setDisable(true);
        ambiente.pause();
        lblMensaje.setText("Pasa algo...");

        PauseTransition silencio = new PauseTransition(Duration.seconds(1.8));
        silencio.setOnFinished(e -> {
            imgSusto.setVisible(true);

            playerSusto.seek(Duration.ZERO);
            playerSusto.play();

            // Zoom inicial de choque
            ScaleTransition impacto = new ScaleTransition(Duration.millis(70), imgSusto);
            impacto.setFromX(0.8);
            impacto.setFromY(0.8);
            impacto.setToX(1.35);
            impacto.setToY(1.35);

            // Sacudida violenta sostenida
            TranslateTransition convulsion = new TranslateTransition(Duration.millis(35), imgSusto);
            convulsion.setByX(25);
            convulsion.setByY(15);
            convulsion.setCycleCount(80);
            convulsion.setAutoReverse(true);

            ParallelTransition sustoAnim = new ParallelTransition(impacto, convulsion);
            sustoAnim.setOnFinished(ev -> {
                imgSusto.setTranslateX(0);
                imgSusto.setTranslateY(0);
                imgSusto.setScaleX(1.0);
                imgSusto.setScaleY(1.0);
                imgSusto.setVisible(false);
                playerSusto.stop();

                mostrarLetra();

                PauseTransition retardoAmbiente = new PauseTransition(Duration.seconds(1));
                retardoAmbiente.setOnFinished(amb -> ambiente.play());
                retardoAmbiente.play();
            });

            sustoAnim.play();
        });
        silencio.play();
    }

    // --- MÉTODOS DE BETATESTER Y CONTROL ---

    public void reiniciar() {
        autoReinicio.stop();
        if (bucleMinijuego != null) bucleMinijuego.stop();
        if (playerSusto != null) playerSusto.stop();
        if (playerAcierto != null) playerAcierto.stop();

        juegoCompletado = false;
        orbe.setVisible(false);
        imgSusto.setVisible(false);
        lblLetra.setVisible(false);

        btnIniciarJuego.setVisible(true);
        btnIniciarJuego.setManaged(true);

        panelCodigo.setVisible(false);
        panelCodigo.setManaged(false);

        txtNumero.clear();
        txtNumero.setDisable(false);

        lblMensaje.setText("El reflejo está inquieto. Concéntrate para invocar la presencia.");
        ambiente.play();
    }

    public void testSaltarMinijuego() {
        finalizarMinijuego();
    }

    public void testSustoDirecto() {
        if (bucleMinijuego != null) bucleMinijuego.stop();
        orbe.setVisible(false);
        btnIniciarJuego.setVisible(false);
        btnIniciarJuego.setManaged(false);
        panelCodigo.setVisible(false);
        panelCodigo.setManaged(false);
        juegoCompletado = true;
        susto();
    }

    private void mostrarLetra() {
        lblLetra.setText(LETRA);
        lblLetra.setVisible(true);
        ScaleTransition escala = new ScaleTransition(Duration.millis(700), lblLetra);
        escala.setFromX(0.1); escala.setFromY(0.1);
        escala.setToX(1); escala.setToY(1);
        FadeTransition aparicion = new FadeTransition(Duration.millis(700), lblLetra);
        aparicion.setFromValue(0); aparicion.setToValue(1);
        new ParallelTransition(escala, aparicion).play();

        playerAcierto.seek(Duration.ZERO);
        playerAcierto.play();

        autoReinicio.playFromStart();
    }

    private void temblar(Node nodo) {
        TranslateTransition t = new TranslateTransition(Duration.millis(50), nodo);
        t.setFromX(-8); t.setToX(8);
        t.setCycleCount(6); t.setAutoReverse(true);
        t.setOnFinished(e -> nodo.setTranslateX(0));
        t.play();
    }

    private String recurso(String ruta) {
        return getClass().getResource(ruta).toExternalForm();
    }

    public void detener() {
        if (bucleMinijuego != null) bucleMinijuego.stop();
        if (ambiente != null) ambiente.stop();
        if (playerSusto != null) playerSusto.stop();
        if (playerAcierto != null) playerAcierto.stop();
    }
}