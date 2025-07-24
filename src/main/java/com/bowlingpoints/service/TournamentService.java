package com.bowlingpoints.service;

import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.dto.TournamentSummaryDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<TournamentDTO> getAll() {
        return tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TournamentDTO getById(Integer id) {
        return tournamentRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public TournamentDTO create(TournamentDTO dto) {
        Tournament entity = toEntity(dto);
        Tournament saved = tournamentRepository.save(entity);

        // Guardar categorías (pivot)
        if (dto.getCategoryIds() != null) {
            for (Integer catId : dto.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + catId));
                TournamentCategory tc = TournamentCategory.builder()
                        .tournament(saved)
                        .category(category)
                        .build();
                tournamentCategoryRepository.save(tc);
            }
        }

        // Guardar modalidades (pivot)
        if (dto.getModalityIds() != null) {
            for (Integer modId : dto.getModalityIds()) {
                Modality modality = modalityRepository.findById(modId)
                        .orElseThrow(() -> new RuntimeException("Modality not found: " + modId));
                TournamentModality tm = TournamentModality.builder()
                        .tournament(saved)
                        .modality(modality)
                        .build();
                tournamentModalityRepository.save(tm);
            }
        }

        return toDTO(saved);
    }

    public boolean update(Integer id, TournamentDTO dto) {
        Optional<Tournament> existingOpt = tournamentRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Tournament entity = existingOpt.get();
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLocation(dto.getLocation());
        entity.setCauseStatus(dto.getCauseStatus());
        entity.setStatus(dto.getStatus());

        if (dto.getAmbitId() != null) {
            Ambit ambit = ambitRepository.findById(dto.getAmbitId())
                    .orElseThrow(() -> new RuntimeException("Ambit not found"));
            entity.setAmbit(ambit);
        }

        Tournament updated = tournamentRepository.save(entity);

        // Actualizar pivotes: primero borrar los viejos, luego guardar los nuevos
        tournamentCategoryRepository.deleteAll(tournamentCategoryRepository.findByTournament_TournamentId(updated.getTournamentId()));
        tournamentModalityRepository.deleteAll(tournamentModalityRepository.findByTournament_TournamentId(updated.getTournamentId()));

        if (dto.getCategoryIds() != null) {
            for (Integer catId : dto.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + catId));
                TournamentCategory tc = TournamentCategory.builder()
                        .tournament(updated)
                        .category(category)
                        .build();
                tournamentCategoryRepository.save(tc);
            }
        }

        if (dto.getModalityIds() != null) {
            for (Integer modId : dto.getModalityIds()) {
                Modality modality = modalityRepository.findById(modId)
                        .orElseThrow(() -> new RuntimeException("Modality not found: " + modId));
                TournamentModality tm = TournamentModality.builder()
                        .tournament(updated)
                        .modality(modality)
                        .build();
                tournamentModalityRepository.save(tm);
            }
        }

        return true;
    }

    public boolean delete(Integer id) {
        Optional<Tournament> entity = tournamentRepository.findById(id);
        if (entity.isEmpty()) return false;

        Tournament tournament = entity.get();
        tournament.setStatus(false);
        tournament.setDeletedAt(LocalDateTime.now());
        tournamentRepository.save(tournament);
        return true;
    }

    // 🔁 Mapping
    private TournamentDTO toDTO(Tournament entity) {
        // Obtener categorías y modalidades relacionadas (IDs y Nombres)
        List<Integer> categoryIds = entity.getCategories() != null
                ? entity.getCategories().stream()
                .map(tc -> tc.getCategory().getCategoryId())
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<String> categoryNames = entity.getCategories() != null
                ? entity.getCategories().stream()
                .map(tc -> tc.getCategory().getName())
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<Integer> modalityIds = entity.getModalities() != null
                ? entity.getModalities().stream()
                .map(tm -> tm.getModality().getModalityId())
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<String> modalityNames = entity.getModalities() != null
                ? entity.getModalities().stream()
                .map(tm -> tm.getModality().getName())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return TournamentDTO.builder()
                .tournamentId(entity.getTournamentId())
                .name(entity.getName())
                .ambitId(entity.getAmbit() != null ? entity.getAmbit().getAmbitId() : null)
                .ambitName(entity.getAmbit() != null ? entity.getAmbit().getName() : null)
                .imageUrl(entity.getImageUrl()) // Por si usas imageUrl
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .causeStatus(entity.getCauseStatus())
                .status(entity.getStatus())
                .categoryIds(categoryIds)
                .categoryNames(categoryNames)
                .modalityIds(modalityIds)
                .modalityNames(modalityNames)
                .build();
    }


    private Tournament toEntity(TournamentDTO dto) {
        Ambit ambit = null;
        if (dto.getAmbitId() != null) {
            ambit = ambitRepository.findById(dto.getAmbitId())
                    .orElseThrow(() -> new RuntimeException("Ambit not found"));
        }

        return Tournament.builder()
                .name(dto.getName())
                .ambit(ambit)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .location(dto.getLocation())
                .causeStatus(dto.getCauseStatus())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .build();
    }

    public List<TournamentDTO> getTournamentsByAmbit(Integer ambitId, String ambitName) {
        return tournamentRepository.findTournamentsByAmbit(ambitId, ambitName);
    }

    //Resumen torneo

    public TournamentSummaryDTO getTournamentSummary(Integer tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        // Modalidades
        List<String> modalities = t.getModalities().stream()
                .map(tm -> tm.getModality().getName())
                .toList();

        // Categorías
        List<String> categories = t.getCategories().stream()
                .map(tc -> tc.getCategory().getName())
                .toList();

        // Total masculino/femenino, a prueba de todo
        Object countsRaw = resultRepository.countPlayersByGenderInTournament(tournamentId);

        Integer totalMasculino = 0;
        Integer totalFemenino = 0;

        if (countsRaw == null) {
            totalMasculino = 0;
            totalFemenino = 0;
        } else if (countsRaw instanceof Object[]) {
            Object[] arr = (Object[]) countsRaw;
            if (arr.length == 2 && arr[0] instanceof Number && arr[1] instanceof Number) {
                totalMasculino = arr[0] != null ? ((Number) arr[0]).intValue() : 0;
                totalFemenino = arr[1] != null ? ((Number) arr[1]).intValue() : 0;
            } else if (arr.length == 1 && arr[0] instanceof Object[]) {
                Object[] inner = (Object[]) arr[0];
                totalMasculino = inner[0] != null ? ((Number) inner[0]).intValue() : 0;
                totalFemenino = inner[1] != null ? ((Number) inner[1]).intValue() : 0;
            }
        } else {
            throw new IllegalStateException("Tipo inesperado: " + countsRaw.getClass());
        }

        return TournamentSummaryDTO.builder()
                .tournamentId(t.getTournamentId())
                .organizer(t.getOrganizer())
                .tournamentName(t.getName())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .location(t.getLocation())
                .modalities(modalities)
                .categories(categories)
                .totalMasculino(totalMasculino)
                .totalFemenino(totalFemenino)
                .build();
    }


}
