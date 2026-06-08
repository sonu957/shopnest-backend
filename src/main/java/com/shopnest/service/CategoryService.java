package com.shopnest.service;

import com.shopnest.entity.Category;
import com.shopnest.exception.ResourceAlreadyExistsException;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Category already exists: " + name);
        }
        return categoryRepository.save(
            Category.builder().name(name).description(description).build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Category updateCategory(Long id, String name, String description) {
        Category category = getCategoryById(id);
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
