package com.bowlingpoints.dto;

import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Person;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubMemberDTO {

    private Integer personId;
    private String photoUrl;
    private String fullName;
    private String email;
    private String roleInClub;
    private LocalDateTime joinedAt;

    // ✅ Método estático para crear DTO desde entidad
    public static ClubMemberDTO from(ClubPerson member) {
        Person person = member.getPerson();
        // Usamos fullName y fullSurname
        String fullName = (person.getFullName() != null ? person.getFullName() : "") +
                " " +
                (person.getFullSurname() != null ? person.getFullSurname() : "");
        return ClubMemberDTO.builder()
                .personId(person.getPersonId())
                .photoUrl(person.getPhotoUrl())
                .fullName(fullName.trim())
                .email(person.getEmail())
                .roleInClub(member.getRoleInClub())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}