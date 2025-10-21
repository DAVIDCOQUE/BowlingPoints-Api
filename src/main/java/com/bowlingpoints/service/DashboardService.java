package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.AmbitRepository;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.repository.ResultRepository;
import com.bowlingpoints.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TournamentRepository tournamentRepository;
    private final ResultRepository resultRepository;
    private final ClubRepository clubRepository;
    private final AmbitRepository ambitRepository;

    /**
     * Retorna todos los datos del dashboard:
     * - Torneos programados o aplazados
     * - Torneos en curso
     * - Top 10 jugadores
     * - Ámbitos activos con torneos
     */
    public DashboardDTO getDashboardData() {

        // ✅ Torneos activos con estado Programado o Aplazado
        List<TournamentDTO> scheduledOrPostponed = tournamentRepository
                .findActiveScheduledOrPostponed()
                .stream()
                .map(this::toDTO)
                .toList();

        // ✅ Torneos activos con estado En curso
        List<TournamentDTO> inProgress = tournamentRepository
                .findActiveInProgress()
                .stream()
                .map(this::toDTO)
                .toList();

        // ✅ Ámbitos que tengan al menos un torneo asociado
        List<AmbitDTO> ambits = ambitRepository.findDistinctWithTournaments();

        // ✅ Top 10 jugadores por promedio de puntaje
        List<DashboardPlayerDTO> topPlayers = resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 10));

        // ✅ Construcción del objeto principal del Dashboard
        return DashboardDTO.builder()
                .scheduledOrPostponedTournaments(scheduledOrPostponed)
                .inProgressTournaments(inProgress)
                .topPlayers(topPlayers)
                .ambits(ambits)
                .build();
    }

    /**
     * Convierte una entidad Tournament a su DTO completo,
     * incluyendo categorías y modalidades asociadas.
     */
    private TournamentDTO toDTO(Tournament tournament) {
        return TournamentDTO.builder()
                .tournamentId(tournament.getTournamentId())
                .name(tournament.getName())
                .organizer(tournament.getOrganizer())
                .ambitId(tournament.getAmbit() != null ? tournament.getAmbit().getAmbitId() : null)
                .ambitName(tournament.getAmbit() != null ? tournament.getAmbit().getName() : null)
                .imageUrl(tournament.getImageUrl())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .location(tournament.getLocation())
                .stage(tournament.getStage())
                .status(tournament.getStatus())

                // ✅ IDs de categorías
                .categoryIds(
                        tournament.getCategories() != null
                                ? tournament.getCategories().stream()
                                .map(tc -> tc.getCategory().getCategoryId())
                                .toList()
                                : null
                )

                // ✅ Nombres de categorías
                .categoryNames(
                        tournament.getCategories() != null
                                ? tournament.getCategories().stream()
                                .map(tc -> tc.getCategory().getName())
                                .toList()
                                : null
                )

                // ✅ Objetos completos de categorías
                .categories(
                        tournament.getCategories() != null
                                ? tournament.getCategories().stream()
                                .map(tc -> CategoryDTO.builder()
                                        .categoryId(tc.getCategory().getCategoryId())
                                        .name(tc.getCategory().getName())
                                        .description(tc.getCategory().getDescription())
                                        .status(tc.getCategory().getStatus())
                                        .build())
                                .toList()
                                : null
                )

                // ✅ IDs de modalidades
                .modalityIds(
                        tournament.getModalities() != null
                                ? tournament.getModalities().stream()
                                .map(tm -> tm.getModality().getModalityId())
                                .toList()
                                : null
                )

                // ✅ Nombres de modalidades
                .modalityNames(
                        tournament.getModalities() != null
                                ? tournament.getModalities().stream()
                                .map(tm -> tm.getModality().getName())
                                .toList()
                                : null
                )

                // ✅ Objetos completos de modalidades
                .modalities(
                        tournament.getModalities() != null
                                ? tournament.getModalities().stream()
                                .map(tm -> ModalityDTO.builder()
                                        .modalityId(tm.getModality().getModalityId())
                                        .name(tm.getModality().getName())
                                        .description(tm.getModality().getDescription())
                                        .status(tm.getModality().getStatus())
                                        .build())
                                .toList()
                                : null
                )

                .build();
    }
}
