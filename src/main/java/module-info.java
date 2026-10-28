module es.medac.espejo.tunel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens es.medac.espejo.tunel to javafx.fxml;
    exports es.medac.espejo.tunel;
}