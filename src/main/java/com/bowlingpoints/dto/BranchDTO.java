package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
    private Integer branchId;
    private String name;
    private String description;
    private Boolean status;
}
