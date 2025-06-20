package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Modality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModalityRepository extends JpaRepository<Modality, Integer> {
}
