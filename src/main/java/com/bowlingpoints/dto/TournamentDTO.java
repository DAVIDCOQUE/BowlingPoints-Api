package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDTO {
    private Integer tournamentId;
    private String name;
    private String organizer;

    // Relación con ambit
    private Integer ambitId;
    private String ambitName;

    private String imageUrl; // 🔥 Nuevo campo para la imagen

    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String causeStatus;
    private Boolean status;

    // 🔥 NUEVO: múltiples categorías/modalidades
    private List<Integer> categoryIds;
    private List<Integer> modalityIds;

    // (Opcional) Nombres si quieres mostrarlos en el front:
    private List<String> categoryNames;
    private List<String> modalityNames;

    // Constructor personalizado (opcional, puedes actualizarlo si lo necesitas)
    public TournamentDTO(Integer tournamentId, String name, String organizer, String location, String modalityName, String ambitName, LocalDate startDate) {
        this.tournamentId = tournamentId;
        this.name = name;
        this.organizer = organizer;
        this.location = location;
        this.ambitName = ambitName;
        this.startDate = startDate;
        // Como ahora es lista, modalityName y categoryName ya no hacen falta aquí
    }
}
