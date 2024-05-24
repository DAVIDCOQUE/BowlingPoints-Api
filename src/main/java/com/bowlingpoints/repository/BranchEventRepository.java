package com.bowlingpoints.repository;

import com.bowlingpoints.entity.BranchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchEventRepository extends JpaRepository<BranchEvent,Integer> {

    Optional<BranchEvent> findById(Integer integer);
}
