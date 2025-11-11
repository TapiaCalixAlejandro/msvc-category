package com.ecommerce.category.repositories;

import com.ecommerce.category.models.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Paginacion
    Page<Category> findAll(Pageable pageable);
    boolean existsById(Long id);
    boolean existsByName(String name);
}
