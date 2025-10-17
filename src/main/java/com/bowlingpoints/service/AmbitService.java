package com.bowlingpoints.service;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Ambit;
import com.bowlingpoints.repository.AmbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones CRUD sobre Ambits.
 */
@Service
@RequiredArgsConstructor
public class AmbitService {

    private final AmbitRepository ambitRepository;

    /**
     * Convierte entidad a DTO.
     */
    private AmbitDTO toDTO(Ambit entity) {
        return AmbitDTO.builder()
                .ambitId(entity.getAmbitId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .build();
    }

    /**
     * Convierte DTO a entidad nueva.
     */
    private Ambit toEntity(AmbitDTO dto) {
        return Ambit.builder()
                .ambitId(dto.getAmbitId())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Lista todos los ambits no eliminados.
     */
    public ResponseGenericDTO<List<AmbitDTO>> getAll() {
        List<AmbitDTO> result = ambitRepository
                .findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .toList();

        return new ResponseGenericDTO<>(true, "Ámbitos cargados correctamente", result);
    }

    /**
     * Lista solo los ambits activos (status = true y no eliminados).
     */
    public ResponseGenericDTO<List<AmbitDTO>> getAllActives() {
        List<AmbitDTO> result = ambitRepository
                .findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .toList();

        return new ResponseGenericDTO<>(true, "Ámbitos activos cargados correctamente", result);
    }

    /**
     * Lista ambits que tienen torneos asociados no eliminados.
     */

    public ResponseGenericDTO<List<AmbitDTO>> getAmbitsWithTournaments() {
        List<AmbitDTO> result = ambitRepository.findDistinctWithTournaments();
        return new ResponseGenericDTO<>(true, "Ámbitos con torneos cargados correctamente", result);
    }

    /**
     * Busca un ambit por ID.
     */
    public ResponseGenericDTO<AmbitDTO> getById(Integer id) {
        Optional<Ambit> entity = ambitRepository.findById(id);
        return entity.map(ambit -> new ResponseGenericDTO<>(true, "Ámbito encontrado", toDTO(ambit)))
                .orElseGet(() -> new ResponseGenericDTO<>(false, "Ámbito no encontrado", null));
    }

    /**
     * Crea un nuevo ambit.
     */
    public ResponseGenericDTO<AmbitDTO> create(AmbitDTO dto) {
        Ambit saved = ambitRepository.save(toEntity(dto));
        return new ResponseGenericDTO<>(true, "Ámbito creado correctamente", toDTO(saved));
    }

    /**
     * Actualiza un ambit existente con los datos del DTO.
     */
    public ResponseGenericDTO<Void> update(Integer id, AmbitDTO dto) {
        Optional<Ambit> optional = ambitRepository.findById(id);
        if (optional.isPresent()) {
            Ambit entity = validateUpdateData(optional.get(), dto);
            ambitRepository.save(entity);
            return new ResponseGenericDTO<>(true, "Ámbito actualizado correctamente", null);
        }
        return new ResponseGenericDTO<>(false, "Ámbito no encontrado", null);
    }

    /**
     * Elimina un ambit de forma suave (soft delete).
     */
    public ResponseGenericDTO<Void> delete(Integer id) {
        Optional<Ambit> optional = ambitRepository.findById(id);
        if (optional.isPresent()) {
            Ambit entity = optional.get();
            entity.setStatus(false);
            entity.setDeletedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            ambitRepository.save(entity);
            return new ResponseGenericDTO<>(true, "Ámbito eliminado correctamente", null);
        }
        return new ResponseGenericDTO<>(false, "Ámbito no encontrado", null);
    }

    /**
     * Aplica solo los cambios válidos al objeto existente.
     */
    private Ambit validateUpdateData(Ambit existing, AmbitDTO dto) {
        boolean changed = false;

        if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
            existing.setName(dto.getName());
            changed = true;
        }

        if (dto.getDescription() != null && !dto.getDescription().equals(existing.getDescription())) {
            existing.setDescription(dto.getDescription());
            changed = true;
        }

        if (dto.getStatus() != null && !dto.getStatus().equals(existing.getStatus())) {
            existing.setStatus(dto.getStatus());
            changed = true;
        }

        if (changed) {
            existing.setUpdatedAt(LocalDateTime.now());
        }

        return existing;
    }
}
