package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResultTableDTO {
    private Integer personId;
    private String playerName;
    private String clubName;
    private List<Integer> scores; // L1, L2, ..., Ln
    private Integer total;
    private Double promedio;
}
