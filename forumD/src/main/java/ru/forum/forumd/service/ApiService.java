package ru.forum.forumd.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.forum.forumd.response.AuthResponse;
import ru.forum.forumd.HelloApplication;
import ru.forum.forumd.dto.CategoryDTO;
import ru.forum.forumd.dto.PostDTO;
import ru.forum.forumd.dto.ThreadDTO;
import ru.forum.forumd.request.AuthRequest;
import ru.forum.forumd.request.PostCreateRequest;
import ru.forum.forumd.request.ThreadCreateRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiService {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AuthResponse login(String username, String password) throws Exception {
        AuthRequest request = new AuthRequest(username, password);
        String json = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), AuthResponse.class);
        } else {
            throw new Exception("Ошибка входа: " + response.body());
        }
    }

    public static void register(String username, String password) throws Exception {
        AuthRequest request = new AuthRequest(username, password);
        String json = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new Exception("Ошибка регистрации: " + response.body());
        }
    }

    public static List<CategoryDTO> getCategories() throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/categories"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Настраиваем ObjectMapper игнорировать неизвестные свойства
            objectMapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );

            return objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CategoryDTO.class));
        } else {
            throw new Exception("Ошибка получения категорий: " + response.body());
        }
    }

    public static List<ThreadDTO> getThreads() throws Exception {
        return getThreadsPage(0, 20);
    }

    public static List<ThreadDTO> getThreadsPage(int page, int size) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads?page=" + page + "&size=" + size))
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Парсим Page ответ
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode contentNode = rootNode.path("content");

            objectMapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );

            return objectMapper.readValue(contentNode.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ThreadDTO.class));
        } else {
            throw new Exception("Ошибка получения тем: " + response.body());
        }
    }

    public static List<ThreadDTO> getThreadsByCategory(Long categoryId) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads/category/" + categoryId + "?page=0&size=20"))
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Парсим Page ответ
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode contentNode = rootNode.path("content");

            objectMapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );

            return objectMapper.readValue(contentNode.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ThreadDTO.class));
        } else {
            throw new Exception("Ошибка получения тем: " + response.body());
        }
    }

    public static ThreadDTO createThread(String title, String content, Long categoryId) throws Exception {
        ThreadCreateRequest request = new ThreadCreateRequest(title, content, categoryId);
        String json = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ThreadDTO.class);
        } else {
            throw new Exception("Ошибка создания темы: " + response.body());
        }
    }

    public static List<PostDTO> getPostsByThread(Long threadId) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads/" + threadId + "/posts"))
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode contentNode = rootNode.path("content");

            objectMapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );

            return objectMapper.readValue(contentNode.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PostDTO.class));
        } else {
            throw new Exception("Ошибка получения сообщений: " + response.body());
        }
    }

    public static PostDTO createPost(String content, Long threadId) throws Exception {
        PostCreateRequest request = new PostCreateRequest(content, threadId);
        String json = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads/" + threadId + "/posts"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), PostDTO.class);
        } else {
            throw new Exception("Ошибка создания сообщения: " + response.body());
        }
    }

    // ========== MODERATION ==========

    public static ThreadDTO updateThreadStatus(Long threadId, String status) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads/" + threadId + "/status?status=" + status))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ThreadDTO.class);
        } else {
            throw new Exception("Ошибка изменения статуса: " + response.body());
        }
    }

    public static void deleteThread(Long threadId) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(HelloApplication.API_BASE_URL + "/threads/" + threadId))
                .DELETE()
                .header("Authorization", "Bearer " + HelloApplication.jwtToken)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new Exception("Ошибка удаления темы: " + response.body());
        }
    }
}