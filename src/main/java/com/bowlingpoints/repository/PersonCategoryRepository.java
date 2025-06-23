package com.bowlingpoints.repository;

import com.bowlingpoints.entity.PersonCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonCategoryRepository extends JpaRepository<PersonCategory, Integer> {
    List<PersonCategory> findByPerson_PersonId(Integer personId);
    void deleteAllByPerson_PersonId(Integer personId);
}
