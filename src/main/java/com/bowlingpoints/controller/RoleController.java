package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Roles list retrieved", roles));
    }
}