package com.bowlingpoints.service;

import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream().map(role -> RoleDTO.builder()
                        .roleId(role.getId())
                        .name(role.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
