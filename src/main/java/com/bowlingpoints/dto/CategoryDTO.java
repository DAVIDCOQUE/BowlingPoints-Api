package com.bowlingpoints.dto;

import lombok.*;

/**
 * DTO para la transferencia de datos de la entidad Category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Integer categoryId;
    private String name;
    private String description;
    private Boolean status;
}
