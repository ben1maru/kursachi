module biblioty.armbiblioty {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.commons.codec;


    opens biblioty.armbiblioty to javafx.fxml;
    exports biblioty.armbiblioty;
}