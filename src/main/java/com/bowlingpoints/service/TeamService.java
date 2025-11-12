package com.bowlingpoints.service;

import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PersonRepository personRepository;
    private final TeamPersonRepository teamPersonRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentRegistrationRepository tournamentRegistrationRepository;
    private final BranchRepository branchRepository;

    // =====================================================
    // GET ALL
    // =====================================================
    public List<TeamDTO> getAll() {
        return teamRepository.findAll().stream()
                .map(team -> TeamDTO.builder()
                        .teamId(team.getTeamId())
                        .nameTeam(team.getNameTeam())
                        .phone(team.getPhone())
                        .status(team.getStatus())
                        .playerIds(
                                team.getTeamPersons() != null
                                        ? team.getTeamPersons().stream()
                                        .map(tp -> tp.getPerson().getPersonId())
                                        .collect(Collectors.toList())
                                        : List.of()
                        )
                        .build())
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET BY ID
    // =====================================================
    public TeamDTO getById(Integer id) {
        return teamRepository.findById(id)
                .map(team -> TeamDTO.builder()
                        .teamId(team.getTeamId())
                        .nameTeam(team.getNameTeam())
                        .phone(team.getPhone())
                        .status(team.getStatus())
                        .playerIds(
                                team.getTeamPersons() != null
                                        ? team.getTeamPersons().stream()
                                        .map(tp -> tp.getPerson().getPersonId())
                                        .collect(Collectors.toList())
                                        : List.of()
                        )
                        .build())
                .orElse(null);
    }

    // =====================================================
    // CREATE TEAM
    // =====================================================
    @Transactional
    public TeamDTO create(TeamDTO dto) {

        // ✅ Validaciones básicas
        if (dto.getNameTeam() == null || dto.getNameTeam().isBlank()) {
            throw new RuntimeException("El nombre del equipo es obligatorio.");
        }

        if (dto.getPlayerIds() == null || dto.getPlayerIds().size() < 2) {
            throw new RuntimeException("Debe seleccionar al menos 2 jugadores para crear un equipo.");
        }

        // 🚫 Evitar equipos duplicados por nombre
        if (teamRepository.findByNameTeam(dto.getNameTeam()).isPresent()) {
            throw new RuntimeException("Ya existe un equipo con el nombre: " + dto.getNameTeam());
        }

        // 1️⃣ Crear equipo
        Team team = Team.builder()
                .nameTeam(dto.getNameTeam())
                .phone(dto.getPhone())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .build();

        teamRepository.save(team);

        // 2️ Asociar jugadores al equipo
        List<Integer> playerIds = Optional.ofNullable(dto.getPlayerIds()).orElse(Collections.emptyList());

        List<TeamPerson> members = playerIds.stream()
                .map(pid -> {
                    Person person = personRepository.findById(pid)
                            .orElseThrow(() -> new RuntimeException("Persona no encontrada ID " + pid));
                    return TeamPerson.builder()
                            .team(team)
                            .person(person)
                            .build();
                }).toList();

        teamPersonRepository.saveAll(members);

        // 3️⃣ Asociar equipo al torneo
        if (dto.getTournamentId() != null) {
            TournamentTeam tt = TournamentTeam.builder()
                    .team(team)
                    .tournament(Tournament.builder().tournamentId(dto.getTournamentId()).build())
                    .status(true)
                    .build();
            tournamentTeamRepository.save(tt);
        }

        // 4️⃣ Registrar jugadores individualmente con rama automática
        if (dto.getTournamentId() != null && dto.getCategoryId() != null && dto.getModalityId() != null) {
            Tournament tournament = Tournament.builder().tournamentId(dto.getTournamentId()).build();
            Category category = Category.builder().categoryId(dto.getCategoryId()).build();
            Modality modality = Modality.builder().modalityId(dto.getModalityId()).build();

            for (Integer pid : dto.getPlayerIds()) {
                Person person = personRepository.findById(pid)
                        .orElseThrow(() -> new RuntimeException("Jugador no encontrado ID=" + pid));

                // Buscar rama por nombre igual al género (masculino/femenino)
                Branch branch = branchRepository.findByNameIgnoreCase(person.getGender())
                        .orElseThrow(() -> new RuntimeException("Rama no encontrada para género " + person.getGender()));

                // ⚡ Evitar duplicados de inscripción
                if (!tournamentRegistrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                        dto.getTournamentId(), dto.getModalityId(), pid)) {
                    TournamentRegistration reg = TournamentRegistration.builder()
                            .person(person)
                            .team(team)
                            .tournament(tournament)
                            .category(category)
                            .modality(modality)
                            .branch(branch)
                            .status(true)
                            .build();

                    tournamentRegistrationRepository.save(reg);
                }
            }
        }

        // ✅ Retornar DTO completo
        return TeamDTO.builder()
                .teamId(team.getTeamId())
                .nameTeam(team.getNameTeam())
                .phone(team.getPhone())
                .status(team.getStatus())
                .playerIds(dto.getPlayerIds())
                .categoryId(dto.getCategoryId())
                .modalityId(dto.getModalityId())
                .tournamentId(dto.getTournamentId())
                .build();
    }

    // =====================================================
    // UPDATE TEAM (Actualiza relaciones también)
    // =====================================================
    @Transactional
    public boolean update(Integer id, TeamDTO dto) {
        Optional<Team> existingOpt = teamRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Team team = existingOpt.get();

        // ✅ Actualizar datos del equipo
        team.setNameTeam(dto.getNameTeam());
        team.setPhone(dto.getPhone());
        team.setStatus(dto.getStatus());
        teamRepository.save(team);

        // 🔄 Actualizar miembros
        teamPersonRepository.deleteAllByTeam_TeamId(id);

        List<Integer> playerIds = Optional.ofNullable(dto.getPlayerIds()).orElse(Collections.emptyList());

        List<TeamPerson> members = playerIds.stream()
                .map(pid -> {
                    Person person = personRepository.findById(pid)
                            .orElseThrow(() -> new RuntimeException("Persona no encontrada ID " + pid));
                    return TeamPerson.builder()
                            .team(team)
                            .person(person)
                            .build();
                }).toList();

        teamPersonRepository.saveAll(members);

        // 🔄 Actualizar vínculo equipo/torneo
        if (dto.getTournamentId() != null) {
            // Eliminar vínculo anterior si existía
            tournamentTeamRepository.findByTournament_TournamentIdAndTeam_TeamId(dto.getTournamentId(), id)
                    .ifPresentOrElse(
                            tt -> {}, // Ya existe
                            () -> {
                                TournamentTeam newTT = TournamentTeam.builder()
                                        .team(team)
                                        .tournament(Tournament.builder().tournamentId(dto.getTournamentId()).build())
                                        .status(true)
                                        .build();
                                tournamentTeamRepository.save(newTT);
                            }
                    );
        }

        // 🔄 Actualizar inscripciones individuales
        if (dto.getTournamentId() != null && dto.getCategoryId() != null && dto.getModalityId() != null) {
            Tournament tournament = Tournament.builder().tournamentId(dto.getTournamentId()).build();
            Category category = Category.builder().categoryId(dto.getCategoryId()).build();
            Modality modality = Modality.builder().modalityId(dto.getModalityId()).build();

            for (Integer pid : dto.getPlayerIds()) {
                Person person = personRepository.findById(pid)
                        .orElseThrow(() -> new RuntimeException("Jugador no encontrado ID=" + pid));

                Branch branch = branchRepository.findByNameIgnoreCase(person.getGender())
                        .orElseThrow(() -> new RuntimeException("Rama no encontrada para género " + person.getGender()));

                // Evitar duplicados
                if (!tournamentRegistrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                        dto.getTournamentId(), dto.getModalityId(), pid)) {
                    TournamentRegistration reg = TournamentRegistration.builder()
                            .person(person)
                            .team(team)
                            .tournament(tournament)
                            .category(category)
                            .modality(modality)
                            .branch(branch)
                            .status(true)
                            .build();

                    tournamentRegistrationRepository.save(reg);
                }
            }
        }

        return true;
    }

    // =====================================================
    // DELETE TEAM
    // =====================================================
    @Transactional
    public boolean delete(Integer id) {
        if (!teamRepository.existsById(id)) return false;

        // Eliminar relaciones
        teamPersonRepository.deleteAllByTeam_TeamId(id);
        tournamentTeamRepository.findAll()
                .stream()
                .filter(tt -> tt.getTeam().getTeamId().equals(id))
                .forEach(tt -> tournamentTeamRepository.delete(tt));

        tournamentRegistrationRepository.findAll()
                .stream()
                .filter(tr -> tr.getTeam() != null && tr.getTeam().getTeamId().equals(id))
                .forEach(tr -> tournamentRegistrationRepository.delete(tr));

        teamRepository.deleteById(id);
        return true;
    }
}
