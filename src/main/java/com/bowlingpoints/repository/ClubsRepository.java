package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Clubs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubsRepository extends JpaRepository<Clubs, Integer> {

    // Puedes agregar consultas personalizadas si se requieren más adelante.
}
