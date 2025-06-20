package com.bowlingpoints.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Integer categoryId;
    private String name;
    private String description;
    private Boolean status;
}