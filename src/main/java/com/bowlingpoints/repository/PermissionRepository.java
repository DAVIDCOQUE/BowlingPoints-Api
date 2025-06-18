package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name); // ✅ ESTE MÉTODO

    boolean existsByName(String name); // (opcional, por si quieres usar exists en lugar de buscar)
}
