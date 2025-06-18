// src/main/java/com/bowlingpoints/dto/ClubMemberRequestDTO.java
package com.bowlingpoints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClubMemberRequestDTO {
    @NotNull private Integer clubId;
    @NotNull private Integer personId;
    @NotBlank private String roleInClub;
}
