package com.ecommerce.category.services;

import com.ecommerce.category.mappers.CategoryMapper;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.models.entities.Category;
import com.ecommerce.category.repositories.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryFilterServiceImpl implements CategoryFilterService {
    private static final Logger log = LoggerFactory.getLogger(CategoryFilterServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryFilterServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CategoryResponse> filterCategory(
            String name,
            Boolean status,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable;
        boolean hasSort = sortBy != null && !sortBy.isBlank()
                && sortDir != null && !sortDir.isBlank();

        if (hasSort) {
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            pageable = PageRequest.of(page, size,sort);
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<Category> categories = categoryRepository.filterCategoryies(name, status, pageable);

        return categories.map(categoryMapper::toResponse);
    }
}
