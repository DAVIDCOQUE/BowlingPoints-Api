package com.bowlingpoints.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CategoryDTO {
    private Integer categoryId;
    private String name;
    private String description;
    private Boolean status;
}