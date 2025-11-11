package com.ecommerce.category.services;

import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.models.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryResponse> listCategory();
    CategoryResponse createCategory(CategoryRequest request, MultipartFile file);
    Optional<CategoryResponse> findCategory(Long id);
    List<CategoryResponse> findCategories(List<Long> ids);
    Optional<CategoryResponse> updateCategory(Long id, CategoryRequest category, MultipartFile file);
    void deleteCategory(Long id);
    Page<CategoryResponse> getAllPaged(Pageable pageable);
}
