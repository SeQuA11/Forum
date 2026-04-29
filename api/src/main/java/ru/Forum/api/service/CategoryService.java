package ru.Forum.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Forum.api.dto.CategoryDTO;
import ru.Forum.api.entity.Category;
import ru.Forum.api.repository.CategoryRepository;
import ru.Forum.api.repository.ThreadRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ThreadRepository threadRepository;  // ДОБАВЛЕНО!

    public CategoryService(CategoryRepository categoryRepository, ThreadRepository threadRepository) {
        this.categoryRepository = categoryRepository;
        this.threadRepository = threadRepository;
    }


    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSlug(dto.getSlug());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена: " + id));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSlug(dto.getSlug());
        category.setSortOrder(dto.getSortOrder());
        category.setIsActive(dto.getIsActive());

        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Категория не найдена: " + id);
        }

        // Проверяем, есть ли темы в категории
        long threadCount = threadRepository.countByCategoryId(id);
        if (threadCount > 0) {
            throw new RuntimeException("Нельзя удалить категорию с темами. Сначала удалите " + threadCount + " тем.");
        }

        categoryRepository.deleteById(id);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setSlug(category.getSlug());
        dto.setSortOrder(category.getSortOrder());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
}