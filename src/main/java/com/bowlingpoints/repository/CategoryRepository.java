package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByDeletedAtIsNull(); // soft delete
}