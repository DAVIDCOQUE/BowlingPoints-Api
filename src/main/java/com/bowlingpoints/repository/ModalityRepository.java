package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Modality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModalityRepository extends JpaRepository<Modality, Integer> {

    Optional<Modality> findByName(String name);

    List<Modality> findAllByOrderByNameAsc();
}
