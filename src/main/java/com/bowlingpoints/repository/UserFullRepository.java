package com.bowlingpoints.repository;

import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFullRepository extends CrudRepository<User, Integer> {

    @Query(value = """
            SELECT
                u.user_id,
                p.photo_url,
                u.nickname,
                 p.document,
                p.email,
                p.first_name,
                p.second_name,
                p.lastname,
                p.second_lastname,
                p.phone,
                p.gender,
                r.description AS role_description
            FROM users u
            JOIN person p ON u.person_id = p.person_id
            JOIN user_role ur ON ur.user_id = u.user_id
            JOIN roles r ON r.role_id = ur.role_id
            WHERE u.deleted_at IS NULL AND p.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> getUserFullInfoRaw();
}
