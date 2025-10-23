package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Busca un rol por su nombre.
     */
    Optional<Role> findByName(String name);

    /**
     * Lista todos los roles ordenados alfabéticamente.
     */
    List<Role> findAllByOrderByNameAsc();
}
