package com.bowlingpoints.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmbitDTO {
    private Integer ambitId;
    private String name;
    private String description;
    private Boolean status;
}
