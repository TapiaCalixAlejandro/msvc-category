package com.ecommerce.category.mappers;

import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.models.entities.Category;

public class CategoryMapper {
    //  Convierte DTO -> Entidad
    public static Category toEntity(CategoryRequest dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus());
        category.setImage(dto.getImage());
        return category;
    }

    //  Convierte Entidad -> DTO
    public static CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setStatus(category.getStatus());
        response.setImage(category.getImage());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
