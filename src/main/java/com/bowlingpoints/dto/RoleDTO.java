package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // <-- necesaria para usar RoleDTO.builder()
@NoArgsConstructor
@AllArgsConstructor

public class RoleDTO {
    private Integer roleId;
    private String description;
}