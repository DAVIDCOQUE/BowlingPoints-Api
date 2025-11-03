package com.bowlingpoints.repository;

import com.bowlingpoints.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Elimina todos los roles asociados a un usuario.
     */
    @Transactional
    void deleteByUser_UserId(Integer userId);

    /**
     * Obtiene todos los roles activos del usuario.
     */
    List<UserRole> findAllByUser_UserIdAndStatusTrue(Integer userId);

}
