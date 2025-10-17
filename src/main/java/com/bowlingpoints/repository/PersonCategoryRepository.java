package com.bowlingpoints.repository;

import com.bowlingpoints.entity.PersonCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonCategoryRepository extends JpaRepository<PersonCategory, Integer> {

    // Trae todas las categorías asociadas a una persona
    List<PersonCategory> findByPerson_PersonId(Integer personId);

    // Elimina todas las relaciones categoría-persona de una persona
    void deleteAllByPerson_PersonId(Integer personId);
}