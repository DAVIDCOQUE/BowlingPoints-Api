package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    Optional<Person> findByDocument(String document);
    Optional<Person> findByFullNameAndFullSurname(String fullName, String fullSurname);
}

