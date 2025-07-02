package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByDeletedAtIsNull(); // soft delete

    Optional<Category> findByName(String name);
}