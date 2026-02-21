package com.ecommerce.category.services;

import com.ecommerce.category.exceptions.*;
import com.ecommerce.category.mappers.CategoryMapper;
import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.models.entities.Category;
import com.ecommerce.category.repositories.CategoryRepository;
import com.ecommerce.category.validations.CategoryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de Catgeoria
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final FileStorageService fileStorageService;
    private final CategoryRepository categoryRepository;
    private final CategoryValidator categoryValidator;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(
            FileStorageService fileStorageService,
            CategoryRepository categoryRepository,
            CategoryValidator categoryValidator,
            CategoryMapper categoryMapper
    ) {
        this.fileStorageService = fileStorageService;
        this.categoryRepository = categoryRepository;
        this.categoryValidator = categoryValidator;
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
        log.info("Iniciando proceso de creación de nueva categoria");
        categoryValidator.validateOnCreate(request, file);
        //  Convertir DTO en Entidad
        Category category = categoryMapper.toEntity(request);

        //  Creamos primero sin imagen (para obtener el ID)
        Category savedCategory = categoryRepository.save(category);
        log.info("Categoría: {} creada correctamente.", request.getName());

        //  Si hay imagen la guardamos en el sistema de archivos
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = fileStorageService.saveCategoryImage(file, savedCategory.getId(), null);
                savedCategory.setImage(fileName);
                categoryRepository.save(savedCategory);
                log.info("Imagen guardada correctamente para la categoría: {}", savedCategory.getName());
            } catch (IOException e) {
                log.error("Error al guardar la imagen de la categoría '{}': {}", request.getName(), e.getMessage());
                throw new FileStorageException("Error al subir archivo: " + e.getMessage(), e);
            }
        }
        //  Devolvemos el DTO de respuesta
        return categoryMapper.toResponse(savedCategory);
    }

    //  Buscar categoria por ID
    @Transactional(readOnly = true)
    @Override
    public CategoryResponse findCategory(UUID id) {
        log.info("Obteniendo detalle de categoría con ID: {}", id);
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("No se encontró la categoría con ID: {}", id);
            return new ResourceNotFoundException("La categoria con el ID: " + id + " no existe.");
        });
        return categoryMapper.toResponse(category);
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

        categoryValidator.validateOnUpdate(id, request, file);

        //  Actaulizar campos
        exists.setName(request.getName());
        exists.setStatus(request.getStatus());
        exists.setDescription(request.getDescription());

        if (file != null && !file.isEmpty()) {
            try {
                //  Si se envia una nueva imagen se procesa
                String newImageName = fileStorageService.saveCategoryImage(file, id, exists.getImage());
                exists.setImage(newImageName);
                log.info("Imagen actualizada para la categoría: {}", request.getName());
            } catch (IOException e) {
                log.error("Error al actualizar categoría con ID {} : {}", id,e.getMessage(),e);
                throw new FileStorageException("Error al actualizar archivo: " + e.getMessage(), e);
            }
        }

        Category update = categoryRepository.save(exists);
        log.info("Categoría actualizada correctamente con ID: {}", update.getId());
        return Optional.of(categoryMapper.toResponse(update));
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
}