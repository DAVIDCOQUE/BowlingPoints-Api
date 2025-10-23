package com.bowlingpoints.service;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Modality;
import com.bowlingpoints.repository.ModalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Servicio para operaciones CRUD sobre Modalidades.
 */
@Service
@RequiredArgsConstructor
public class ModalityService {

    private final ModalityRepository modalityRepository;

    /**
     * Lista todas las modalidades no eliminadas.
     */
    public ResponseGenericDTO<List<ModalityDTO>> getAll() {
        List<ModalityDTO> result = modalityRepository
                .findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toDto)
                .collect(toList());

        return new ResponseGenericDTO<>(true, "Modalidades cargadas correctamente", result);
    }

    /**
     * Lista modalidades activas y no eliminadas.
     */
    public ResponseGenericDTO<List<ModalityDTO>> getAllActives() {
        List<ModalityDTO> result = modalityRepository
                .findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc()
                .stream()
                .map(this::toDto)
                .collect(toList());

        return new ResponseGenericDTO<>(true, "Modalidades activas cargadas correctamente", result);
    }

    /**
     * Obtiene una modalidad por ID.
     */
    public ResponseGenericDTO<ModalityDTO> getById(Integer id) {
        Optional<Modality> optional = modalityRepository.findById(id);

        return optional.map(modality ->
                        new ResponseGenericDTO<>(true, "Modalidad encontrada", toDto(modality)))
                .orElseGet(() ->
                        new ResponseGenericDTO<>(false, "Modalidad no encontrada", null));
    }

    /**
     * Crea una nueva modalidad.
     */
    public ResponseGenericDTO<ModalityDTO> create(ModalityDTO dto) {
        Modality entity = Modality.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Modality saved = modalityRepository.save(entity);
        return new ResponseGenericDTO<>(true, "Modalidad creada correctamente", toDto(saved));
    }

    /**
     * Actualiza una modalidad existente.
     */
    public ResponseGenericDTO<Void> update(Integer id, ModalityDTO dto) {
        Optional<Modality> optional = modalityRepository.findById(id);

        if (optional.isPresent()) {
            Modality existing = optional.get();
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setStatus(dto.getStatus());
            existing.setUpdatedAt(LocalDateTime.now());

            modalityRepository.save(existing);
            return new ResponseGenericDTO<>(true, "Modalidad actualizada correctamente", null);
        }

        return new ResponseGenericDTO<>(false, "Modalidad no encontrada", null);
    }

    /**
     * Elimina suavemente una modalidad (soft delete).
     */
    public ResponseGenericDTO<Void> delete(Integer id) {
        Optional<Modality> optional = modalityRepository.findById(id);

        if (optional.isPresent()) {
            Modality existing = optional.get();
            existing.setDeletedAt(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());

            modalityRepository.save(existing);
            return new ResponseGenericDTO<>(true, "Modalidad eliminada correctamente", null);
        }

        return new ResponseGenericDTO<>(false, "Modalidad no encontrada", null);
    }

    /**
     * Convierte entidad a DTO.
     */
    private ModalityDTO toDto(Modality m) {
        return ModalityDTO.builder()
                .modalityId(m.getModalityId())
                .name(m.getName())
                .description(m.getDescription())
                .status(m.getStatus())
                .build();
    }
}
