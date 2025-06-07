package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Role;
import com.bowlingpoints.entity.Permission;
import com.bowlingpoints.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    boolean existsByRoleAndPermission(Role role, Permission permission);
}