package com.bowlingpoints.service;

import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.dto.TournamentSummaryDTO;
import com.bowlingpoints.dto.response.CategoriesDTO;
import com.bowlingpoints.dto.response.ModalitiesDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final AmbitRepository ambitRepository;
    private final ModalityRepository modalityRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentCategoryRepository tournamentCategoryRepository;
    private final TournamentModalityRepository tournamentModalityRepository;
    private final ResultRepository resultRepository;

    // Obtener todos los torneos no eliminados
    public List<TournamentDTO> getAll() {
        return tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Obtener torneo por ID
    public TournamentDTO getById(Integer id) {
        return tournamentRepository.findById(id).map(this::toDTO).orElse(null);
    }

    // Crear torneo
    public TournamentDTO create(TournamentDTO dto) {
        validateDates(dto);

        Tournament entity = toEntity(dto);
        Tournament saved = tournamentRepository.save(entity);

        // Guardar categorías
        if (dto.getCategoryIds() != null) {
            for (Integer catId : dto.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + catId));
                TournamentCategory tc = TournamentCategory.builder()
                        .tournament(saved)
                        .category(category)
                        .build();
                tournamentCategoryRepository.save(tc);
            }
        }

        // Guardar modalidades
        if (dto.getModalityIds() != null) {
            for (Integer modId : dto.getModalityIds()) {
                Modality modality = modalityRepository.findById(modId)
                        .orElseThrow(() -> new RuntimeException("Modalidad no encontrada: " + modId));
                TournamentModality tm = TournamentModality.builder()
                        .tournament(saved)
                        .modality(modality)
                        .build();
                tournamentModalityRepository.save(tm);
            }
        }

        return toDTO(saved);
    }

    // Actualizar torneo
    public boolean update(Integer id, TournamentDTO dto) {
        Optional<Tournament> existingOpt = tournamentRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        validateDates(dto);

        Tournament entity = existingOpt.get();
        entity.setName(dto.getName());
        entity.setOrganizer(dto.getOrganizer());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLocation(dto.getLocation());
        entity.setStage(dto.getStage());
        entity.setStatus(dto.getStatus());
        entity.setImageUrl(dto.getImageUrl());

        if (dto.getAmbitId() != null) {
            Ambit ambit = ambitRepository.findById(dto.getAmbitId())
                    .orElseThrow(() -> new RuntimeException("Ámbito no encontrado"));
            entity.setAmbit(ambit);
        }

        Tournament updated = tournamentRepository.save(entity);

        // Eliminar categorías y modalidades antiguas
        tournamentCategoryRepository.deleteAll(tournamentCategoryRepository.findByTournament_TournamentId(updated.getTournamentId()));
        tournamentModalityRepository.deleteAll(tournamentModalityRepository.findByTournament_TournamentId(updated.getTournamentId()));

        // Guardar nuevas categorías
        if (dto.getCategoryIds() != null) {
            for (Integer catId : dto.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + catId));
                TournamentCategory tc = TournamentCategory.builder()
                        .tournament(updated)
                        .category(category)
                        .build();
                tournamentCategoryRepository.save(tc);
            }
        }

        // Guardar nuevas modalidades
        if (dto.getModalityIds() != null) {
            for (Integer modId : dto.getModalityIds()) {
                Modality modality = modalityRepository.findById(modId)
                        .orElseThrow(() -> new RuntimeException("Modalidad no encontrada: " + modId));
                TournamentModality tm = TournamentModality.builder()
                        .tournament(updated)
                        .modality(modality)
                        .build();
                tournamentModalityRepository.save(tm);
            }
        }

        return true;
    }

    // Eliminar torneo (soft delete)
    public boolean delete(Integer id) {
        Optional<Tournament> entity = tournamentRepository.findById(id);
        if (entity.isEmpty()) return false;

        Tournament tournament = entity.get();
        tournament.setStatus(false);
        tournament.setDeletedAt(LocalDateTime.now());
        tournamentRepository.save(tournament);
        return true;
    }

    // Obtener torneos por ID de ámbito
    public List<TournamentDTO> getTournamentsByAmbit(Integer ambitId, String ambitName) {
        return tournamentRepository.findByAmbit_AmbitIdAndDeletedAtIsNull(ambitId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Obtener resumen del torneo
    public TournamentSummaryDTO getTournamentSummary(Integer tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        List<String> modalities = t.getModalities().stream()
                .map(tm -> tm.getModality().getName())
                .toList();

        List<String> categories = t.getCategories().stream()
                .map(tc -> tc.getCategory().getName())
                .toList();

        Object countsRaw = resultRepository.countPlayersByGenderInTournament(tournamentId);

        int totalMasculino = 0;
        int totalFemenino = 0;

        if (countsRaw instanceof Object[] arr) {
            if (arr.length == 2) {
                totalMasculino = arr[0] != null ? ((Number) arr[0]).intValue() : 0;
                totalFemenino = arr[1] != null ? ((Number) arr[1]).intValue() : 0;
            } else if (arr.length == 1 && arr[0] instanceof Object[] inner) {
                totalMasculino = inner[0] != null ? ((Number) inner[0]).intValue() : 0;
                totalFemenino = inner[1] != null ? ((Number) inner[1]).intValue() : 0;
            }
        }

        return TournamentSummaryDTO.builder()
                .tournamentId(t.getTournamentId())
                .tournamentName(t.getName())
                .organizer(t.getOrganizer())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .location(t.getLocation())
                .modalities(modalities)
                .categories(categories)
                .totalMasculino(totalMasculino)
                .totalFemenino(totalFemenino)
                .build();
    }

    // =============== 🔁 Mapping Helpers ===============

    private TournamentDTO toDTO(Tournament entity) {
        List<CategoriesDTO> categoriesDTOS = entity.getCategories() != null
                ? entity.getCategories().stream()
                .map(tc -> new CategoriesDTO(
                        tc.getCategory().getCategoryId(),
                        tc.getCategory().getName()))
                .toList()
                : Collections.emptyList();

        List<ModalitiesDTO> modalitiesDTOS = entity.getModalities() != null
                ? entity.getModalities().stream()
                .map(tm -> new ModalitiesDTO(
                        tm.getModality().getModalityId(),
                        tm.getModality().getName()))
                .toList()
                : Collections.emptyList();

        return TournamentDTO.builder()
                .tournamentId(entity.getTournamentId())
                .name(entity.getName())
                .organizer(entity.getOrganizer())
                .ambitId(entity.getAmbit() != null ? entity.getAmbit().getAmbitId() : null)
                .ambitName(entity.getAmbit() != null ? entity.getAmbit().getName() : null)
                .imageUrl(entity.getImageUrl())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .stage(entity.getStage())
                .status(entity.getStatus())
                .categories(categoriesDTOS)
                .modalities(modalitiesDTOS)
                .build();
    }

    private Tournament toEntity(TournamentDTO dto) {
        Ambit ambit = null;
        if (dto.getAmbitId() != null) {
            ambit = ambitRepository.findById(dto.getAmbitId())
                    .orElseThrow(() -> new RuntimeException("Ámbito no encontrado"));
        }

        return Tournament.builder()
                .name(dto.getName())
                .organizer(dto.getOrganizer())
                .ambit(ambit)
                .imageUrl(dto.getImageUrl())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .location(dto.getLocation())
                .stage(dto.getStage())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .build();
    }

    // Validar fechas
    private void validateDates(TournamentDTO dto) {
        if (dto.getStartDate() != null && dto.getEndDate() != null &&
                dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }
}
