package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Clubs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Clubs, Integer> {
    // Buscar un club por su nombre
    Optional<Clubs> findByName(String name);
}
