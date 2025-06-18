package com.bowlingpoints.repository;

import com.bowlingpoints.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Transactional
    void deleteByUser_UserId(Integer userId); // nota: navega a través del campo user
}