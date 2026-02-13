package com.ecommerce.category.services;

import com.ecommerce.category.models.entities.Category;
import com.ecommerce.category.repositories.CategoryRepository;
import com.ecommerce.category.validations.CategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryValidator categoryValidator;
    @InjectMocks
    private CategoryServiceImpl categoryService;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Tecnologia");
        category.setDescription("Categoria de tecnologia");
        //category.setImage();
    }
}
