package com.ecommerce.category.services;

import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> listCategory();
    CategoryResponse createCategory(CategoryRequest request, MultipartFile file);
    CategoryResponse findCategory(UUID id);
    List<CategoryResponse> findCategories(List<UUID> ids);
    Optional<CategoryResponse> updateCategory(UUID id, CategoryRequest category, MultipartFile file);
    void deleteCategory(UUID id);
}
