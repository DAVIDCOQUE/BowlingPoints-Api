package com.bowlingpoints.dto;


import lombok.Data;

@Data
public class RegisterRequest {

    String username;
    String password;
    String email;

}
