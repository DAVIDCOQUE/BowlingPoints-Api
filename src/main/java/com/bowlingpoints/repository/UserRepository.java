package com.bowlingpoints.repository;

import com.bowlingpoints.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Buscar usuario por nickname
    Optional<User> findByNickname(String nickname);

    // Validar si un nickname ya existe
    boolean existsByNickname(String nickname);

    // Obtener solo usuarios que no han sido eliminados lógicamente
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllNotDeleted(); // ✅ no sobrescribe findAll()

    // Obtener usuario no eliminado por ID
    @Query("SELECT u FROM User u WHERE u.userId = :id AND u.deletedAt IS NULL")
    Optional<User> findNotDeletedById(Integer id);
}
