package com.bowlingpoints.service;

import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.Ambit;
import com.bowlingpoints.entity.Modality;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.AmbitRepository;
import com.bowlingpoints.repository.ModalityRepository;
import com.bowlingpoints.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final ModalityRepository modalityRepository;
    private final AmbitRepository ambitRepository;

    public List<TournamentDTO> getAll() {
        return tournamentRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TournamentDTO getById(Integer id) {
        return tournamentRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public TournamentDTO create(TournamentDTO dto) {
        Tournament entity = toEntity(dto);
        return toDTO(tournamentRepository.save(entity));
    }

    public boolean update(Integer id, TournamentDTO dto) {
        Optional<Tournament> existing = tournamentRepository.findById(id);
        if (existing.isEmpty()) return false;

        Tournament entity = existing.get();
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLocation(dto.getLocation());
        entity.setCauseStatus(dto.getCauseStatus());
        entity.setStatus(dto.getStatus());

        if (dto.getModalityId() != null) {
            Modality modality = modalityRepository.findById(dto.getModalityId())
                    .orElseThrow(() -> new RuntimeException("Modality not found"));
            entity.setModality(modality);
        }

        if (dto.getAmbitId() != null) {
            Ambit ambit = ambitRepository.findById(dto.getAmbitId())
                    .orElseThrow(() -> new RuntimeException("Ambit not found"));
            entity.setAmbit(ambit);
        }

        tournamentRepository.save(entity);
        return true;
    }

    public boolean delete(Integer id) {
        Optional<Tournament> entity = tournamentRepository.findById(id);
        if (entity.isEmpty()) return false;

        Tournament tournament = entity.get();
        tournament.setStatus(false); // Si quieres mantenerlo también
        tournament.setDeletedAt(LocalDateTime.now()); // Esto es el soft delete real
        tournamentRepository.save(tournament);
        return true;
    }

    // 🔁 Mapping
    private TournamentDTO toDTO(Tournament entity) {
        return TournamentDTO.builder()
                .tournamentId(entity.getTournamentId())
                .name(entity.getName())
                .modalityId(entity.getModality() != null ? entity.getModality().getModalityId() : null)
                .modalityName(entity.getModality() != null ? entity.getModality().getName() : null)
                .ambitId(entity.getAmbit() != null ? entity.getAmbit().getAmbitId() : null)         // <-- Agrega esto
                .ambitName(entity.getAmbit() != null ? entity.getAmbit().getName() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .causeStatus(entity.getCauseStatus())
                .status(entity.getStatus())
                .build();
    }

    private Tournament toEntity(TournamentDTO dto) {
        Modality modality = modalityRepository.findById(dto.getModalityId())
                .orElseThrow(() -> new RuntimeException("Modality not found"));

        Ambit ambit = null;
        if (dto.getAmbitId() != null) {
            ambit = ambitRepository.findById(dto.getAmbitId())
                    .orElseThrow(() -> new RuntimeException("Ambit not found"));
        }

        return Tournament.builder()
                .name(dto.getName())
                .modality(modality)
                .ambit(ambit)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .location(dto.getLocation())
                .causeStatus(dto.getCauseStatus())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .build();
    }
}
