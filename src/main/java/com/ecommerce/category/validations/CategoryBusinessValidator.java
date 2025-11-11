package com.ecommerce.category.validations;

import com.ecommerce.category.repositories.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CategoryBusinessValidator {
    private static final Logger log = LoggerFactory.getLogger(CategoryBusinessValidator.class);

    private final CategoryRepository categoryRepository;

    public CategoryBusinessValidator(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Optional<String> validateUniqueName(String name, boolean isUpdate) {
        if (!isUpdate && categoryRepository.existsByName(name)) {
            log.error("Ya existe una categoria con el nombre: {}", name);
            return Optional.of("Ya existe una categoria con el nombre: " + name);
        }
        return Optional.empty();
    }
}
