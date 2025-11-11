package com.ecommerce.category.validations;

import com.ecommerce.category.models.dtos.CategoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class CategoryValidator {
    private final Validator validator;
    private final CategoryBusinessValidator businessValidator;
    private final ImageValidator imageValidator;

    public CategoryValidator(Validator validator, CategoryBusinessValidator businessValidator, ImageValidator imageValidator) {
        this.validator = validator;
        this.businessValidator = businessValidator;
        this.imageValidator = imageValidator;
    }

    /**
     * Valida una categoría antes de crearla o actualizarla.
     * @param request DTO con los datos de la categoría
     * @param file archivo de imagen (opcional en update)
     * @param isUpdate true si es actualización, false si es creación
     * @return Mapa de errores con el formato campo → mensaje
     */
    public Map<String, String> validate(CategoryRequest request, MultipartFile file, boolean isUpdate) {
        Map<String, String> errors = new HashMap<>();

        //  Validar anotaciones del DTO
        validator.validate(request).forEach(v ->
            errors.put(v.getPropertyPath().toString(), v.getMessage())
        );

        //  Validar nombre unico
        businessValidator.validateUniqueName(request.getName(), isUpdate).ifPresent(msg -> errors.put("name", msg));

        //  Validar imagen
        imageValidator.validate(file, isUpdate).ifPresent(msg -> errors.put("file", msg));

        return errors;
    }
}
