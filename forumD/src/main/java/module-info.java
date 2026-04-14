module ru.forum.forumd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.controlsfx.controls;

    opens ru.forum.forumd to javafx.fxml;
    exports ru.forum.forumd;
    exports ru.forum.forumd.dto;
    opens ru.forum.forumd.dto to javafx.fxml;
    exports ru.forum.forumd.controller;
    opens ru.forum.forumd.controller to javafx.fxml;
    exports ru.forum.forumd.request;
    opens ru.forum.forumd.request to javafx.fxml;
    exports ru.forum.forumd.service;
    opens ru.forum.forumd.service to javafx.fxml;
    exports ru.forum.forumd.response;
    opens ru.forum.forumd.response to javafx.fxml;
}