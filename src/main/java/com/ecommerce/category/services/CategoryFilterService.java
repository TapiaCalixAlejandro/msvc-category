package com.ecommerce.category.services;

import com.ecommerce.category.models.dtos.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryFilterService {
    Page<CategoryResponse> filterCategory(
            String name,
            Boolean status,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
