package com.bowlingpoints.service;

import com.bowlingpoints.dto.files.TeamPersonRow;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Team;
import com.bowlingpoints.entity.TeamPerson;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.TeamPersonRepository;
import com.bowlingpoints.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamPersonImportService {

    private final PersonRepository personRepository;
    private final TeamRepository teamRepository;
    private final TeamPersonRepository teamPersonRepository;

    @Transactional
    public ImportResult importCsv(MultipartFile file, Integer userId, boolean skipHeader) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int skipped = 0;

        List<TeamPersonRow> rows = readRows(file, skipHeader, errors);

        for (TeamPersonRow row : rows) {
            String doc = safeTrim(row.documentNumber());
            String teamName = safeTrim(row.teamName());

            if (doc.isEmpty() || teamName.isEmpty()) {
                errors.add("Línea " + row.lineNumber() + ": documentNumber o teamName vacío.");
                continue;
            }

            var personOpt = personRepository.findByDocument(doc);
            if (personOpt.isEmpty()) {
                errors.add("Línea " + row.lineNumber() + ": no existe Person con documento=" + doc);
                continue;
            }

            var teamOpt = teamRepository.findByNameTeam(teamName);
            if (teamOpt.isEmpty()) {
                errors.add("Línea " + row.lineNumber() + ": no existe Team con nombre=" + teamName);
                continue;
            }

            Person person = personOpt.get();
            Team team = teamOpt.get();

            boolean exists = teamPersonRepository.existsByPerson_PersonIdAndTeam_TeamId(person.getPersonId(), team.getTeamId());
            if (exists) {
                skipped++;
                continue;
            }

            TeamPerson entity = TeamPerson.builder()
                    .person(person)
                    .team(team)
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();

            teamPersonRepository.save(entity);
            created++;
        }

        return new ImportResult(created, skipped, errors);
    }

    private List<TeamPersonRow> readRows(MultipartFile file, boolean skipHeader, List<String> errors) {
        List<TeamPersonRow> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (skipHeader && lineNumber == 1) continue;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    errors.add("Línea " + lineNumber + ": se esperaban 2 columnas (documentNumber, teamName).");
                    continue;
                }

                rows.add(new TeamPersonRow(parts[0], parts[1], lineNumber));
            }
        } catch (Exception e) {
            errors.add("Error leyendo archivo: " + e.getMessage());
        }
        return rows;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    public record ImportResult(int created, int skipped, List<String> errors) {}
}

