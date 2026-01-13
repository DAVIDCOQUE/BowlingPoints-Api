package com.bowlingpoints.service;

import com.bowlingpoints.dto.files.TournamentRegistrationRow;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentRegistrationImportService {

    private final TournamentRegistrationRepository registrationRepository;
    private final PersonRepository personRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final ModalityRepository modalityRepository;
    private final BranchRepository branchRepository;
    private final TeamRepository teamRepository;
    private final TeamPersonRepository teamPersonRepository;

    @Transactional
    public ImportResult importCsv(MultipartFile file, Integer userId, boolean skipHeader) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int skipped = 0;

        List<TournamentRegistrationRow> rows = readRows(file, skipHeader, errors);

        for (TournamentRegistrationRow row : rows) {
            String doc = safeTrim(row.documentNumber());
            String tournamentName = safeTrim(row.tournamentName());

            // Validar campos requeridos
            if (doc.isEmpty() || tournamentName.isEmpty()) {
                errors.add("Línea " + row.lineNumber() + ": documentNumber o tournamentName vacío.");
                continue;
            }

            // Buscar Person por documento
            var personOpt = personRepository.findByDocument(doc);
            if (personOpt.isEmpty()) {
                errors.add("Línea " + row.lineNumber() + ": no existe Person con documento=" + doc);
                continue;
            }

            // Buscar Tournament por nombre
            var tournamentOpt = tournamentRepository.findByName(tournamentName);
            if (tournamentOpt.isEmpty()) {
                errors.add("Línea " + row.lineNumber() + ": no existe Tournament con nombre=" + tournamentName);
                continue;
            }

            Person person = personOpt.get();
            Tournament tournament = tournamentOpt.get();

            // Buscar entidades opcionales
            Category category = findCategoryByName(safeTrim(row.categoryName()));
            Modality modality = findModalityByName(safeTrim(row.modalityName()));
            Branch branch = findBranchByName(safeTrim(row.branchName()));
            Team team = findTeamByName(safeTrim(row.teamName()));

            // Validar que la persona pertenezca al equipo (si se especificó un equipo)
            if (team != null) {
                boolean personBelongsToTeam = teamPersonRepository.existsByPerson_PersonIdAndTeam_TeamId(
                        person.getPersonId(), team.getTeamId());
                if (!personBelongsToTeam) {
                    errors.add("Línea " + row.lineNumber() + ": la persona con documento=" + doc +
                            " no pertenece al equipo '" + safeTrim(row.teamName()) + "'");
                    continue;
                }
            }

            // Validar si ya existe registro (persona + torneo + modalidad)
            Integer modalityId = modality != null ? modality.getModalityId() : null;
            boolean exists = registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                    tournament.getTournamentId(), modalityId, person.getPersonId());

            if (exists) {
                skipped++;
                continue;
            }

            // Crear nuevo registro
            TournamentRegistration registration = TournamentRegistration.builder()
                    .person(person)
                    .tournament(tournament)
                    .category(category)
                    .modality(modality)
                    .branch(branch)
                    .team(team)
                    .status(true)
                    .registrationDate(new Date())
                    .createdAt(new Date())
                    .createdBy(userId.toString())
                    .build();

            registrationRepository.save(registration);
            created++;
        }

        return new ImportResult(created, skipped, errors);
    }

    private List<TournamentRegistrationRow> readRows(MultipartFile file, boolean skipHeader, List<String> errors) {
        List<TournamentRegistrationRow> rows = new ArrayList<>();
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
                    errors.add("Línea " + lineNumber + ": se esperaban al menos 2 columnas (documentNumber, tournamentName).");
                    continue;
                }

                // Crear fila con campos opcionales
                String documentNumber = parts[0];
                String tournamentName = parts[1];
                String categoryName = parts.length > 2 ? parts[2] : "";
                String modalityName = parts.length > 3 ? parts[3] : "";
                String branchName = parts.length > 4 ? parts[4] : "";
                String teamName = parts.length > 5 ? parts[5] : "";

                rows.add(new TournamentRegistrationRow(
                        documentNumber,
                        tournamentName,
                        categoryName,
                        modalityName,
                        branchName,
                        teamName,
                        lineNumber
                ));
            }
        } catch (Exception e) {
            errors.add("Error leyendo archivo: " + e.getMessage());
        }
        return rows;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private Category findCategoryByName(String name) {
        if (name.isEmpty()) return null;
        return categoryRepository.findByNameAndDeletedAtIsNull(name).orElse(null);
    }

    private Modality findModalityByName(String name) {
        if (name.isEmpty()) return null;
        return modalityRepository.findByNameAndDeletedAtIsNull(name).orElse(null);
    }

    private Branch findBranchByName(String name) {
        if (name.isEmpty()) return null;
        return branchRepository.findByNameIgnoreCase(name).orElse(null);
    }

    private Team findTeamByName(String name) {
        if (name.isEmpty()) return null;
        return teamRepository.findByNameTeam(name).orElse(null);
    }

    public record ImportResult(int created, int skipped, List<String> errors) {}
}
