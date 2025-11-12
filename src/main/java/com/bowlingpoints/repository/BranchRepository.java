package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    List<Branch> findAllByStatusTrue();

    Optional<Branch> findByBranchIdAndStatusTrue(Integer branchId);

    Optional<Branch> findByNameIgnoreCase(String name);
}
