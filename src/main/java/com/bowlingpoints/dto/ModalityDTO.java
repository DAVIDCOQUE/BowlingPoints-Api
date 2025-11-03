package com.bowlingpoints.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModalityDTO {
    private Integer modalityId;
    private String name;
    private String description;
    private Boolean status;
}
