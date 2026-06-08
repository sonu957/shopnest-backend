package com.shopnest.controller;

import com.shopnest.entity.Category;
import com.shopnest.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(Map.of("success", true, "data", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("success", true, "data", categoryService.getCategoryById(id)));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(request.getName(), request.getDescription());
        return ResponseEntity.ok(Map.of("success", true, "message", "Category created", "data", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.updateCategory(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(Map.of("success", true, "message", "Category updated", "data", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Category deleted"));
    }

    @Data
    static class CategoryRequest {
        @NotBlank private String name;
        private String description;
    }
}
