package com.bowlingpoints.service;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.entity.Modality;
import com.bowlingpoints.repository.ModalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ModalityService {

    private final ModalityRepository modalityRepository;

    public List<ModalityDTO> getAll() {
        return modalityRepository.findAll().stream()
                .map(m -> new ModalityDTO(m.getModalityId(), m.getName(), m.getDescription(), m.getStatus()))
                .collect(toList());
    }

    public ModalityDTO getById(Integer id) {
        return modalityRepository.findById(id)
                .map(m -> new ModalityDTO(m.getModalityId(), m.getName(), m.getDescription(), m.getStatus()))
                .orElse(null);
    }

    public ModalityDTO create(ModalityDTO dto) {
        Modality m = Modality.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();
        return toDto(modalityRepository.save(m));
    }

    public boolean update(Integer id, ModalityDTO dto) {
        return modalityRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setDescription(dto.getDescription());
                    existing.setStatus(dto.getStatus());
                    modalityRepository.save(existing);
                    return true;
                }).orElse(false);
    }

    public boolean delete(Integer id) {
        return modalityRepository.findById(id)
                .map(existing -> {
                    existing.setDeletedAt(java.time.LocalDateTime.now());
                    modalityRepository.save(existing);
                    return true;
                }).orElse(false);
    }

    private ModalityDTO toDto(Modality m) {
        return new ModalityDTO(m.getModalityId(), m.getName(), m.getDescription(), m.getStatus());
    }
}
