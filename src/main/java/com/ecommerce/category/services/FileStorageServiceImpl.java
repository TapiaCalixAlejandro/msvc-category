package com.ecommerce.category.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${app.upload.dir}")
    private String uploadDir;
    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Override
    public String saveCategoryImage(MultipartFile file, UUID id, String oldImageName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Directorio de carga creado: {}", uploadPath);
        }

        // Si existe una imagen anterior la borramos
        if (oldImageName != null && !oldImageName.isBlank()) {
            Path oldImagePath = uploadPath.resolve(Paths.get(oldImageName).getFileName().toString());
            Files.deleteIfExists(oldImagePath);
            log.info("Imagen anterior eliminada: {}", oldImagePath);
        }

        // Obtener extension segura
        String originalName = file.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // Generar nombre controlado
        String newFileName = "category_" + id + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return newFileName;
    }
}
