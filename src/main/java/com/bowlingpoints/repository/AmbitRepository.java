package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Ambit;
import com.bowlingpoints.entity.Modality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmbitRepository extends JpaRepository<Ambit, Integer> {
    // Solo los no eliminados
    List<Ambit> findAllByDeletedAtIsNull();

    Optional<Ambit> findByName(String name);
}
