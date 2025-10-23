package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
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
    private final BranchRepository branchRepository;
    private final TournamentCategoryRepository tournamentCategoryRepository;
    private final TournamentModalityRepository tournamentModalityRepository;
    private final TournamentBranchRepository tournamentBranchRepository;
    private final ResultRepository resultRepository;
    private final TournamentRegistrationRepository tournamentRegistrationRepository;

    // Obtener todos los torneos no eliminados
    public List<TournamentDTO> getAll() {
        return tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Obtener torneo por ID
    public TournamentDTO getById(Integer id) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isEmpty()) return null;

        TournamentDTO dto = toDTO(tournamentOpt.get());

        //0 Agregar la sumatoria de jugadores por rama desde resultados
        List<TournamentBranchPlayerCountDTO> branchPlayerCounts = resultRepository.countPlayersByBranch(id);
        dto.setBranchPlayerCounts(branchPlayerCounts);

        return dto;
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

        // Guardar ramas (nuevo bloque)
        if (dto.getBranchIds() != null) {
            for (Integer branchId : dto.getBranchIds()) {
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new RuntimeException("Rama no encontrada: " + branchId));
                tournamentBranchRepository.save(
                        TournamentBranch.builder()
                                .tournament(saved)
                                .branch(branch)
                                .build()
                );
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
        tournamentCategoryRepository.deleteAll(
                tournamentCategoryRepository.findByTournament_TournamentId(updated.getTournamentId()));
        tournamentModalityRepository.deleteAll(
                tournamentModalityRepository.findByTournament_TournamentId(updated.getTournamentId()));
        tournamentBranchRepository.deleteAll(
                tournamentBranchRepository.findByTournament_TournamentId(updated.getTournamentId())
        );

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

        if (dto.getBranchIds() != null) {
            for (Integer branchId : dto.getBranchIds()) {
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new RuntimeException("Rama no encontrada: " + branchId));
                tournamentBranchRepository.save(
                        TournamentBranch.builder()
                                .tournament(updated)
                                .branch(branch)
                                .build()
                );
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

    // =============== 🔁 Mapping Helpers ===============

    private TournamentDTO toDTO(Tournament entity) {
        //  Mapeo de categorías
        List<CategoryDTO> categoryDTOS = entity.getCategories() != null
                ? entity.getCategories().stream()
                .filter(tc -> tc.getCategory() != null)
                .map(tc -> CategoryDTO.builder()
                        .categoryId(tc.getCategory().getCategoryId())
                        .name(tc.getCategory().getName())
                        .description(tc.getCategory().getDescription())
                        .status(tc.getCategory().getStatus())
                        .build())
                .toList()
                : Collections.emptyList();

        //  Mapeo de modalidades
        List<ModalityDTO> modalityDTOS = entity.getModalities() != null
                ? entity.getModalities().stream()
                .filter(tm -> tm.getModality() != null)
                .map(tm -> ModalityDTO.builder()
                        .modalityId(tm.getModality().getModalityId())
                        .name(tm.getModality().getName())
                        .description(tm.getModality().getDescription())
                        .status(tm.getModality().getStatus())
                        .build())
                .toList()
                : Collections.emptyList();

        //  Mapeo de ramas (nueva parte)
        List<BranchDTO> branchDTOS = entity.getBranches() != null
                ? entity.getBranches().stream()
                .filter(tb -> tb.getBranch() != null)
                .map(tb -> BranchDTO.builder()
                        .branchId(tb.getBranch().getBranchId())
                        .name(tb.getBranch().getName())
                        .description(tb.getBranch().getDescription())
                        .status(tb.getBranch().getStatus())
                        .build())
                .toList()
                : Collections.emptyList();

        // Mapeo de jugadores registrados
        List<TournamentRegistrationDTO> registrationDTOS = tournamentRegistrationRepository
                .findByTournament_TournamentId(entity.getTournamentId())
                .stream()
                .map(reg -> TournamentRegistrationDTO.builder()
                        .registrationId(reg.getRegistrationId())
                        .tournamentId(reg.getTournament().getTournamentId())
                        .personId(reg.getPerson().getPersonId())
                        .personFullName(reg.getPerson().getFullName() + " " + reg.getPerson().getFullSurname())
                        .categoryId(reg.getCategory().getCategoryId())
                        .categoryName(reg.getCategory().getName())
                        .modalityId(reg.getModality().getModalityId())
                        .modalityName(reg.getModality().getName())
                        .branchId(reg.getBranch().getBranchId())
                        .branchName(reg.getBranch().getName())
                        .teamId(reg.getTeam() != null ? reg.getTeam().getTeamId() : null)
                        .teamName(reg.getTeam() != null ? reg.getTeam().getNameTeam() : null)
                        .status(reg.getStatus())
                        .build())
                .toList();

        //  Construcción del DTO final
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
                .categories(categoryDTOS)
                .modalities(modalityDTOS)
                .branches(branchDTOS)
                .tournamentRegistrations(registrationDTOS)
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
