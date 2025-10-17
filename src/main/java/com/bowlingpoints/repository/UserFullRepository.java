package com.bowlingpoints.repository;

import com.bowlingpoints.entity.User;
import com.bowlingpoints.projection.UserFullProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFullRepository extends CrudRepository<User, Integer> {

    @Query(value = """
                SELECT
                    u.user_id AS userId,
                    u.person_id AS personId,
                    p.photo_url AS photoUrl,
                    u.nickname AS nickname,
                    p.document AS document,
                    p.email AS email,
                    p.full_name AS fullName,
                    p.full_surname AS fullSurname,
                    p.birth_date AS birthDate,
                    p.phone AS phone,
                    p.gender AS gender,
                    r.name AS roleName 
                FROM users u
                JOIN person p ON u.person_id = p.person_id
                JOIN user_role ur ON ur.user_id = u.user_id
                JOIN roles r ON r.role_id = ur.role_id
                WHERE u.deleted_at IS NULL AND p.deleted_at IS NULL
                ORDER BY p.full_name ASC
            """, nativeQuery = true)
    List<UserFullProjection> findAllUserFull();

}
