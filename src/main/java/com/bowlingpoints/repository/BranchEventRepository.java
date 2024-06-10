package com.bowlingpoints.repository;

import com.bowlingpoints.entity.EventoRama;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchEventRepository extends JpaRepository<EventoRama,Integer> {

    Optional<EventoRama> findById(Integer integer);
}
