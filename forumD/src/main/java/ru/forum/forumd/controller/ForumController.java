package ru.forum.forumd.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ru.forum.forumd.service.ApiService;
import ru.forum.forumd.HelloApplication;
import ru.forum.forumd.dto.CategoryDTO;
import ru.forum.forumd.dto.ThreadDTO;

import java.util.List;

public class ForumController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private ListView<CategoryDTO> categoryListView;

    @FXML
    private TableView<ThreadDTO> threadsTableView;

    @FXML
    private TableColumn<ThreadDTO, Long> idColumn;

    @FXML
    private TableColumn<ThreadDTO, String> titleColumn;

    @FXML
    private TableColumn<ThreadDTO, String> authorColumn;

    @FXML
    private TableColumn<ThreadDTO, String> statusColumn;

    @FXML
    private TableColumn<ThreadDTO, Integer> viewsColumn;

    @FXML
    private TextField newThreadTitleArea;  // ИСПРАВЛЕНО: TextField вместо TextArea

    @FXML
    private TextArea newThreadContentArea;

    @FXML
    private ComboBox<CategoryDTO> categoryComboBox;

    @FXML
    private VBox createThreadPane;

    private ObservableList<CategoryDTO> categories = FXCollections.observableArrayList();
    private ObservableList<ThreadDTO> threads = FXCollections.observableArrayList();
    private CategoryDTO selectedCategory = null;

    @FXML
    public void initialize() {
        // Настраиваем таблицу тем
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorUsername"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        viewsColumn.setCellValueFactory(new PropertyValueFactory<>("viewsCount"));

        // Загружаем данные
        loadCategories();
        loadThreads();

        // Показываем имя пользователя и роль
        if (HelloApplication.currentUsername != null) {
            welcomeLabel.setText("Добро пожаловать, " + HelloApplication.currentUsername + "!");
            roleLabel.setText("Роль: " + HelloApplication.currentUserRole);
        }
    }

    private void loadCategories() {
        try {
            List<CategoryDTO> categoryList = ApiService.getCategories();
            categories.setAll(categoryList);
            categoryListView.setItems(categories);
            categoryComboBox.setItems(categories);

            // Обработчик выбора категории
            categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedCategory = newVal;
                    loadThreadsByCategory(newVal.getId());
                }
            });

        } catch (Exception e) {
            showError("Ошибка загрузки категорий: " + e.getMessage());
        }
    }

    private void loadThreads() {
        try {
            List<ThreadDTO> threadList = ApiService.getThreads();
            threads.setAll(threadList);
            threadsTableView.setItems(threads);
        } catch (Exception e) {
            showError("Ошибка загрузки тем: " + e.getMessage());
        }
    }

    private void loadThreadsByCategory(Long categoryId) {
        try {
            List<ThreadDTO> threadList = ApiService.getThreadsByCategory(categoryId);
            threads.setAll(threadList);
            threadsTableView.setItems(threads);
        } catch (Exception e) {
            showError("Ошибка загрузки тем: " + e.getMessage());
        }
    }

    @FXML
    private void onCreateThreadClick() {
        createThreadPane.setVisible(!createThreadPane.isVisible());
        createThreadPane.setManaged(createThreadPane.isVisible());
    }

    @FXML
    private void onSubmitNewThreadClick() {
        String title = newThreadTitleArea.getText();
        String content = newThreadContentArea.getText();
        CategoryDTO category = categoryComboBox.getValue();

        if (title.isEmpty() || content.isEmpty()) {
            showError("Заполните все поля!");
            return;
        }

        if (category == null) {
            showError("Выберите категорию!");
            return;
        }

        try {
            ApiService.createThread(title, content, category.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успешно");
            alert.setHeaderText(null);
            alert.setContentText("Тема создана!");
            alert.showAndWait();

            // Очищаем поля и закрываем форму
            newThreadTitleArea.clear();
            newThreadContentArea.clear();
            categoryComboBox.getSelectionModel().clearSelection();
            createThreadPane.setVisible(false);
            createThreadPane.setManaged(false);

            // Перезагружаем темы
            loadThreads();

        } catch (Exception e) {
            showError("Ошибка создания темы: " + e.getMessage());
        }
    }

    @FXML
    private void onRefreshClick() {
        loadCategories();
        loadThreads();
    }

    @FXML
    private void onLogoutClick() {
        // Очищаем данные пользователя
        HelloApplication.jwtToken = null;
        HelloApplication.currentUserRole = null;
        HelloApplication.currentUserId = null;
        HelloApplication.currentUsername = null;

        // Возвращаемся к экрану входа
        try {
            HelloApplication.setRootWithSize("login-view", 800, 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onThreadDoubleClick(javafx.scene.input.MouseEvent event) {
        // Проверяем, что выбрана тема и был двойной клик
        if (event.getClickCount() != 2) return;

        ThreadDTO selectedThread = threadsTableView.getSelectionModel().getSelectedItem();
        if (selectedThread == null) return;

        try {
            // 1. Создаём загрузчик
            FXMLLoader loader = new FXMLLoader(getClass().getResource("thread-detail-view.fxml"));

            // 2. СНАЧАЛА загружаем FXML
            Parent root = loader.load();

            // 3. ТОЛЬКО ПОСЛЕ load() получаем контроллер
            ThreadDetailController controller = loader.getController();

            // 4. Передаём данные
            controller.setThreadData(selectedThread);

            // 5. Создаём новую сцену и переключаемся
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Тема: " + selectedThread.getTitle());

        } catch (Exception e) {
            showError("Ошибка открытия темы: " + e.getMessage());
            e.printStackTrace();
        }
    }
}