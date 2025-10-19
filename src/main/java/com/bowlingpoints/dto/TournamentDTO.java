package com.bowlingpoints.dto;

import com.bowlingpoints.dto.response.CategoriesDTO;
import com.bowlingpoints.dto.response.ModalitiesDTO;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TournamentDTO {
    private Integer tournamentId;         // ID del torneo
    private String name;                  // Nombre del torneo
    private String organizer;             // Organizador
    private Integer ambitId;              // FK a Ambit
    private String ambitName;             // Nombre del ámbito (opcional para respuesta)
    private String imageUrl;              // Imagen
    private LocalDate startDate;          // Inicio
    private LocalDate endDate;            // Fin
    private String location;              // Ubicación
    private String stage;                 // Etapa
    private Boolean status;               // Activo o inactivo

    // RELACIÓN CON CATEGORÍAS
    private List<CategoriesDTO> categories;    // Datos completos (para respuesta)

    // RELACIÓN CON MODALIDADES
    private List<ModalitiesDTO> modalities;
}
