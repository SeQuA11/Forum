module ru.forum.forumd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.controlsfx.controls;

    opens ru.forum.forumd to javafx.fxml;
    exports ru.forum.forumd;
}