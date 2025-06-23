package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<Round, Integer> {
}
