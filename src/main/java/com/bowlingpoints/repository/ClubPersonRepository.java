package com.bowlingpoints.repository;

import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubPersonRepository extends JpaRepository<ClubPerson, Integer> {

    // Trae todas las personas activas de un club
    List<ClubPerson> findByClubAndStatusIsTrue(Clubs club);

    // Trae todos los clubes activos a los que pertenece una persona (normalmente devolvería uno)
    List<ClubPerson> findByPersonAndStatusIsTrue(Person person);

    // Trae el registro activo de una persona (más limpio si solo puede tener uno)
    Optional<ClubPerson> findFirstByPersonAndStatusIsTrue(Person person);

    // Borra todos los registros asociados a un club
    @Transactional
    void deleteAllByClub_ClubId(Integer clubId);
}
