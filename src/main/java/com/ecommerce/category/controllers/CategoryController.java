package com.ecommerce.category.controllers;

import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.services.CategoryFilterService;
import com.ecommerce.category.services.CategoryService;
import com.ecommerce.category.validations.CategoryValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryFilterService categoryFilterService;
    private final CategoryValidator categoryValidator;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;

    public CategoryController(
            CategoryFilterService categoryFilterService,
            CategoryService categoryService,
            ObjectMapper objectMapper,
            CategoryValidator categoryValidator
    ) {
        this.categoryFilterService = categoryFilterService;
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;
        this.categoryValidator = categoryValidator;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.listCategory());
    }
    //  Prueba
    @PostMapping("/bulk")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByIds(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(categoryService.findCategories(ids));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@Valid @RequestPart CategoryRequest request, @RequestPart(required = false) MultipartFile file) {
        //  Usamos el servicio que retorna CategoryResponse directamente
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> detail(@PathVariable UUID id) {
        //return categoryService.findCategory(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        return ResponseEntity.ok(categoryService.findCategory(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestPart CategoryRequest request, @RequestPart(required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.updateCategory(id, request, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<CategoryResponse>> getAllPaged(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "") String sortDir
    ) {
        Page<CategoryResponse> categories = categoryFilterService.filterCategory(name, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(categories);
    }
}
