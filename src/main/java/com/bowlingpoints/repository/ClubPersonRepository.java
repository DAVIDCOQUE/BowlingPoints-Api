package com.bowlingpoints.repository;

import com.bowlingpoints.entity.ClubPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubPersonRepository extends JpaRepository<ClubPerson, Integer> {

    void deleteAllByClub_ClubId(Integer clubId);

    // Buscar todos los ClubPerson activos (no eliminados) por clubId
    List<ClubPerson> findAllByClub_ClubIdAndDeletedAtIsNull(Integer clubId);

    // Buscar un ClubPerson activo por personId
    Optional<ClubPerson> findFirstByPerson_PersonIdAndStatusTrue(Integer personId);
}
