package es.medac.espejo.tunel;

import java.util.Random;
import java.util.Set;
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
    // Conjunto de opciones válidas simétricas
    private static final Set<String> NUMEROS_VALIDOS = Set.of("0", "1", "8");
    private static final double TIEMPO_OBJETIVO_MINIJUEGO = 10.0;

    @FXML private StackPane raiz;
    @FXML private Region capaAlertaRoja;
    @FXML private Rectangle espejo;
    @FXML private HBox ojosEspejo;
    @FXML private Circle pupilaIzquierda;
    @FXML private Circle pupilaDerecha;
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
    private double destinoX, destinoY;
    private double velocidadActual = 180.0;
    private final Random random = new Random();

    private double tiempoSeguido = 0.0;
    private double tiempoFallo = 0.0;
    private double intensidadAlerta = 0.0;
    private long ultimoTiempoNano = 0;
    private boolean juegoCompletado = false;
    private int fallosMinijuego = 0;

    // Animaciones y multimedia
    private FadeTransition respiracion;
    private Timeline bucleParpadeo;
    private MediaPlayer ambiente;
    private AudioClip audioFallo;
    private MediaPlayer playerSusto;
    private MediaPlayer playerAcierto;

    private final PauseTransition autoReinicio = new PauseTransition(Duration.seconds(12));

    @FXML
    private void initialize() {
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

        respiracion = new FadeTransition(Duration.seconds(3), espejo);
        respiracion.setFromValue(0.55);
        respiracion.setToValue(1.0);
        respiracion.setAutoReverse(true);
        respiracion.setCycleCount(Animation.INDEFINITE);
        respiracion.play();

        ambiente = new MediaPlayer(new Media(recurso("sonidos/ambiente.mp3")));
        ambiente.setCycleCount(MediaPlayer.INDEFINITE);
        ambiente.setVolume(0.3);
        ambiente.play();

        orbe = new Circle(32);
        orbe.setFill(Color.web("#8b0000", 0.6));
        orbe.setStroke(Color.web("#ff3333"));
        orbe.setStrokeWidth(3);
        orbe.setEffect(new DropShadow(25, Color.RED));
        orbe.setVisible(false);
        panelMinijuego.getChildren().add(orbe);

        raiz.setOnMouseMoved(e -> {
            ratonX = e.getX();
            ratonY = e.getY();
            actualizarMiradaOjos(ratonX, ratonY);
        });

        configurarBucleMinijuego();
        iniciarParpadeo();

        autoReinicio.setOnFinished(e -> reiniciar());
        reiniciar();
    }

    private void actualizarMiradaOjos(double x, double y) {
        if (pupilaIzquierda == null || pupilaDerecha == null || raiz == null) return;

        double centroX = raiz.getWidth() / 2.0;
        double centroY = (raiz.getHeight() / 2.0) - 110.0;

        double dx = x - centroX;
        double dy = y - centroY;
        double distancia = Math.hypot(dx, dy);

        double limiteDesplazamiento = 11.0;

        if (distancia > 0) {
            double factor = Math.min(distancia / 350.0, 1.0) * limiteDesplazamiento;
            double offsetX = (dx / distancia) * factor;
            double offsetY = (dy / distancia) * (factor * 0.5);

            pupilaIzquierda.setTranslateX(offsetX);
            pupilaIzquierda.setTranslateY(offsetY);

            pupilaDerecha.setTranslateX(offsetX);
            pupilaDerecha.setTranslateY(offsetY);
        }
    }

    private void iniciarParpadeo() {
        bucleParpadeo = new Timeline(new KeyFrame(Duration.seconds(4.8), e -> {
            ScaleTransition parpadeo = new ScaleTransition(Duration.millis(110), ojosEspejo);
            parpadeo.setFromY(1.0);
            parpadeo.setToY(0.04);
            parpadeo.setAutoReverse(true);
            parpadeo.setCycleCount(2);
            parpadeo.play();
        }));
        bucleParpadeo.setCycleCount(Animation.INDEFINITE);
        bucleParpadeo.play();
    }

    private void generarNuevoDestino(double ancho, double alto) {
        double minX = 90;
        double maxX = Math.max(minX + 120, ancho - 90);
        double minY = 90;
        double maxY = Math.max(minY + 120, alto - 90);

        if (orbeY > alto * 0.5) {
            destinoY = minY + random.nextDouble() * ((alto * 0.42) - minY);
        } else {
            destinoY = (alto * 0.58) + random.nextDouble() * (maxY - (alto * 0.58));
        }

        destinoX = minX + random.nextDouble() * (maxX - minX);
        velocidadActual = 160.0 + random.nextDouble() * 100.0;
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

                double dx = destinoX - orbeX;
                double dy = destinoY - orbeY;
                double distDestino = Math.hypot(dx, dy);

                if (distDestino < 25.0) {
                    generarNuevoDestino(ancho, alto);
                } else {
                    orbeX += (dx / distDestino) * velocidadActual * dt;
                    orbeY += (dy / distDestino) * velocidadActual * dt;
                }

                orbeX = Math.max(radio + 30, Math.min(ancho - radio - 30, orbeX));
                orbeY = Math.max(radio + 30, Math.min(alto - radio - 30, orbeY));

                orbe.setCenterX(orbeX);
                orbe.setCenterY(orbeY);

                double distRaton = Math.hypot(ratonX - orbeX, ratonY - orbeY);
                boolean sobreOrbe = distRaton <= (radio + 25);

                if (sobreOrbe) {
                    tiempoSeguido += dt;
                    tiempoFallo = 0.0;
                    orbe.setFill(Color.web("#ff2222", 0.85));
                    lblMensaje.setText("Sigue a la presencia con tu reflejo...");
                    intensidadAlerta = Math.max(0.0, intensidadAlerta - dt * 2.8);
                } else {
                    tiempoFallo += dt;
                    orbe.setFill(Color.web("#8b0000", 0.5));
                    lblMensaje.setText("Acompaña el punto con el ratón");
                    intensidadAlerta = Math.min(1.0, tiempoFallo / 3.0);

                    if (tiempoFallo >= 3.0) {
                        fallarMinijuego();
                        return;
                    }
                }

                capaAlertaRoja.setOpacity(intensidadAlerta);

                double progreso = Math.min(1.0, tiempoSeguido / TIEMPO_OBJETIVO_MINIJUEGO);
                double escalaOjos = 1.0 + (progreso * 0.70);
                ojosEspejo.setScaleX(escalaOjos);
                ojosEspejo.setScaleY(escalaOjos);

                if (tiempoSeguido >= TIEMPO_OBJETIVO_MINIJUEGO) {
                    finalizarMinijuego();
                }
            }
        };
    }

    private void resetearOjosYEfectos() {
        if (ojosEspejo != null) {
            ojosEspejo.setScaleX(1.0);
            ojosEspejo.setScaleY(1.0);
        }
        if (capaAlertaRoja != null) {
            capaAlertaRoja.setOpacity(0.0);
        }
        intensidadAlerta = 0.0;
    }

    private void fallarMinijuego() {
        if (bucleMinijuego != null) bucleMinijuego.stop();
        orbe.setVisible(false);
        tiempoSeguido = 0.0;
        tiempoFallo = 0.0;

        resetearOjosYEfectos();

        temblar(espejo);
        audioFallo.play();

        fallosMinijuego++;

        if (fallosMinijuego >= 4) {
            finalizarMinijuego();
        } else {
            lblMensaje.setText("Has perdido el rastro...\nPulsa para volver a intentarlo.");
            btnIniciarJuego.setText("Volver a intentar");
            btnIniciarJuego.setVisible(true);
            btnIniciarJuego.setManaged(true);
        }
    }

    @FXML
    public void empezarMinijuego() {
        if (lblLetra != null && lblLetra.isVisible()) {
            reiniciar();
            return;
        }

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

        resetearOjosYEfectos();

        if (ratonX > 0 && ratonY > 0) {
            orbeX = ratonX;
            orbeY = ratonY;
        } else {
            orbeX = raiz.getWidth() > 0 ? raiz.getWidth() / 2.0 : 400;
            orbeY = raiz.getHeight() > 0 ? raiz.getHeight() / 2.0 : 300;
        }
        orbe.setCenterX(orbeX);
        orbe.setCenterY(orbeY);
        orbe.setVisible(true);

        double ancho = raiz.getWidth() > 0 ? raiz.getWidth() : 1280;
        double alto = raiz.getHeight() > 0 ? raiz.getHeight() : 720;
        generarNuevoDestino(ancho, alto);

        bucleMinijuego.start();
    }

    private void finalizarMinijuego() {
        if (bucleMinijuego != null) bucleMinijuego.stop();
        juegoCompletado = true;
        orbe.setVisible(false);
        btnIniciarJuego.setVisible(false);
        btnIniciarJuego.setManaged(false);

        if (capaAlertaRoja != null) capaAlertaRoja.setOpacity(0.0);

        lblMensaje.setText("Ante mi presencia permanece inmutable. \nParece que hay alguien detrás de ti. Quizá sepa algo...");

        panelCodigo.setVisible(true);
        panelCodigo.setManaged(true);
        txtNumero.requestFocus();
    }

    @FXML
    private void comprobar() {
        if (!juegoCompletado || txtNumero.isDisabled()) return;

        String entrada = txtNumero.getText().trim();
        if (NUMEROS_VALIDOS.contains(entrada)) {
            susto();
        } else {
            audioFallo.play();
            lblMensaje.setText("El espejo no te reconoce...");
            temblar(espejo);
            txtNumero.clear();
        }
    }

    public void susto() {
        txtNumero.setDisable(true);
        panelCodigo.setVisible(false);
        panelCodigo.setManaged(false);

        ambiente.pause();
        lblMensaje.setText("");

        // Retardo mínimo (200 ms) para un susto casi instantáneo al pulsar
        PauseTransition silencio = new PauseTransition(Duration.millis(200));
        silencio.setOnFinished(e -> {
            if (ojosEspejo != null) ojosEspejo.setVisible(false);
            imgSusto.setVisible(true);

            playerSusto.seek(Duration.ZERO);
            playerSusto.play();

            ScaleTransition impacto = new ScaleTransition(Duration.millis(70), imgSusto);
            impacto.setFromX(0.8);
            impacto.setFromY(0.8);
            impacto.setToX(1.35);
            impacto.setToY(1.35);

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

    public void reiniciar() {
        autoReinicio.stop();
        if (bucleMinijuego != null) bucleMinijuego.stop();
        if (playerSusto != null) playerSusto.stop();
        if (playerAcierto != null) playerAcierto.stop();

        juegoCompletado = false;
        fallosMinijuego = 0;
        orbe.setVisible(false);
        imgSusto.setVisible(false);
        lblLetra.setVisible(false);
        if (ojosEspejo != null) ojosEspejo.setVisible(true);

        resetearOjosYEfectos();

        btnIniciarJuego.setText("Iniciar invocación");
        btnIniciarJuego.setVisible(true);
        btnIniciarJuego.setManaged(true);

        panelCodigo.setVisible(false);
        panelCodigo.setManaged(false);

        txtNumero.clear();
        txtNumero.setDisable(false);

        lblMensaje.setText("El reflejo está inquieto. Pulsa el botón para invocar a la presencia.");
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
        panelCodigo.setVisible(false);
        panelCodigo.setManaged(false);

        // Se elimina el texto "Pasa algo..." del cristal al revelar la letra
        lblMensaje.setText("");

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

        btnIniciarJuego.setText("Reiniciar sala");
        btnIniciarJuego.setVisible(true);
        btnIniciarJuego.setManaged(true);

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
        if (bucleParpadeo != null) bucleParpadeo.stop();
        if (ambiente != null) ambiente.stop();
        if (playerSusto != null) playerSusto.stop();
        if (playerAcierto != null) playerAcierto.stop();
    }
}