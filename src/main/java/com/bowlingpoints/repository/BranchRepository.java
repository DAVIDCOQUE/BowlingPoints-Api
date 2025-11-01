package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    // Listar todas las ramas activas
    List<Branch> findAllByStatusTrue();

    // Buscar una rama específica por ID si está activa
    Optional<Branch> findByBranchIdAndStatusTrue(Integer branchId);

    // Buscar una rama por nombre
    Optional<Branch> findByName(String name);
}
