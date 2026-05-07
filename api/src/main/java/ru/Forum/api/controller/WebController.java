package ru.Forum.api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.*;
import ru.Forum.api.service.CategoryService;
import ru.Forum.api.service.PostService;
import ru.Forum.api.service.ThreadService;
import ru.Forum.api.service.UserService;

import java.util.List;

@Controller
public class WebController {

    private final CategoryService categoryService;
    private final ThreadService threadService;
    private final PostService postService;
    private final UserService userService;

    public WebController(CategoryService categoryService, ThreadService threadService, 
                         PostService postService, UserService userService) {
        this.categoryService = categoryService;
        this.threadService = threadService;
        this.postService = postService;
        this.userService = userService;
    }


    // Страница входа
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Страница регистрации
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Главная страница форума
    @GetMapping("/forum")
    public String forumPage(@RequestParam(required = false) Long categoryId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Получаем данные
        List<CategoryDTO> categories = categoryService.getAllActiveCategories();
        List<ThreadResponseDTO> threads = categoryId != null ?
                threadService.getThreadsByCategory(categoryId, 0, 20, username).getContent() :
                threadService.getActiveThreads(0, 20, username).getContent();

        // Определяем роль
        boolean isAdminOrModerator = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN") ||
                        r.getAuthority().equals("ROLE_MODERATOR"));

        model.addAttribute("username", username);
        model.addAttribute("role", auth.getAuthorities());
        model.addAttribute("categories", categories);
        model.addAttribute("threads", threads);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("isAdminOrModerator", isAdminOrModerator);

        return "forum";
    }

    // Страница темы
    @GetMapping("/thread/{id}")
    public String threadPage(@PathVariable Long id, Model model) {
        // Здесь логика загрузки темы и постов
        return "thread";
    }
}