package com.bowlingpoints.service;

import com.bowlingpoints.dto.files.ResultImportRow;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultImportService {

    private final PersonRepository personRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final ModalityRepository modalityRepository;
    private final BranchRepository branchRepository;
    private final TeamRepository teamRepository;
    private final ResultRepository resultRepository;

    @Transactional
    public ImportResult importCsv(MultipartFile file, Integer userId, boolean skipHeader) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int skipped = 0;

        List<ResultImportRow> rows = readRows(file, skipHeader, errors);

        if (rows.isEmpty()) {
            errors.add("No se encontraron filas válidas para procesar.");
            return new ImportResult(created, skipped, errors);
        }

        // Validar que todos los resultados pertenezcan al mismo torneo
        String firstTournamentName = rows.get(0).nombreTorneo();
        boolean allSameTournament = rows.stream()
                .allMatch(row -> row.nombreTorneo().equals(firstTournamentName));

        if (!allSameTournament) {
            errors.add("ERROR CRÍTICO: Todos los resultados deben pertenecer al mismo torneo. " +
                    "Se encontraron múltiples torneos en el archivo.");
            return new ImportResult(created, skipped, errors);
        }

        // Resolver el torneo una sola vez (todos son del mismo torneo)
        var tournamentOpt = tournamentRepository.findByName(firstTournamentName);
        if (tournamentOpt.isEmpty()) {
            errors.add("ERROR CRÍTICO: No existe torneo con nombre '" + firstTournamentName + "'");
            return new ImportResult(created, skipped, errors);
        }
        Tournament tournament = tournamentOpt.get();

        // Procesar cada fila
        List<Result> resultsToSave = new ArrayList<>();

        for (ResultImportRow row : rows) {
            try {
                // Validar campos obligatorios
                if (isEmpty(row.documento()) || isEmpty(row.categoria()) ||
                        isEmpty(row.modalidad()) || isEmpty(row.rama())) {
                    errors.add("Línea " + row.lineNumber() + ": campos obligatorios vacíos " +
                            "(documento, categoría, modalidad o rama).");
                    continue;
                }

                if (row.puntaje() == null || row.numeroRonda() == null ||
                        row.numeroCarril() == null || row.numeroLinea() == null) {
                    errors.add("Línea " + row.lineNumber() + ": campos numéricos obligatorios vacíos " +
                            "(puntaje, ronda, carril o línea).");
                    continue;
                }

                // Validar rangos
                if (row.puntaje() < 0 || row.puntaje() > 300) {
                    errors.add("Línea " + row.lineNumber() + ": puntaje fuera de rango (0-300): " + row.puntaje());
                    continue;
                }

                if (row.numeroRonda() <= 0 || row.numeroCarril() <= 0 || row.numeroLinea() <= 0) {
                    errors.add("Línea " + row.lineNumber() + ": números de ronda, carril o línea deben ser > 0");
                    continue;
                }

                // Resolver Person
                var personOpt = personRepository.findByDocument(row.documento());
                if (personOpt.isEmpty()) {
                    errors.add("Línea " + row.lineNumber() + ": no existe jugador con documento '" + row.documento() + "'");
                    continue;
                }
                Person person = personOpt.get();

                // Resolver Category
                var categoryOpt = categoryRepository.findByNameAndDeletedAtIsNull(row.categoria());
                if (categoryOpt.isEmpty()) {
                    errors.add("Línea " + row.lineNumber() + ": no existe categoría '" + row.categoria() + "'");
                    continue;
                }
                Category category = categoryOpt.get();

                // Resolver Modality
                var modalityOpt = modalityRepository.findByNameAndDeletedAtIsNull(row.modalidad());
                if (modalityOpt.isEmpty()) {
                    errors.add("Línea " + row.lineNumber() + ": no existe modalidad '" + row.modalidad() + "'");
                    continue;
                }
                Modality modality = modalityOpt.get();

                // Resolver Branch
                var branchOpt = branchRepository.findByNameIgnoreCase(row.rama());
                if (branchOpt.isEmpty()) {
                    errors.add("Línea " + row.lineNumber() + ": no existe rama '" + row.rama() + "'");
                    continue;
                }
                Branch branch = branchOpt.get();

                // Resolver Team (obligatorio según modalidad)
                // Si la modalidad contiene "Sencillo" o "Individual", el equipo es opcional
                // De lo contrario, el equipo es obligatorio
                String modalityNameLower = modality.getName().toLowerCase();
                boolean isIndividualModality = modalityNameLower.contains("sencillo") ||
                        modalityNameLower.contains("individual");

                Team team = null;
                if (!isEmpty(row.equipo())) {
                    var teamOpt = teamRepository.findByNameTeam(row.equipo());
                    if (teamOpt.isEmpty()) {
                        errors.add("Línea " + row.lineNumber() + ": no existe equipo '" + row.equipo() + "'");
                        continue;
                    }
                    team = teamOpt.get();
                } else if (!isIndividualModality) {
                    errors.add("Línea " + row.lineNumber() + ": el equipo es obligatorio para la modalidad '" +
                            modality.getName() + "'");
                    continue;
                }

                // Detectar duplicados
                boolean exists = resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                        person.getPersonId(),
                        tournament.getTournamentId(),
                        row.numeroRonda(),
                        row.numeroLinea()
                );

                if (exists) {
                    skipped++;
                    errors.add("Línea " + row.lineNumber() + ": resultado duplicado (jugador=" + row.documento() +
                            ", ronda=" + row.numeroRonda() + ", línea=" + row.numeroLinea() + "). Saltado.");
                    continue;
                }

                // Crear Result
                Result result = Result.builder()
                        .person(person)
                        .team(team)
                        .tournament(tournament)
                        .category(category)
                        .modality(modality)
                        .branch(branch)
                        .roundNumber(row.numeroRonda())
                        .laneNumber(row.numeroCarril())
                        .lineNumber(row.numeroLinea())
                        .score(row.puntaje())
                        .createdBy(userId)
                        .updatedBy(userId)
                        .build();

                resultsToSave.add(result);
                created++;

                // Batch processing cada 500 registros
                if (resultsToSave.size() >= 500) {
                    resultRepository.saveAll(resultsToSave);
                    resultsToSave.clear();
                }

            } catch (Exception e) {
                errors.add("Línea " + row.lineNumber() + ": error inesperado -> " + e.getMessage());
                log.error("Error procesando línea {}: {}", row.lineNumber(), e.getMessage(), e);
            }
        }

        // Guardar los restantes
        if (!resultsToSave.isEmpty()) {
            resultRepository.saveAll(resultsToSave);
        }

        return new ImportResult(created, skipped, errors);
    }

    private List<ResultImportRow> readRows(MultipartFile file, boolean skipHeader, List<String> errors) {
        List<ResultImportRow> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (skipHeader && lineNumber == 1) continue;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 10) {
                    errors.add("Línea " + lineNumber + ": se esperaban 10 columnas (documento, nombreTorneo, categoria, " +
                            "modalidad, rama, equipo, numeroRonda, numeroCarril, numeroLinea, puntaje). Se encontraron " + parts.length);
                    continue;
                }

                try {
                    Integer numeroRonda = parseInteger(parts[6].trim(), lineNumber, "numeroRonda", errors);
                    Integer numeroCarril = parseInteger(parts[7].trim(), lineNumber, "numeroCarril", errors);
                    Integer numeroLinea = parseInteger(parts[8].trim(), lineNumber, "numeroLinea", errors);
                    Integer puntaje = parseInteger(parts[9].trim(), lineNumber, "puntaje", errors);

                    if (numeroRonda == null || numeroCarril == null || numeroLinea == null || puntaje == null) {
                        continue;  // Errores ya registrados en parseInteger
                    }

                    rows.add(new ResultImportRow(
                            parts[0].trim(),  // documento
                            parts[1].trim(),  // nombreTorneo
                            parts[2].trim(),  // categoria
                            parts[3].trim(),  // modalidad
                            parts[4].trim(),  // rama
                            parts[5].trim(),  // equipo
                            numeroRonda,
                            numeroCarril,
                            numeroLinea,
                            puntaje,
                            lineNumber
                    ));
                } catch (Exception e) {
                    errors.add("Línea " + lineNumber + ": error parseando datos -> " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Error leyendo archivo: " + e.getMessage());
            log.error("Error leyendo archivo CSV: {}", e.getMessage(), e);
        }
        return rows;
    }

    private Integer parseInteger(String value, int lineNumber, String fieldName, List<String> errors) {
        if (value == null || value.isEmpty()) {
            errors.add("Línea " + lineNumber + ": campo '" + fieldName + "' vacío");
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            errors.add("Línea " + lineNumber + ": campo '" + fieldName + "' no es un número válido: '" + value + "'");
            return null;
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public record ImportResult(int created, int skipped, List<String> errors) {
    }
}
