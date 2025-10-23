package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Modality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones sobre la entidad Modality.
 */
public interface ModalityRepository extends JpaRepository<Modality, Integer> {

    /**
     * Busca una modalidad por nombre exacto.
     */
    Optional<Modality> findByNameAndDeletedAtIsNull(String name);

    /**
     * Lista todas las modalidades no eliminadas, ordenadas por nombre.
     */
    List<Modality> findAllByDeletedAtIsNullOrderByNameAsc();

    /**
     * Lista todas las modalidades activas y no eliminadas, ordenadas por nombre.
     */
    List<Modality> findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc();
}
