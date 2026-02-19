package com.ecommerce.category.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileStorageService {
    String saveCategoryImage(MultipartFile file, UUID id, String oldImageName) throws IOException;
}
