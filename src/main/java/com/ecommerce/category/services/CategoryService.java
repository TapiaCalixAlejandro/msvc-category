package com.ecommerce.category.services;

import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> listCategory();
    CategoryResponse createCategory(CategoryRequest request, MultipartFile file);
    Optional<CategoryResponse> findCategory(UUID id);
    List<CategoryResponse> findCategories(List<UUID> ids);
    Optional<CategoryResponse> updateCategory(UUID id, CategoryRequest category, MultipartFile file);
    void deleteCategory(UUID id);
    //Page<CategoryResponse> getAllPaged(Pageable pageable);
}
