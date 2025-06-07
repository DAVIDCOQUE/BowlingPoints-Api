package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFullDTO {
    private Integer userId;
    private String nickname;
    private String email;
    private String firstname;
    private String secondname;
    private String lastname;
    private String secondlastname;
    private String phone;
    private String gender;
    private String roleDescription;

    private List<String> roles;
}
