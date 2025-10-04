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

@Service
@RequiredArgsConstructor
public class AmbitService {

    private final AmbitRepository ambitRepository;

    private AmbitDTO toDTO(Ambit entity) {
        return AmbitDTO.builder()
                .ambitId(entity.getAmbitId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .build();
    }

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

    public ResponseGenericDTO<List<AmbitDTO>> getAll() {
        List<AmbitDTO> result = ambitRepository.findAllByDeletedAtIsNullOrderByNameAsc().stream().map(this::toDTO).toList();
        return new ResponseGenericDTO<>(true, "Ámbitos cargados correctamente", result);
    }

    public ResponseGenericDTO<AmbitDTO> getById(Integer id) {
        Optional<Ambit> entity = ambitRepository.findById(id);
        return entity.map(ambit -> new ResponseGenericDTO<>(true, "Ámbito encontrado", toDTO(ambit)))
                .orElseGet(() -> new ResponseGenericDTO<>(false, "Ámbito no encontrado", null));
    }

    public ResponseGenericDTO<AmbitDTO> create(AmbitDTO dto) {
        Ambit saved = ambitRepository.save(toEntity(dto));
        return new ResponseGenericDTO<>(true, "Ámbito creado correctamente", toDTO(saved));
    }

    public ResponseGenericDTO<Void> update(Integer id, AmbitDTO dto) {
        Optional<Ambit> optional = ambitRepository.findById(id);
        if (optional.isPresent()) {
            ambitRepository.save(this.validateUpdateData(optional.get(),dto));
            return new ResponseGenericDTO<>(true, "Ámbito actualizado correctamente", null);
        }
        return new ResponseGenericDTO<>(false, "Ámbito no encontrado", null);
    }

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

    private Ambit validateUpdateData(Ambit existenceAmbit, AmbitDTO newAmbit){
        boolean haveChange = false;
        if (newAmbit.getName() != null && !newAmbit.getName().equals(existenceAmbit.getName())) {
            existenceAmbit.setName(newAmbit.getName());
            haveChange = true;
        }

        if (newAmbit.getDescription() != null && !newAmbit.getDescription().equals(existenceAmbit.getDescription())) {
            existenceAmbit.setDescription(newAmbit.getDescription());
            haveChange = true;
        }

        if (newAmbit.getStatus() != null && !newAmbit.getStatus().equals(existenceAmbit.getStatus())) {
            existenceAmbit.setStatus(newAmbit.getStatus());
            haveChange = true;
        }

        if(haveChange){
            existenceAmbit.setUpdatedAt(LocalDateTime.now());
        }
        return existenceAmbit;
    }
}
