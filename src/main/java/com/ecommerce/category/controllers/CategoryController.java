package com.ecommerce.category.controllers;

import com.ecommerce.category.exceptions.ResourceNotFoundException;
import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.services.CategoryService;
import com.ecommerce.category.validations.CategoryValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;
    private final CategoryValidator categoryValidator;

    public CategoryController(CategoryService categoryService, ObjectMapper objectMapper, CategoryValidator categoryValidator) {
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;
        this.categoryValidator = categoryValidator;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.listCategory());
    }
    //  Prueba
    @PostMapping("/bulk")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(categoryService.findCategories(ids));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@RequestPart String category, @RequestPart(required = false) MultipartFile file) throws JsonProcessingException {
        //  Convertimos JSON a DTO
        CategoryRequest request = objectMapper.readValue(category, CategoryRequest.class);
        //  Centralizar la validación
        Map<String, String> errors = categoryValidator.validate(request, file, false);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        //  Usamos el servicio que retorna CategoryResponse directamente
        CategoryResponse response = categoryService.createCategory(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> detail(@PathVariable Long id) {
        return categoryService.findCategory(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestPart @Valid String category, @RequestPart(required = false) MultipartFile file) throws JsonProcessingException {
        //  Convertimos el JSON recibido a CategoryRequest
        CategoryRequest request = objectMapper.readValue(category, CategoryRequest.class);
        //  Centralizar validación
        Map<String, String> errors = categoryValidator.validate(request, file, true);
        if (!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        //  Llamamos al servicio y retornamos el ResponseEntity con el CategoryResponse
        CategoryResponse response = categoryService.updateCategory(id, request, file)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría con ID " + id + " no existe."));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<CategoryResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryResponse> categories = categoryService.getAllPaged(pageable);
        return ResponseEntity.ok(categories);
    }
}
