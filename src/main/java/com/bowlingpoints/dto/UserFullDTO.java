package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import com.bowlingpoints.dto.CategoryDTO;
/**
 * DTO para crear o representar un usuario completo con datos personales y roles.
 */

import com.bowlingpoints.dto.CategoryDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFullDTO {

    // Datos del usuario
    private Integer userId;
    private String nickname;
    private String password;

    // Datos personales (Person)
    private Integer personId;
    private Integer clubId;
    private String photoUrl;
    private String document;
    private String fullName;
    private String fullSurname;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String gender;

    // Roles asociados
    private List<RoleDTO> roles;

    // Categorías completas asociadas
    private List<CategoryDTO> categories;
}
