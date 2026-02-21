package com.ecommerce.category.services;

import com.ecommerce.category.exceptions.BusinessException;
import com.ecommerce.category.exceptions.ResourceNotFoundException;
import com.ecommerce.category.mappers.CategoryMapper;
import com.ecommerce.category.models.dtos.CategoryRequest;
import com.ecommerce.category.models.dtos.CategoryResponse;
import com.ecommerce.category.models.entities.Category;
import com.ecommerce.category.repositories.CategoryRepository;
import com.ecommerce.category.validations.CategoryValidator;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryValidator categoryValidator;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryFilterService categoryFilterService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {
        // GIVEN
        CategoryRequest request = new CategoryRequest();
        request.setName("Suplementos");
        request.setDescription("Categoría de suplementos alimenticios");

        MultipartFile file = mock(MultipartFile.class);

        Category category = new Category();
        category.setId(UUID.randomUUID());

        CategoryResponse response = new CategoryResponse();
        response.setName("Suplementos");
        response.setDescription("Categoría de suplementos alimenticios");

        doNothing().when(categoryValidator).validateOnCreate(request, file);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(fileStorageService.saveCategoryImage(eq(file), any(UUID.class), isNull())).thenReturn("image.jpg");
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        // WHEN
        CategoryResponse result = categoryService.createCategory(request, file);

        // THEN
        assertNotNull(result);
        assertEquals("Suplementos", result.getName());
        assertEquals("Categoría de suplementos alimenticios", result.getDescription());

        verify(categoryValidator).validateOnCreate(request, file);
        verify(categoryMapper).toEntity(request);
        verify(categoryRepository, times(2)).save(any(Category.class));
        verify(fileStorageService).saveCategoryImage(eq(file), any(UUID.class), isNull());
    }

    @Test
    void shouldThrowBusinessExceptionWhenCategoryExists() {
        // GIVEN
        CategoryRequest request = new CategoryRequest();
        request.setName("Suplementos");

        MultipartFile file = mock(MultipartFile.class);

        doThrow(new BusinessException("La categoria ya existe"))
                .when(categoryValidator)
                .validateOnCreate(any(CategoryRequest.class), any());

        // WHEN + THEN
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.createCategory(request, file)
        );

        assertEquals("La categoria ya existe", exception.getMessage());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldReturnCategoryById() {
        UUID id = UUID.randomUUID();

        Category category = new Category();
        category.setId(id);

        CategoryResponse response = new CategoryResponse();
        response.setName("Suplementos");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.findCategory(id);

        assertNotNull(result);
        assertEquals("Suplementos", result.getName());
    }

    // No encontrado - 404
    @Test
    void shouldThrowResourceNotFoundWhenCategoryNotExists() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findCategory(id));
    }

    // Soft Delete
    @Test
    void shouldSoftDeleteCategory() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.existsById(id)).thenReturn(true);

        categoryService.deleteCategory(id);

        verify(categoryRepository).deleteById(id);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // Pagination
    @Test
    void shouldReturnPagedCategories() {
        //Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setName("Suplementos");

        Page<Category> page = new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1);

        CategoryResponse response = new CategoryResponse();
        response.setName("Suplementos");

        Page<CategoryResponse> responsePage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(categoryFilterService.filterCategory(any(), any(), anyInt(), anyInt(), any(), any())).thenReturn(responsePage);

        Page<CategoryResponse> result = categoryFilterService.filterCategory(null, true, 10, 0, null, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Suplementos", result.getContent().get(0).getName());
    }
}
