package com.ecommerce.category.repositories;

import com.ecommerce.category.models.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // Paginacion
    //Page<Category> findAll(Pageable pageable);
    boolean existsById(UUID id);
    boolean existsByName(String name);

    @Query("""
        SELECT DISTINCT c FROM Category c
        WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE CONCAT('%', LOWER(:name), '%'))
            AND (:status IS NULL OR c.status = :status)
        """)
    Page<Category> filterCategoryies(
            String name,
            Boolean status,
            Pageable pageable
    );
}
