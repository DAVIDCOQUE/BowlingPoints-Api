package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserFullDTO {
    private Integer userId;
    private Integer personId;
    private Integer clubId;
    private String photoUrl;
    private String nickname;
    private String document;
    private String fullName;
    private String fullSurname;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String gender;
    private String roleDescription;
    private String password;

    private List<String> roles;
}
