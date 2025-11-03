package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones sobre la entidad Category.
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Obtiene todas las categorías no eliminadas.
     */
    List<Category> findAllByDeletedAtIsNullOrderByNameAsc();

    /**
     * Obtiene todas las categorías activas y no eliminadas.
     */
    List<Category> findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc();

    /**
     * Busca categoría por nombre (ignorando eliminadas).
     */
    Optional<Category> findByNameAndDeletedAtIsNull(String name);
}
