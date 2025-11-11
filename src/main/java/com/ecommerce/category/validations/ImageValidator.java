package com.ecommerce.category.validations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Component
public class ImageValidator {
    private static final Logger log = LoggerFactory.getLogger(ImageValidator.class);

    public Optional<String> validate(MultipartFile file, boolean isUpdate) {
        if (!isUpdate && (file == null || file.isEmpty())) {
            log.error("La imagen es obligatoria al crear una categoría.");
            return Optional.of("La imagen es obligatoria al crear una categoría.");
        }

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg") || contentType.equalsIgnoreCase("image/png"))) {
                log.error("Solo se permiten imagenes JPEG, JPG o PNG.");
                return Optional.of("Solo se permiten imagenes JPEG, JPG o PNG.");
            }

            if (file.getSize() > 2 * 1024 * 1024) {
                log.error("La imagen no debe superar los 2 MB.");
                return Optional.of("La imagen no debe superar los 2 MB.");
            }
        }
        return Optional.empty();
    }
}
