package com.bowlingpoints.service;

import com.bowlingpoints.dto.BranchDTO;
import com.bowlingpoints.entity.Branch;
import com.bowlingpoints.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    // Crear una nueva rama
    public BranchDTO create(BranchDTO dto) {
        Branch entity = Branch.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();

        Branch saved = branchRepository.save(entity);
        return toDTO(saved);
    }

    // Obtener todas las ramas
    public List<BranchDTO> getAll() {
        return branchRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // Obtener solo ramas activas
    public List<BranchDTO> getActive() {
        return branchRepository.findAllByStatusTrue().stream()
                .map(this::toDTO)
                .toList();
    }

    // Obtener rama por ID
    public BranchDTO getById(Integer id) {
        return branchRepository.findById(id).map(this::toDTO).orElse(null);
    }

    // Actualizar una rama
    public boolean update(Integer id, BranchDTO dto) {
        Optional<Branch> existingOpt = branchRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Branch entity = existingOpt.get();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("system");

        branchRepository.save(entity);
        return true;
    }

    // Eliminar (soft delete)
    public boolean delete(Integer id) {
        Optional<Branch> entityOpt = branchRepository.findById(id);
        if (entityOpt.isEmpty()) return false;

        Branch entity = entityOpt.get();
        entity.setStatus(false);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("system");
        branchRepository.save(entity);
        return true;
    }

    private BranchDTO toDTO(Branch entity) {
        return BranchDTO.builder()
                .branchId(entity.getBranchId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .build();
    }
}
