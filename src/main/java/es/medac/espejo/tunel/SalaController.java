package es.medac.espejo.tunel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class SalaController {

    @FXML
    private StackPane panelPrincipal;

    @FXML
    private Label lblTemporizador;

    @FXML
    private Label lblPista;

    @FXML
    private TextField txtCodigo;

    @FXML
    private Button btnComprobar;

    @FXML
    private Label lblMensaje;

    @FXML
    private ImageView imgJumpscare;

    @FXML
    public void initialize() {
        lblMensaje.setText("");
    }

    @FXML
    private void comprobarSolucion(ActionEvent event) {
        String texto = txtCodigo.getText().trim();
        if (texto.isEmpty()) {
            lblMensaje.setText("El cristal no responde al silencio...");
        } else {
            lblMensaje.setText("Comprobando: " + texto);
        }
    }
}