package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona,Integer> {
}
