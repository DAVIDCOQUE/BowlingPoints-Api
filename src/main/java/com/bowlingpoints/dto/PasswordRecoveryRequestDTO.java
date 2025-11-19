package com.bowlingpoints.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRecoveryRequestDTO {
    @NotBlank(message = "El identificador (email o documento) es obligatorio.")
    private String identifier;
}
