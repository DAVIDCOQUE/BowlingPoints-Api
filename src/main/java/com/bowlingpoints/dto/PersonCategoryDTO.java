package com.bowlingpoints.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonCategoryDTO {
    private Integer personId;
    private List<Integer> categoryIds;
}