package com.ecommerce.category.validations;

import com.ecommerce.category.models.dtos.CategoryRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class CategoryValidator {
    private final BusinessValidator businessValidator;
    private final ImageValidator imageValidator;

    public CategoryValidator(BusinessValidator businessValidator, ImageValidator imageValidator) {
        this.businessValidator = businessValidator;
        this.imageValidator = imageValidator;
    }

    /**
     * @param request
     * @param file
     */
    public void validateOnCreate(CategoryRequest request, MultipartFile file) {
        imageValidator.validateCreate(file);
        businessValidator.validateUniqueNameOnCreate(request.getName());
    }

    /**
     * @param id
     * @param request
     * @param file
     */
    public void validateOnUpdate(UUID id, CategoryRequest request, MultipartFile file) {
        businessValidator.validateUniqueNameOnUpdate(id, request.getName());
        imageValidator.validateUpdate(file);
    }
}
