package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Integer> {
}

