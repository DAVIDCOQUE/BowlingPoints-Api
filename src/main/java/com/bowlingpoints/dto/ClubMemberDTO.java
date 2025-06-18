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
    private String fullName;
    private String email;
    private String roleInClub;
    private LocalDateTime joinedAt;

    // ✅ Método estático para crear DTO desde entidad
    public static ClubMemberDTO from(ClubPerson member) {
        Person person = member.getPerson();
        return ClubMemberDTO.builder()
                .personId(person.getPersonId())
                .fullName(person.getFirstName() + " " + person.getLastname())
                .email(person.getEmail())
                .roleInClub(member.getRoleInClub())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
