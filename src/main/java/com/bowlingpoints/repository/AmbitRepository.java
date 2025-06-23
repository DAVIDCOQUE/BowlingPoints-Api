package com.bowlingpoints.repository;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.entity.Ambit;
import com.bowlingpoints.entity.Modality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AmbitRepository extends JpaRepository<Ambit, Integer> {
    // Solo los no eliminados
    List<Ambit> findAllByDeletedAtIsNull();

    Optional<Ambit> findByName(String name);

    @Query("""
SELECT DISTINCT new com.bowlingpoints.dto.AmbitDTO(
    a.ambitId,
    a.imageUrl,
    a.name,
    a.description,
    a.status
)
FROM Tournament t
JOIN t.ambit a
WHERE t.deletedAt IS NULL
""")
    List<AmbitDTO> findDistinctWithTournaments();
}
