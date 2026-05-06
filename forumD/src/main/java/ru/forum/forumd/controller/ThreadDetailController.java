package ru.forum.forumd.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import ru.forum.forumd.service.ApiService;
import ru.forum.forumd.HelloApplication;
import ru.forum.forumd.dto.PostDTO;
import ru.forum.forumd.dto.ThreadDTO;

import java.util.List;

public class ThreadDetailController {

    @FXML
    private Label threadTitleLabel;

    @FXML
    private Label threadAuthorLabel;

    @FXML
    private Label threadStatusLabel;

    @FXML
    private Label threadViewsLabel;

    @FXML
    private VBox postsContainer;

    @FXML
    private TextArea newPostArea;

    @FXML
    private Button submitPostButton;

    @FXML
    private VBox moderationPanel;

    @FXML
    private Label threadDescriptionLabel;

    @FXML
    private ComboBox<String> statusComboBox;

    private Long currentThreadId;
    private ThreadDTO currentThread;
    private ObservableList<PostDTO> posts = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("ACTIVE", "CLOSED", "PINNED", "HIDDEN");
    }

    public void setThreadData(ThreadDTO thread) {
        this.currentThread = thread;
        this.currentThreadId = thread.getId();

        threadTitleLabel.setText(thread.getTitle());

        // 🆕 Устанавливаем описание (контент) темы
        String content = thread.getContent();
        threadDescriptionLabel.setText(content != null && !content.isEmpty()
                ? content
                : "Описание отсутствует");

        threadAuthorLabel.setText("👤 Автор: " + thread.getAuthorUsername());
        threadStatusLabel.setText("🏷️ Статус: " + thread.getStatus());
        threadViewsLabel.setText("👁️ Просмотры: " + thread.getViewsCount());

        // Показываем панель модерации только для модераторов и админов
        String role = HelloApplication.currentUserRole;
        if ("ROLE_MODERATOR".equals(role) || "ROLE_ADMIN".equals(role)) {
            moderationPanel.setVisible(true);
            moderationPanel.setManaged(true);
            statusComboBox.setValue(thread.getStatus());
        } else {
            moderationPanel.setVisible(false);
            moderationPanel.setManaged(false);
        }

        loadPosts();
    }

    private void loadPosts() {
        try {
            List<PostDTO> postList = ApiService.getPostsByThread(currentThreadId);
            posts.setAll(postList);
            displayPosts();
        } catch (Exception e) {
            showError("Ошибка загрузки сообщений: " + e.getMessage());
        }
    }

    private void displayPosts() {
        postsContainer.getChildren().clear();

        for (PostDTO post : posts) {
            VBox postBox = createPostNode(post);
            postsContainer.getChildren().add(postBox);
        }
    }

    private VBox createPostNode(PostDTO post) {
        VBox postBox = new VBox(10);
        postBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");

        Label authorLabel = new Label(post.getAuthorUsername());
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateLabel = new Label(post.getCreatedAt());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        Text contentText = new Text(post.getContent());
        contentText.setWrappingWidth(800);

        TextFlow textFlow = new TextFlow(contentText);
        textFlow.setStyle("-fx-padding: 10;");

        postBox.getChildren().addAll(authorLabel, dateLabel, textFlow);

        return postBox;
    }

    @FXML
    private void onSubmitPostClick() {
        String content = newPostArea.getText();

        if (content.isEmpty()) {
            showError("Введите сообщение!");
            return;
        }

        try {
            ApiService.createPost(content, currentThreadId);
            newPostArea.clear();
            loadPosts();
        } catch (Exception e) {
            showError("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    @FXML
    private void onChangeStatusClick() {
        String newStatus = statusComboBox.getValue();

        try {
            ApiService.updateThreadStatus(currentThreadId, newStatus);
            currentThread.setStatus(newStatus);
            threadStatusLabel.setText("Статус: " + newStatus);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успешно");
            alert.setHeaderText(null);
            alert.setContentText("Статус темы изменён на " + newStatus);
            alert.showAndWait();

        } catch (Exception e) {
            showError("Ошибка изменения статуса: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClick() {
        try {
            System.out.println("Возврат к списку тем...");

            // 🔧 ИСПРАВЛЕНИЕ: используем HelloApplication.class для поиска FXML
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("forum-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());

            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Форум с модерацией");

        } catch (Exception e) {
            System.err.println("Ошибка при возврате: " + e.getMessage());
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
}