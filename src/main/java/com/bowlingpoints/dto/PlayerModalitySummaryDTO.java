package com.bowlingpoints.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerModalitySummaryDTO {
    private Integer modalityId;
    private String modalityName;
    private Integer total;
    private Double promedio;
    private Integer lineas;
}
