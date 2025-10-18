package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.dto.response.CategoriesDTO;
import com.bowlingpoints.dto.response.ModalitiesDTO;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.AmbitRepository;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.repository.ResultRepository;
import com.bowlingpoints.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TournamentRepository tournamentRepository;
    private final ResultRepository resultRepository;
    private final ClubRepository clubRepository;
    private final AmbitRepository ambitRepository;

    public DashboardDTO getDashboardData() {
        // Trae solo torneos activos y no eliminados
        List<TournamentDTO> activeTournaments = tournamentRepository.findAllByStatusTrueAndDeletedAtIsNull()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        List<PlayerRankingDTO> topPlayers = resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 10));

        List<ClubDashboardDTO> topClubs = resultRepository.findTopClubsRaw().stream()
                .map(row -> ClubDashboardDTO.builder()
                        .clubId(((Number) row[0]).intValue())
                        .name((String) row[1])
                        .totalScore(row[2] != null ? ((Number) row[2]).intValue() : 0)
                        .build())
                .collect(Collectors.toList());

        List<AmbitDTO> ambits = ambitRepository.findDistinctWithTournaments();

        return DashboardDTO.builder()
                .activeTournaments(activeTournaments)
                .topPlayers(topPlayers)
                .topClubs(topClubs)
                .ambits(ambits)
                .build();
    }

    // Este método convierte un Tournament a TournamentDTO incluyendo nombres de categorías y modalidades
    private TournamentDTO toDTO(Tournament entity) {
        List<CategoriesDTO> categoriesDTOS = entity.getCategories() != null
                ? entity.getCategories().stream()
                .map(tc -> new CategoriesDTO(
                        tc.getCategory().getCategoryId(),
                        tc.getCategory().getName()
                ))
                .toList()
                : Collections.emptyList();

        List<ModalitiesDTO> modalitiesDTOS = entity.getModalities() != null
                ? entity.getModalities().stream()
                .map(tc -> new ModalitiesDTO(
                        tc.getModality().getModalityId(),
                        tc.getModality().getName()
                ))
                .toList()
                : Collections.emptyList();

        return TournamentDTO.builder()
                .tournamentId(entity.getTournamentId())
                .name(entity.getName())
                .ambitId(entity.getAmbit() != null ? entity.getAmbit().getAmbitId() : null)
                .ambitName(entity.getAmbit() != null ? entity.getAmbit().getName() : null)
                .imageUrl(entity.getImageUrl())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .stage(entity.getStage())
                .status(entity.getStatus())
                .modalities(modalitiesDTOS)
                .categories(categoriesDTOS)
                .build();
    }
}
