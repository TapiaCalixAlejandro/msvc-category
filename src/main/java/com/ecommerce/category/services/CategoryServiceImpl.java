package com.ecommerce.category.services;

import com.ecommerce.category.exceptions.*;
import com.ecommerce.category.mappers.CategoryMapper;
import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.models.entities.Category;
import com.ecommerce.category.repositories.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    @Value("${app.upload.dir}")
    private String uploadDir;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    //  Listar todas las categorias
    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> listCategory() {
        try {
            log.info("Consultando lista de categorias.");
            List<Category> categories = categoryRepository.findAll();

            if (categories.isEmpty()) {
                log.warn("No se encontraron categorias registradas en la base de datos.");
                //  No se lanza exception - se devulve lista vacia para que el controlador lo maneje
                return Collections.emptyList();
            }
             log.info("Se encontraron {} categorias registradas.", categories.size());

            return categories.stream().map(categoryMapper::toResponse).toList();
        } catch (Exception e) {
            log.error("Error al obtener la lista de categorias: {}", e.getMessage(), e);
            throw new DatabaseException("Error al consultar categorias.", e);
        }
    }

    //  Crear categoría
    @Transactional
    @Override
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile file) {
        log.info("Creando nueva categoria: {}", request.getName());
        //  Validar nombre unico
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + request.getName());
        }
        //Category savedCategory = null;
        try {
            //  Convertir DTO en Entidad
            Category category = categoryMapper.toEntity(request);

            //  Creamos primero sin imagen (para obtener el ID)
            Category savedCategory = categoryRepository.save(category);
            log.info("Categoría: {} creada correctamente.", request.getName());

            //  Si hay imagen la guardamos en el sistema de archivos
            if (file != null && !file.isEmpty()) {
                String fileName = handleImageUpload(file, null, savedCategory.getId());
                category.setImage(fileName);
                categoryRepository.save(savedCategory);
                log.info("Imagen guardada correctamente para la categoría: {}", category.getName());
            }

            //  Devolvemos el DTO de respuesta
            return categoryMapper.toResponse(savedCategory);
        } catch (IOException e) {
            log.error("Error al guardar la imagen de la categoría '{}': {}", request.getName(), e.getMessage());
            throw new FileStorageException("Error al subir archivo: " + e.getMessage(), e);
        }
    }

    //  Buscar categoria por ID
    @Transactional(readOnly = true)
    @Override
    public Optional<CategoryResponse> findCategory(UUID id) {
        log.info("Obteniendo detalle de categoría con ID: {}", id);
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("No se encontró la categoría con ID: {}", id);
            return new ResourceNotFoundException("La categoria con el ID: " + id + " no existe.");
        });
        return Optional.of(categoryMapper.toResponse(category));
    }

    //  Buscar varias categorias por ID
    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> findCategories(List<UUID> ids) {
        return categoryRepository.findAllById(ids)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    //  Actualizar categoria
    @Transactional
    @Override
    public Optional<CategoryResponse> updateCategory(UUID id, CategoryRequest request, MultipartFile file) {
        log.info("Inicializando actualización de categoría con ID: {}", id);
        Category exists = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró categoría con ID: {}", id);
                    return new ResourceNotFoundException("La categoria con ID: " + id + " no existe.");
                });
        //  Validación de nombre unico (solo si cambia)
        if (!exists.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName()))
            throw new BusinessException("Ya existe una categoría con el nombre: " + request.getName());

        try {
            //  Actaulizar campos
            exists.setName(request.getName());
            exists.setStatus(request.getStatus());
            exists.setDescription(request.getDescription());

            //  Si se envia una nueva imagen se procesa
            if (file != null && !file.isEmpty()) {
                String newImageName = handleImageUpload(file, exists.getImage(), id);
                exists.setImage(newImageName);
                log.info("Imagen actualizada para la categoría: {}", request.getName());
            }

            Category update = categoryRepository.save(exists);
            log.info("Categoría actualizada correctamente con ID: {}", update.getId());
            return Optional.of(categoryMapper.toResponse(update));
        } catch (IOException e) {
            log.error("Error al actualizar categoría con ID {} : {}", id,e.getMessage(),e);
            throw new FileStorageException("Error al actualizar archivo: " + e.getMessage(), e);
        }
    }

    //  Eliminar categoria
    @Transactional
    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: la categoría con ID: " + id + " no existe.");
        }
        categoryRepository.deleteById(id);
    }

    //  Metodo reutilizable para manejar la logica de la imagen (Creacion y modificacion)
    private String handleImageUpload(MultipartFile file, String oldImageName, UUID id) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.debug("Directorio de carga creado: {}", uploadPath);
        }
        //  Si existe una imagen anterior la borramos
        if (oldImageName != null) {
            Path oldImagePath = uploadPath.resolve(Paths.get(oldImageName).getFileName().toString());
            Files.deleteIfExists(oldImagePath);
            log.debug("Imagen anterior eliminada: {}", oldImagePath);
        }
        //  Guardar nueva imagen
        String newFileName = "category_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Nueva imagen guardada en: {}", filePath);

        return newFileName;
    }

    //  Paginacion
    //@Transactional(readOnly = true)
    //@Override
    //public Page<CategoryResponse> getAllPaged(Pageable pageable) {
    //    return categoryRepository.findAll(pageable).map(CategoryMapper::toResponse);    //  Convertimos cada entidad en DTO
    //}
}