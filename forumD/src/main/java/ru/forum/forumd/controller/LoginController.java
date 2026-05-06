package ru.forum.forumd.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

            System.out.println("✓ Вход успешен!");
            System.out.println("  Токен: " + (response.getToken() != null ? "получен" : "нет"));
            System.out.println("  Роль: " + response.getRole());
            System.out.println("  ID: " + response.getUserId());
            System.out.println("  Username: " + response.getUsername());

            // Сохраняем данные
            HelloApplication.jwtToken = response.getToken();
            HelloApplication.currentUserRole = response.getRole();
            HelloApplication.currentUserId = response.getUserId();
            HelloApplication.currentUsername = response.getUsername();

            errorLabel.setText("");

            // Принудительно создаем новую сцену
            javafx.application.Platform.runLater(() -> {
                try {
                    System.out.println("Загрузка forum-view.fxml...");
                    FXMLLoader loader = new FXMLLoader(
                            HelloApplication.class.getResource("forum-view.fxml")
                    );
                    Parent root = loader.load();

                    System.out.println("FXML загружен, создаем сцену...");
                    Scene scene = new Scene(root, 1200, 800);
                    scene.getStylesheets().add(
                            HelloApplication.class.getResource("styles.css").toExternalForm()
                    );

                    var stage = HelloApplication.getPrimaryStage();
                    stage.setScene(scene);
                    stage.setTitle("Форум с модерацией");
                    stage.show();

                    System.out.println("✓ Переход на форум выполнен!");

                } catch (Exception e) {
                    System.err.println("✗ Ошибка при загрузке форума: " + e.getMessage());
                    e.printStackTrace();
                    errorLabel.setText("Ошибка загрузки форума: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("✗ Ошибка входа: " + e.getMessage());
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

        registerErrorLabel.setText("Регистрация...");

        try {
            System.out.println("Регистрация: " + username);
            ApiService.register(username, password);
            System.out.println("✓ Регистрация успешна!");

            // Автоматический вход
            System.out.println("Автоматический вход...");
            AuthResponse response = ApiService.login(username, password);

            HelloApplication.jwtToken = response.getToken();
            HelloApplication.currentUserRole = response.getRole();
            HelloApplication.currentUserId = response.getUserId();
            HelloApplication.currentUsername = response.getUsername();

            // Переход на форум
            javafx.application.Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            HelloApplication.class.getResource("forum-view.fxml")
                    );
                    Parent root = loader.load();

                    Scene scene = new Scene(root, 1200, 800);
                    scene.getStylesheets().add(
                            HelloApplication.class.getResource("styles.css").toExternalForm()
                    );

                    var stage = HelloApplication.getPrimaryStage();
                    stage.setScene(scene);
                    stage.setTitle("Форум с модерацией");
                    stage.show();

                    System.out.println("✓ Переход на форум после регистрации выполнен!");

                } catch (Exception e) {
                    System.err.println("✗ Ошибка: " + e.getMessage());
                    e.printStackTrace();
                }
            });

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