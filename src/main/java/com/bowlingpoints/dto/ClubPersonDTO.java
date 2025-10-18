package com.bowlingpoints.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubPersonDTO {
    private Integer clubPersonId;  // ID del registro club_person (para edición)
    private Integer clubId;
    private Integer personId;      // Persona asociada
    private String roleInClub;     // Rol del miembro en el club

    // Info básica de la persona (puede expandirse según necesidad)
    private String fullName;
    private String fullSurname;
    private String document;
    private String email;
    private String phone;
    private String gender;
    private String photoUrl;
}
