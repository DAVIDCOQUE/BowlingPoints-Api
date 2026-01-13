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

        if (dto.getNameTeam() == null || dto.getNameTeam().isBlank()) {
            throw new RuntimeException("El nombre del equipo es obligatorio.");
        }

        boolean isTournamentFlow =
                dto.getTournamentId() != null &&
                        dto.getCategoryId() != null &&
                        dto.getModalityId() != null;

        if (isTournamentFlow) {
            if (dto.getPlayerIds() == null || dto.getPlayerIds().size() < 2) {
                throw new RuntimeException(
                        "Debe seleccionar al menos 2 jugadores para registrar el equipo en un torneo."
                );
            }
        }

        // 🔍 Buscar equipo existente por nombre
        Optional<Team> existingTeamOpt = teamRepository.findByNameTeam(dto.getNameTeam());

        Team team;

        if (existingTeamOpt.isPresent()) {

            // ❌ Si NO es flujo torneo, no permitir duplicados
            if (!isTournamentFlow) {
                throw new RuntimeException(
                        "Ya existe un equipo con el nombre: " + dto.getNameTeam()
                );
            }

            // ✅ Flujo torneo → reutilizar equipo existente
            team = existingTeamOpt.get();

        } else {

            // ✅ Crear nuevo equipo
            team = Team.builder()
                    .nameTeam(dto.getNameTeam())
                    .phone(dto.getPhone())
                    .status(dto.getStatus() != null ? dto.getStatus() : true)
                    .build();

            teamRepository.save(team);
        }

        // =====================================================
        // ASOCIAR JUGADORES AL EQUIPO (si vienen)
        // =====================================================
        List<Integer> playerIds = Optional.ofNullable(dto.getPlayerIds()).orElse(Collections.emptyList());

        if (!playerIds.isEmpty()) {

            // Evitar duplicados
            teamPersonRepository.deleteAllByTeam_TeamId(team.getTeamId());

            List<TeamPerson> members = playerIds.stream()
                    .map(pid -> {
                        Person person = personRepository.findById(pid)
                                .orElseThrow(() -> new RuntimeException("Persona no encontrada ID " + pid));
                        return TeamPerson.builder()
                                .team(team)
                                .person(person)
                                .build();
                    })
                    .toList();

            teamPersonRepository.saveAll(members);
        }

        // =====================================================
        // ASOCIAR EQUIPO AL TORNEO
        // =====================================================
        if (isTournamentFlow) {

            tournamentTeamRepository
                    .findByTournament_TournamentIdAndTeam_TeamId(
                            dto.getTournamentId(),
                            team.getTeamId()
                    )
                    .orElseGet(() -> tournamentTeamRepository.save(
                            TournamentTeam.builder()
                                    .team(team)
                                    .tournament(
                                            Tournament.builder()
                                                    .tournamentId(dto.getTournamentId())
                                                    .build()
                                    )
                                    .status(true)
                                    .build()
                    ));
        }

        // =====================================================
        // REGISTRAR JUGADORES EN TORNEO
        // =====================================================
        if (isTournamentFlow) {

            Tournament tournament = Tournament.builder()
                    .tournamentId(dto.getTournamentId())
                    .build();

            Category category = Category.builder()
                    .categoryId(dto.getCategoryId())
                    .build();

            Modality modality = Modality.builder()
                    .modalityId(dto.getModalityId())
                    .build();

            for (Integer pid : playerIds) {

                Person person = personRepository.findById(pid)
                        .orElseThrow(() -> new RuntimeException("Jugador no encontrado ID=" + pid));

                Branch branch = branchRepository.findByNameIgnoreCase(person.getGender())
                        .orElseThrow(() ->
                                new RuntimeException("Rama no encontrada para género " + person.getGender())
                        );

                if (!tournamentRegistrationRepository
                        .existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                                dto.getTournamentId(),
                                dto.getModalityId(),
                                pid
                        )) {

                    tournamentRegistrationRepository.save(
                            TournamentRegistration.builder()
                                    .person(person)
                                    .team(team)
                                    .tournament(tournament)
                                    .category(category)
                                    .modality(modality)
                                    .branch(branch)
                                    .status(true)
                                    .build()
                    );
                }
            }
        }

        // =====================================================
        // RESPONSE
        // =====================================================
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

    @Transactional
    public boolean update(Integer id, TeamDTO dto) {
        Optional<Team> existingOpt = teamRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Team team = existingOpt.get();

        team.setNameTeam(dto.getNameTeam());
        team.setPhone(dto.getPhone());
        team.setStatus(dto.getStatus());
        teamRepository.save(team);

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

        if (dto.getTournamentId() != null) {
            // Eliminar vínculo anterior si existía
            tournamentTeamRepository.findByTournament_TournamentIdAndTeam_TeamId(dto.getTournamentId(), id)
                    .ifPresentOrElse(
                            tt -> {
                            }, // Ya existe
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

        Team team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            return false;
        }

        // 🚫 Si el equipo está asociado a algún torneo
        if (tournamentTeamRepository.existsByTeam_TeamId(id)) {
            throw new RuntimeException(
                    "No se puede eliminar el equipo porque está asociado a un torneo."
            );
        }

        // 🚫 Si el equipo tiene jugadores inscritos en torneos
        if (tournamentRegistrationRepository.existsByTeam_TeamId(id)) {
            throw new RuntimeException(
                    "No se puede eliminar el equipo porque tiene registros en torneos."
            );
        }

        // ✅ Eliminar solo miembros del equipo
        teamPersonRepository.deleteAllByTeam_TeamId(id);

        // ✅ Eliminar equipo
        teamRepository.delete(team);

        return true;
    }


}
