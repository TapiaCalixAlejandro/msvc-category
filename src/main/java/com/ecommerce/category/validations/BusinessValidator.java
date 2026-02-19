package com.ecommerce.category.validations;

import com.ecommerce.category.exceptions.BusinessException;
import com.ecommerce.category.exceptions.ResourceNotFoundException;
import com.ecommerce.category.repositories.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BusinessValidator {
    private static final Logger log = LoggerFactory.getLogger(BusinessValidator.class);
    private final CategoryRepository categoryRepository;

    public BusinessValidator(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void validateUniqueNameOnCreate(String name) {
        if (categoryRepository.existsByName(name)) {
            log.error("Ya existe una categoria con el nombre: {}", name);
            throw new BusinessException("El nombre de la categoria '" + name + "' ya está en uso por otro recurso.");
        }
    }

    public void validateUniqueNameOnUpdate(UUID id, String name) {
        if (categoryRepository.existsByNameAndIdNot(id, name)) {
            throw new BusinessException("El nombre de la categoría '" + name + "' ya está en uso por otra categoría.");
        }
    }
}
