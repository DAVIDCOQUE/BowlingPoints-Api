package com.bowlingpoints.service;

import com.bowlingpoints.dto.TournamentRegistrationDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRepository tournamentRepository;
    private final PersonRepository personRepository;
    private final CategoryRepository categoryRepository;
    private final ModalityRepository modalityRepository;
    private final BranchRepository branchRepository;
    private final TeamRepository teamRepository;

    // ================================
    // 🟢 CREAR INSCRIPCIÓN
    // ================================
    public TournamentRegistrationDTO create(TournamentRegistrationDTO dto) {
        // Validar duplicado
        if (registrationRepository.existsByTournament_TournamentIdAndPerson_PersonId(dto.getTournamentId(), dto.getPersonId())) {
            throw new RuntimeException("El jugador ya está registrado en este torneo.");
        }

        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        Category category = getOptionalCategory(dto.getCategoryId());
        Modality modality = getOptionalModality(dto.getModalityId());
        Branch branch = getOptionalBranch(dto.getBranchId());
        Team team = getOptionalTeam(dto.getTeamId());

        TournamentRegistration registration = TournamentRegistration.builder()
                .tournament(tournament)
                .person(person)
                .category(category)
                .modality(modality)
                .branch(branch)
                .team(team)
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .registrationDate(new Date())
                .createdAt(new Date())
                .createdBy("system") // TODO: Reemplazar con usuario autenticado
                .build();

        TournamentRegistration saved = registrationRepository.save(registration);
        return toDTO(saved);
    }

    // ================================
    // 🟡 ACTUALIZAR INSCRIPCIÓN
    // ================================
    public TournamentRegistrationDTO update(Integer id, TournamentRegistrationDTO dto) {
        TournamentRegistration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        reg.setCategory(getOptionalCategory(dto.getCategoryId()));
        reg.setModality(getOptionalModality(dto.getModalityId()));
        reg.setBranch(getOptionalBranch(dto.getBranchId()));

        if (dto.getTeamId() != null) {
            reg.setTeam(getOptionalTeam(dto.getTeamId()));
        } else {
            reg.setTeam(null);
        }

        reg.setStatus(dto.getStatus());
        reg.setUpdatedAt(new Date());
        reg.setUpdatedBy("system");

        TournamentRegistration saved = registrationRepository.save(reg);
        return toDTO(saved);
    }

    // ================================
    // 🔴 ELIMINAR (SOFT DELETE)
    // ================================
    public boolean delete(Integer id) {
        Optional<TournamentRegistration> opt = registrationRepository.findById(id);
        if (opt.isEmpty()) return false;

        TournamentRegistration reg = opt.get();
        reg.setStatus(false);
        reg.setUpdatedAt(new Date());
        reg.setUpdatedBy("system");

        registrationRepository.save(reg);
        return true;
    }

    // ================================
    // 🔍 CONSULTAS
    // ================================

    public List<TournamentRegistrationDTO> getAll() {
        return registrationRepository.findByStatusTrue().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<TournamentRegistrationDTO> getByTournament(Integer tournamentId) {
        return registrationRepository.findByTournament_TournamentIdAndStatusTrue(tournamentId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<TournamentRegistrationDTO> getByPerson(Integer personId) {
        return registrationRepository.findByPerson_PersonIdAndStatusTrue(personId).stream()
                .map(this::toDTO)
                .toList();
    }

    public TournamentRegistrationDTO getById(Integer id) {
        TournamentRegistration entity = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));

        return toDTO(entity);
    }

    // ================================
    // ♻️ MAPPER
    // ================================
    private TournamentRegistrationDTO toDTO(TournamentRegistration entity) {
        return TournamentRegistrationDTO.builder()
                .registrationId(entity.getRegistrationId())
                .tournamentId(entity.getTournament().getTournamentId())
                .tournamentName(entity.getTournament().getName())
                .personId(entity.getPerson().getPersonId())
                .personFullName(entity.getPerson().getFullName())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getCategoryId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .modalityId(entity.getModality() != null ? entity.getModality().getModalityId() : null)
                .modalityName(entity.getModality() != null ? entity.getModality().getName() : null)
                .branchId(entity.getBranch() != null ? entity.getBranch().getBranchId() : null)
                .branchName(entity.getBranch() != null ? entity.getBranch().getName() : null)
                .teamId(entity.getTeam() != null ? entity.getTeam().getTeamId() : null)
                .teamName(entity.getTeam() != null ? entity.getTeam().getNameTeam() : null)
                .status(entity.getStatus())
                .registrationDate(entity.getRegistrationDate())
                .build();
    }

    // ================================
    // 🔁 HELPERS (Validación de entidades opcionales)
    // ================================

    private Category getOptionalCategory(Integer id) {
        return id != null ? categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada")) : null;
    }

    private Modality getOptionalModality(Integer id) {
        return id != null ? modalityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modalidad no encontrada")) : null;
    }

    private Branch getOptionalBranch(Integer id) {
        return id != null ? branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rama no encontrada")) : null;
    }

    private Team getOptionalTeam(Integer id) {
        return id != null ? teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado")) : null;
    }
}
