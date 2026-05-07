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
    private Button deleteCategoryButton;

    @FXML
    private Button deleteThreadButton;

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
    private VBox adminPanel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label threadCountLabel;

    private ObservableList<CategoryDTO> categories = FXCollections.observableArrayList();
    private ObservableList<ThreadDTO> threads = FXCollections.observableArrayList();
    private CategoryDTO selectedCategory = null;

    @FXML private Button createCategoryButton;

    @FXML
    public void initialize() {
        // Настройка таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
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
                    Label pinLabel = new Label("📌");
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

            // Проверяем права
            boolean isModOrAdmin = "ROLE_MODERATOR".equals(HelloApplication.currentUserRole) ||
                    "ROLE_ADMIN".equals(HelloApplication.currentUserRole);

            System.out.println("Проверка прав: isModOrAdmin = " + isModOrAdmin);

            // Скрываем кнопки управления категориями для обычных пользователей
            if (createCategoryButton != null) {
                createCategoryButton.setVisible(isModOrAdmin);
                createCategoryButton.setManaged(isModOrAdmin);
            }
            if (deleteCategoryButton != null) {
                deleteCategoryButton.setVisible(isModOrAdmin);
                deleteCategoryButton.setManaged(isModOrAdmin);
            }

            // ПОКАЗЫВАЕМ кнопку удаления тем для модераторов/админов
            if (deleteThreadButton != null) {
                deleteThreadButton.setVisible(isModOrAdmin);
                deleteThreadButton.setManaged(isModOrAdmin);
                System.out.println("Кнопка удаления тем: visible = " + isModOrAdmin);
            }

            // Показываем админ-панель
            if (adminPanel != null) {
                adminPanel.setVisible(isModOrAdmin);
                adminPanel.setManaged(isModOrAdmin);
            }


        }

        // Обработчик выбора категории
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCategory = newVal;
                loadThreadsByCategory(newVal.getId());
            }
        });
    }

    // Создание контекстного меню для изменения статуса темы
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
        
        contextMenu.getItems().addAll(activeItem, pinnedItem, closedItem, hiddenItem);
        return contextMenu;
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

    // ========== УДАЛЕНИЕ ==========

    @FXML
    private void onDeleteThreadClick() {
        ThreadDTO selectedThread = threadsTableView.getSelectionModel().getSelectedItem();

        if (selectedThread == null) {
            showError("Выберите тему для удаления!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText("Удаление темы");
        confirmAlert.setContentText("Вы уверены, что хотите удалить тему \"" + selectedThread.getTitle() + "\"?");

        var result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ApiService.deleteThread(selectedThread.getId());
                loadThreads();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успешно");
                alert.setHeaderText(null);
                alert.setContentText("Тема удалена!");
                alert.showAndWait();

            } catch (Exception e) {
                showError("Ошибка удаления темы: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onDeleteCategoryClick() {
        CategoryDTO selectedCategory = categoryListView.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showError("Выберите категорию для удаления!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText("Удаление категории");
        confirmAlert.setContentText("Вы уверены, что хотите удалить категорию \"" + selectedCategory.getName() + "\"?");

        var result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ApiService.deleteCategory(selectedCategory.getId());
                loadCategories();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успешно");
                alert.setHeaderText(null);
                alert.setContentText("Категория удалена!");
                alert.showAndWait();

            } catch (Exception e) {
                showError("Ошибка удаления категории: " + e.getMessage());
            }
        }
    }

    // ========== СОЗДАНИЕ КАТЕГОРИИ ==========

    @FXML
    private void onCreateCategoryClick() {
        Dialog<CategoryDTO> dialog = new Dialog<>();
        dialog.setTitle("Создание категории");
        dialog.setHeaderText("Введите данные новой категории");

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Название");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Описание");
        TextField slugField = new TextField();
        slugField.setPromptText("URL (slug)");

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("URL (slug):"), 0, 2);
        grid.add(slugField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                CategoryDTO dto = new CategoryDTO();
                dto.setName(nameField.getText());
                dto.setDescription(descriptionField.getText());
                dto.setSlug(slugField.getText());
                dto.setSortOrder(0);
                dto.setIsActive(true);
                return dto;
            }
            return null;
        });

        var result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                ApiService.createCategory(dto.getName(), dto.getDescription(),
                        dto.getSlug(), dto.getSortOrder(), dto.getIsActive());
                loadCategories();
            } catch (Exception e) {
                showError("Ошибка создания категории: " + e.getMessage());
            }
        });
    }

    // ========== МОДЕРАЦИЯ ТЕМ ==========

    @FXML
    private void onPinThreadClick() {
        changeThreadStatus("PINNED");
    }

    @FXML
    private void onCloseThreadClick() {
        changeThreadStatus("CLOSED");
    }

    @FXML
    private void onHideThreadClick() {
        changeThreadStatus("HIDDEN");
    }

    private void changeThreadStatus(String status) {
        ThreadDTO selectedThread = threadsTableView.getSelectionModel().getSelectedItem();

        if (selectedThread == null) {
            showError("Выберите тему!");
            return;
        }

        try {
            ApiService.updateThreadStatus(selectedThread.getId(), status);
            loadThreads();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успешно");
            alert.setHeaderText(null);
            alert.setContentText("Статус темы изменён на " + status);
            alert.showAndWait();

        } catch (Exception e) {
            showError("Ошибка изменения статуса: " + e.getMessage());
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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
