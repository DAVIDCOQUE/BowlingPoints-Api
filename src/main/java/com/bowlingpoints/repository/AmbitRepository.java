package com.bowlingpoints.repository;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.entity.Ambit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones sobre la entidad Ambit.
 */
public interface AmbitRepository extends JpaRepository<Ambit, Integer> {

    /**
     * Lista ámbitos no eliminados, ordenados por nombre.
     */
    List<Ambit> findAllByDeletedAtIsNullOrderByNameAsc();

    /**
     * Lista ámbitos activos y no eliminados, ordenados por nombre.
     */
    List<Ambit> findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc();

    /**
     * Busca un ámbito por nombre.
     */
    Optional<Ambit> findByName(String name);

    /**
     * Lista ámbitos únicos con torneos no eliminados.
     */
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
                  AND a.deletedAt IS NULL
                  AND a.status = true
                ORDER BY a.name ASC
            """)
    List<AmbitDTO> findDistinctWithTournaments();
}
