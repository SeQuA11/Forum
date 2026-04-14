package ru.forum.forumd.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.forum.forumd.service.ApiService;
import ru.forum.forumd.response.AuthResponse;
import ru.forum.forumd.HelloApplication;

public class LoginController {

    @FXML
    private VBox loginForm;

    @FXML
    private VBox registerForm;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField regUsernameField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label registerErrorLabel;

    @FXML
    public void initialize() {
        if (registerForm != null) {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
        }
        errorLabel.setText("");
    }

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните все поля!");
            return;
        }

        errorLabel.setText("Выполняется вход...");

        try {
            System.out.println("Попытка входа: " + username);
            AuthResponse response = ApiService.login(username, password);

            System.out.println("Вход успешен! Токен: " +
                    (response.getToken() != null ? response.getToken().substring(0, 20) + "..." : "null"));

            HelloApplication.jwtToken = response.getToken();
            HelloApplication.currentUserRole = response.getRole();
            HelloApplication.currentUserId = response.getUserId();
            HelloApplication.currentUsername = response.getUsername();

            errorLabel.setText("");

            HelloApplication.setRootWithSize("forum-view", 1200, 800);

        } catch (Exception e) {
            System.err.println("Ошибка входа: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void onRegisterButtonClick() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerForm.setVisible(true);
        registerForm.setManaged(true);
        errorLabel.setText("");
    }

    @FXML
    private void onRegisterSubmitClick() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            registerErrorLabel.setText("Заполните все поля!");
            return;
        }

        if (password.length() < 8) {
            registerErrorLabel.setText("Пароль должен быть не менее 8 символов!");
            return;
        }

        try {
            ApiService.register(username, password);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Регистрация успешна");
            alert.setHeaderText(null);
            alert.setContentText("Пользователь зарегистрирован! Теперь вы можете войти.");
            alert.showAndWait();

            registerForm.setVisible(false);
            registerForm.setManaged(false);
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            registerErrorLabel.setText("");
            usernameField.setText(username);

        } catch (Exception e) {
            registerErrorLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void onBackToLoginClick() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        registerErrorLabel.setText("");
    }
}