module es.medac.espejo.tunel {
    requires javafx.controls;
    requires javafx.fxml;


    opens es.medac.espejo.tunel to javafx.fxml;
    exports es.medac.espejo.tunel;
}