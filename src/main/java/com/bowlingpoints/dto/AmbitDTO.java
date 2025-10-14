package com.bowlingpoints.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class AmbitDTO {
    private Integer ambitId;
    private String imageUrl;
    private String name;
    private String description;
    private Boolean status;

}
