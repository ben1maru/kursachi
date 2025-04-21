module medics.medrovarsarm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.commons.codec;


    opens medics.medrovarsarm to javafx.fxml;
    exports medics.medrovarsarm;
}