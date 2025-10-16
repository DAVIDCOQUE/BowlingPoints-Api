// src/main/java/com/bowlingpoints/dto/ClubMemberRequestDTO.java
package com.bowlingpoints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ClubMemberRequestDTO {
    @NotNull private Integer clubId;
    @NotNull private Integer personId;
    @NotBlank private String roleInClub;
}
