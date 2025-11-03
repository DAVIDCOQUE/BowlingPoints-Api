package com.bowlingpoints.repository;

import com.bowlingpoints.entity.User;
import com.bowlingpoints.projection.UserFullProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFullRepository extends CrudRepository<User, Integer> {

    /**
     * Consulta que obtiene todos los usuarios activos (no eliminados) con su información personal
     * y los roles asociados.
     * Nota: las categorías se cargan desde el servicio (no se incluyen en esta query
     * para evitar duplicaciones masivas en la proyección).
     */
    @Query(value = """
                SELECT
                    u.user_id AS userId,
                    u.person_id AS personId,
                    u.status AS status,
                    p.photo_url AS photoUrl,
                    u.nickname AS nickname,
                    p.document AS document,
                    p.email AS email,
                    p.full_name AS fullName,
                    p.full_surname AS fullSurname,
                    p.birth_date AS birthDate,
                    p.phone AS phone,
                    p.gender AS gender,
                    r.role_id AS roleId,
                    r.name AS roleName
                FROM users u
                JOIN person p ON u.person_id = p.person_id
                LEFT JOIN user_role ur ON ur.user_id = u.user_id AND ur.status = true
                LEFT JOIN roles r ON r.role_id = ur.role_id
                WHERE u.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                ORDER BY p.full_name ASC
            """, nativeQuery = true)
    List<UserFullProjection> findAllUserFull();
}
