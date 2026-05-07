package ru.forum.forumd.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.forum.forumd.HelloApplication;
import ru.forum.forumd.controller.ThreadDetailController;
import ru.forum.forumd.dto.CategoryDTO;
import ru.forum.forumd.dto.ThreadDTO;
import ru.forum.forumd.service.ApiService;

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
    private TableColumn<ThreadDTO, String> titleColumn;

    @FXML
    private TableColumn<ThreadDTO, String> authorColumn;

    @FXML
    private TableColumn<ThreadDTO, String> statusColumn;

    @FXML
    private TableColumn<ThreadDTO, Integer> viewsColumn;

    @FXML
    private TableColumn<ThreadDTO, Void> pinColumn;

    @FXML
    private TableColumn<ThreadDTO, Void> actionsColumn;

    @FXML
    private TextField newThreadTitleArea;

    @FXML
    private TextArea newThreadContentArea;

    @FXML
    private ComboBox<CategoryDTO> categoryComboBox;

    @FXML
    private VBox createThreadPane;

    @FXML
    private Label statusLabel;

    @FXML
    private Label threadCountLabel;

    private ObservableList<CategoryDTO> categories = FXCollections.observableArrayList();
    private ObservableList<ThreadDTO> threads = FXCollections.observableArrayList();
    private CategoryDTO selectedCategory = null;

    @FXML
    public void initialize() {
        // Настройка таблицы
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorUsername"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        viewsColumn.setCellValueFactory(new PropertyValueFactory<>("viewsCount"));

        // Настройка колонки закрепления (иконка)
        pinColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ThreadDTO thread = getTableView().getItems().get(getIndex());
                    Label pinLabel = new Label(thread.isPinned() ? "📌" : "");
                    pinLabel.setStyle("-fx-font-size: 16px;");
                    setGraphic(pinLabel);
                }
            }
        });

        // Настройка колонки действий (кнопка удаления)
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ThreadDTO thread = getTableView().getItems().get(getIndex());
                    Button deleteBtn = new Button("❌");
                    deleteBtn.getStyleClass().add("danger-button");
                    deleteBtn.setTooltip(new Tooltip("Удалить тему"));
                    deleteBtn.setOnAction(e -> deleteThreadById(thread.getId()));
                    setGraphic(deleteBtn);
                }
            }
        });

        // Контекстное меню для изменения статуса (правый клик)
        threadsTableView.setContextMenu(createStatusContextMenu());

        loadCategories();
        loadThreads();

        if (HelloApplication.currentUsername != null) {
            welcomeLabel.setText("Добро пожаловать, " + HelloApplication.currentUsername + "!");
            roleLabel.setText("Роль: " + HelloApplication.currentUserRole);
        }

        // Обработчик выбора категории
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCategory = newVal;
                loadThreadsByCategory(newVal.getId());
            }
        });
    }

    // Создание контекстного меню для изменения статуса темы и закрепления
    private ContextMenu createStatusContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem activeItem = new MenuItem("ACTIVE (Активна)");
        activeItem.setOnAction(e -> changeSelectedThreadStatus("ACTIVE"));
        
        MenuItem pinnedItem = new MenuItem("PINNED (Закреплена)");
        pinnedItem.setOnAction(e -> changeSelectedThreadStatus("PINNED"));
        
        MenuItem closedItem = new MenuItem("CLOSED (Закрыта)");
        closedItem.setOnAction(e -> changeSelectedThreadStatus("CLOSED"));
        
        MenuItem hiddenItem = new MenuItem("HIDDEN (Скрыта)");
        hiddenItem.setOnAction(e -> changeSelectedThreadStatus("HIDDEN"));
        
        // Разделитель
        SeparatorMenuItem separator = new SeparatorMenuItem();
        
        // Пункт для закрепления/открепления темы
        MenuItem togglePinItem = new MenuItem("Закрепить тему");
        togglePinItem.setOnAction(e -> togglePinSelectedThread());
        
        contextMenu.getItems().addAll(activeItem, pinnedItem, closedItem, hiddenItem, separator, togglePinItem);
        return contextMenu;
    }

    private void togglePinSelectedThread() {
        ThreadDTO selectedThread = threadsTableView.getSelectionModel().getSelectedItem();
        if (selectedThread == null) {
            showError("Выберите тему!");
            return;
        }
        try {
            boolean isPinned = selectedThread.isPinned();
            String newStatus = isPinned ? "ACTIVE" : "PINNED";
            ApiService.updateThreadStatus(selectedThread.getId(), newStatus);
            loadThreads();
            showSuccess(isPinned ? "Тема откреплена!" : "Тема закреплена!");
        } catch (Exception e) {
            showError("Ошибка изменения закрепления: " + e.getMessage());
        }
    }

    private void changeSelectedThreadStatus(String status) {
        ThreadDTO selectedThread = threadsTableView.getSelectionModel().getSelectedItem();
        if (selectedThread == null) {
            showError("Выберите тему!");
            return;
        }
        try {
            ApiService.updateThreadStatus(selectedThread.getId(), status);
            loadThreads();
            showSuccess("Статус темы изменён на " + status);
        } catch (Exception e) {
            showError("Ошибка изменения статуса: " + e.getMessage());
        }
    }

    private void deleteThreadById(Long threadId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText("Удаление темы");
        confirmAlert.setContentText("Вы уверены, что хотите удалить эту тему?");

        var result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ApiService.deleteThread(threadId);
                loadThreads();
                showSuccess("Тема удалена!");
            } catch (Exception e) {
                showError("Ошибка удаления темы: " + e.getMessage());
            }
        }
    }

    private void loadCategories() {
        try {
            List<CategoryDTO> categoryList = ApiService.getCategories();
            categories.setAll(categoryList);
            categoryListView.setItems(categories);
            categoryComboBox.setItems(categories);
        } catch (Exception e) {
            showError("Ошибка загрузки категорий: " + e.getMessage());
        }
    }

    private void loadThreads() {
        try {
            List<ThreadDTO> threadList = ApiService.getThreads();
            threads.setAll(threadList);
            threadsTableView.setItems(threads);
            updateThreadCount();
        } catch (Exception e) {
            showError("Ошибка загрузки тем: " + e.getMessage());
        }
    }

    private void loadThreadsByCategory(Long categoryId) {
        try {
            List<ThreadDTO> threadList = ApiService.getThreadsByCategory(categoryId);
            threads.setAll(threadList);
            threadsTableView.setItems(threads);
            updateThreadCount();
        } catch (Exception e) {
            showError("Ошибка загрузки тем: " + e.getMessage());
        }
    }

    private void updateThreadCount() {
        threadCountLabel.setText("Тем: " + threads.size());
    }

    // ========== КНОПКИ УПРАВЛЕНИЯ ==========

    @FXML
    private void onCreateThreadClick() {
        createThreadPane.setVisible(!createThreadPane.isVisible());
        createThreadPane.setManaged(createThreadPane.isVisible());
    }

    @FXML
    private void onCloseCreateThreadPane() {
        createThreadPane.setVisible(false);
        createThreadPane.setManaged(false);
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

            newThreadTitleArea.clear();
            newThreadContentArea.clear();
            categoryComboBox.getSelectionModel().clearSelection();
            createThreadPane.setVisible(false);
            createThreadPane.setManaged(false);

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
    private void onAllCategoriesClick() {
        selectedCategory = null;
        categoryListView.getSelectionModel().clearSelection();
        loadThreads();
    }

    @FXML
    private void onLogoutClick() {
        System.out.println("Выход из системы...");

        // Очищаем данные пользователя
        HelloApplication.jwtToken = null;
        HelloApplication.currentUserRole = null;
        HelloApplication.currentUserId = null;
        HelloApplication.currentUsername = null;

        try {
            // Используем правильный путь к ресурсу
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("login-view.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("styles.css").toExternalForm()
            );

            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Форум с модерацией - Вход");

            System.out.println("Выход выполнен успешно");

        } catch (Exception e) {
            System.err.println("Ошибка при выходе: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Не удалось выйти: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ========== ОТКРЫТИЕ ТЕМЫ ==========

    @FXML
    private void onThreadDoubleClick(MouseEvent event) {
        if (event.getClickCount() != 2) return;

        ThreadDTO selectedThread = threadsTableView.getSelectionModel().getSelectedItem();
        if (selectedThread == null) return;

        try {
            // 🔧 ИСПРАВЛЕНИЕ: используем HelloApplication.class, так как FXML лежат в его пакете
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("thread-detail-view.fxml"));
            Parent root = loader.load();

            ThreadDetailController controller = loader.getController();
            controller.setThreadData(selectedThread);

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());

            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Тема: " + selectedThread.getTitle());

        } catch (Exception e) {
            showError("Ошибка открытия темы: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
