package com.bowlingpoints.service;

import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Team;
import com.bowlingpoints.entity.TeamPerson;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.TeamPersonRepository;
import com.bowlingpoints.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PersonRepository personRepository;
    private final TeamPersonRepository teamPersonRepository;

    public List<TeamDTO> getAll() {
        return teamRepository.findAll().stream()
                .map(team -> TeamDTO.builder()
                        .teamId(team.getTeamId())
                        .nameTeam(team.getNameTeam())
                        .phone(team.getPhone())
                        .status(team.getStatus())
                        .personIds(
                                team.getTeamPersons() != null
                                        ? team.getTeamPersons().stream()
                                        .map(tp -> tp.getPerson().getPersonId())
                                        .collect(Collectors.toList())
                                        : List.of()
                        )
                        .build())
                .collect(Collectors.toList());
    }

    public TeamDTO getById(Integer id) {
        return teamRepository.findById(id)
                .map(team -> TeamDTO.builder()
                        .teamId(team.getTeamId())
                        .nameTeam(team.getNameTeam())
                        .phone(team.getPhone())
                        .status(team.getStatus())
                        .personIds(
                                team.getTeamPersons() != null
                                        ? team.getTeamPersons().stream()
                                        .map(tp -> tp.getPerson().getPersonId())
                                        .collect(Collectors.toList())
                                        : List.of()
                        )
                        .build())
                .orElse(null);
    }

    public TeamDTO create(TeamDTO dto) {
        Team team = Team.builder()
                .nameTeam(dto.getNameTeam())
                .phone(dto.getPhone())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .build();

        Team savedTeam = teamRepository.save(team);

        List<TeamPerson> members = dto.getPersonIds().stream()
                .map(pid -> {
                    Person person = personRepository.findById(pid)
                            .orElseThrow(() -> new RuntimeException("Persona no encontrada ID " + pid));
                    return TeamPerson.builder()
                            .team(savedTeam)
                            .person(person)
                            .build();
                }).collect(Collectors.toList());

        teamPersonRepository.saveAll(members);

        return dto;
    }

    public boolean update(Integer id, TeamDTO dto) {
        Optional<Team> existingOpt = teamRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Team team = existingOpt.get();
        team.setNameTeam(dto.getNameTeam());
        team.setPhone(dto.getPhone());
        team.setStatus(dto.getStatus());

        teamRepository.save(team);

        teamPersonRepository.deleteAllByTeam_TeamId(id);

        List<TeamPerson> members = dto.getPersonIds().stream()
                .map(pid -> {
                    Person person = personRepository.findById(pid)
                            .orElseThrow(() -> new RuntimeException("Persona no encontrada ID " + pid));
                    return TeamPerson.builder()
                            .team(team)
                            .person(person)
                            .build();
                }).collect(Collectors.toList());

        teamPersonRepository.saveAll(members);
        return true;
    }

    public boolean delete(Integer id) {
        if (!teamRepository.existsById(id)) return false;
        teamPersonRepository.deleteAllByTeam_TeamId(id);
        teamRepository.deleteById(id);
        return true;
    }
}
