package com.bowlingpoints.dto;

import lombok.*;
/**
 * DTO para la transferencia de datos de Ambit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmbitDTO {

    /** ID del ámbito */
    private Integer ambitId;

    /** URL de la imagen asociada al ámbito */
    private String imageUrl;

    /** Nombre del ámbito */
    private String name;

    /** Descripción del ámbito */
    private String description;

    /** Estado del ámbito (activo/inactivo) */
    private Boolean status;
}
