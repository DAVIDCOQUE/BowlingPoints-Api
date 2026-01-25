package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final PersonRepository personRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final ModalityRepository modalityRepository;
    private final BranchRepository BranchRepository;

    public List<ResultDTO> getAll() {
        return resultRepository.findAll()
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public ResultDTO getById(Integer id) {
        return resultRepository.findById(id)
                .map(this::mapEntityToDto)
                .orElse(null);
    }

    // Filtro avanzado por torneo, rama y ronda

    public List<ResultDTO> getResultsByTournamentFiltered(Integer tournamentId, Integer branchId, Integer roundNumber) {
        return resultRepository.findAll().stream()
                .filter(r -> r.getTournament() != null && Objects.equals(r.getTournament().getTournamentId(), tournamentId))
                .filter(r -> branchId == null || (r.getBranch() != null && Objects.equals(r.getBranch().getBranchId(), branchId)))
                .filter(r -> roundNumber == null || Objects.equals(r.getRoundNumber(), roundNumber))
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public ResultDTO create(ResultDTO dto) {
        Result result = mapDtoToEntity(dto, new Result());
        Result saved = resultRepository.save(result);
        return mapEntityToDto(saved);
    }

    public boolean update(Integer id, ResultDTO dto) {
        Optional<Result> existingOpt = resultRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Result updated = mapDtoToEntity(dto, existingOpt.get());
        resultRepository.save(updated);
        return true;
    }

    public boolean delete(Integer id) {
        if (!resultRepository.existsById(id)) return false;
        resultRepository.deleteById(id);
        return true;
    }

    private ResultDTO mapEntityToDto(Result r) {
        return ResultDTO.builder()
                .resultId(r.getResultId())
                // Persona
                .personId(r.getPerson() != null ? r.getPerson().getPersonId() : null)
                .personName(r.getPerson() != null ? r.getPerson().getFullName() : null)
                // Equipo
                .teamId(r.getTeam() != null ? r.getTeam().getTeamId() : null)
                .teamName(r.getTeam() != null ? r.getTeam().getNameTeam() : null)
                // Torneo
                .tournamentId(r.getTournament() != null ? r.getTournament().getTournamentId() : null)
                .tournamentName(r.getTournament() != null ? r.getTournament().getName() : null)
                // Ronda (ahora es número directo)
                .roundNumber(r.getRoundNumber())
                // Categoría
                .categoryId(r.getCategory() != null ? r.getCategory().getCategoryId() : null)
                .categoryName(r.getCategory() != null ? r.getCategory().getName() : null)
                // Modalidad
                .modalityId(r.getModality() != null ? r.getModality().getModalityId() : null)
                .modalityName(r.getModality() != null ? r.getModality().getName() : null)
                // Datos propios
                .laneNumber(r.getLaneNumber())
                .lineNumber(r.getLineNumber())
                .score(r.getScore())
                .branchId(r.getBranch() != null ? r.getBranch().getBranchId() : null)
                .branchName(r.getBranch() != null ? r.getBranch().getName() : null)
                .build();
    }

    private Result mapDtoToEntity(ResultDTO dto, Result result) {
        //  Validaciones nuevas
        if (dto.getPersonId() == null && dto.getTeamId() == null) {
            throw new IllegalArgumentException("Debe asignar al menos una persona o un equipo.");
        }

        if (dto.getPersonId() == null && dto.getTeamId() != null) {
            throw new IllegalArgumentException("Debe asignar una persona cuando se selecciona un equipo.");
        }

        //  Persona
        if (dto.getPersonId() != null) {
            result.setPerson(personRepository.findById(dto.getPersonId())
                    .orElseThrow(() -> new RuntimeException("Persona no encontrada")));
        } else {
            result.setPerson(null);
        }

        //  Equipo
        if (dto.getTeamId() != null) {
            result.setTeam(teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado")));
        } else {
            result.setTeam(null);
        }

        //  Torneo
        result.setTournament(tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado")));

        //  Categoría
        result.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada")));

        //  Modalidad
        result.setModality(modalityRepository.findById(dto.getModalityId())
                .orElseThrow(() -> new RuntimeException("Modalidad no encontrada")));

        //  Rama
        if (dto.getBranchId() != null) {
            result.setBranch(
                    BranchRepository.findByBranchIdAndStatusTrue(dto.getBranchId())
                            .orElseThrow(() -> new RuntimeException("Rama no encontrada o inactiva"))
            );
        }

        //  Datos propios
        result.setRoundNumber(dto.getRoundNumber());
        result.setLaneNumber(dto.getLaneNumber());
        result.setLineNumber(dto.getLineNumber());
        result.setScore(dto.getScore());

        return result;
    }

    public Map<String, List<PlayerResultSummaryDTO>> getTournamentResultsByGender(Integer tournamentId) {
        List<Object[]> rows = resultRepository.findPlayerModalitySummariesByTournament(tournamentId);

        Map<String, Map<Integer, List<Object[]>>> grouped = rows.stream().collect(
                Collectors.groupingBy(
                        row -> ((String) row[2]).toLowerCase(),
                        Collectors.groupingBy(
                                row -> ((Number) row[0]).intValue()
                        )
                )
        );

        Map<String, List<PlayerResultSummaryDTO>> result = new HashMap<>();

        for (String gender : grouped.keySet()) {
            List<PlayerResultSummaryDTO> players = new ArrayList<>();
            for (List<Object[]> playerRows : grouped.get(gender).values()) {
                Integer playerId = ((Number) playerRows.get(0)[0]).intValue();
                String playerName = (String) playerRows.get(0)[1];

                List<PlayerModalitySummaryDTO> modalities = playerRows.stream().map(row ->
                        new PlayerModalitySummaryDTO(
                                ((Number) row[3]).intValue(),
                                (String) row[4],
                                ((Number) row[5]).intValue(),
                                ((Number) row[6]).doubleValue(),
                                ((Number) row[7]).intValue()
                        )
                ).collect(Collectors.toList());

                int total = modalities.stream().mapToInt(PlayerModalitySummaryDTO::getTotal).sum();
                int lineas = modalities.stream().mapToInt(PlayerModalitySummaryDTO::getLineas).sum();
                double promedio = lineas > 0 ? (double) total / lineas : 0;

                players.add(new PlayerResultSummaryDTO(playerId, playerName, modalities, total, promedio, lineas));
            }
            result.put(gender, players);
        }

        return result;
    }

    public List<PlayerResultTableDTO> getPlayerResultsForTable(Integer tournamentId, Integer modalityId, Integer roundNumber) {
        List<Object[]> raw = resultRepository.findRawPlayerResultsForTable(tournamentId, modalityId);

        Map<Integer, PlayerResultTableDTO> playerMap = new LinkedHashMap<>();
        Map<Integer, PlayerResultTableDTO> teamMap = new LinkedHashMap<>();

        for (Object[] row : raw) {
            Integer personId = (Integer) row[0];
            String playerName = (String) row[1];
            String clubName = (String) row[2];
            Integer round = (Integer) row[3];
            Integer score = (Integer) row[4];
            Integer teamId = (Integer) row[5];           // ← Asegúrate que el query lo trae
            String teamName = (String) row[6];           // ← Asegúrate que el query lo trae

            if (roundNumber != null && !roundNumber.equals(round)) continue;

            if (teamId == null) {
                // Individual
                PlayerResultTableDTO dto = playerMap.get(personId);
                if (dto == null) {
                    dto = PlayerResultTableDTO.builder()
                            .personId(personId)
                            .playerName(playerName)
                            .clubName(clubName)
                            .scores(new ArrayList<>())
                            .total(0)
                            .promedio(0.0)
                            .build();
                    playerMap.put(personId, dto);
                }

                dto.getScores().add(score);
                dto.setTotal(dto.getTotal() + score);

            } else {
                // Por equipo
                PlayerResultTableDTO dto = playerMap.get(personId);
                if (dto == null) {
                    dto = PlayerResultTableDTO.builder()
                            .personId(personId)
                            .playerName(playerName)
                            .teamId(teamId)
                            .teamName(teamName)
                            .clubName(clubName)
                            .scores(new ArrayList<>())
                            .total(0)
                            .promedio(0.0)
                            .build();
                    playerMap.put(personId, dto);
                }

                dto.getScores().add(score);
                dto.setTotal(dto.getTotal() + score);
            }
        }

        // Calcular promedios
        playerMap.values().forEach(dto -> {
            if (!dto.getScores().isEmpty()) {
                dto.setPromedio(dto.getTotal() / (double) dto.getScores().size());
            }
        });

        teamMap.values().forEach(dto -> {
            if (!dto.getScores().isEmpty()) {
                dto.setPromedio(dto.getTotal() / (double) dto.getScores().size());
            }
        });

        List<PlayerResultTableDTO> allResults = new ArrayList<>();
        allResults.addAll(playerMap.values());
        allResults.addAll(teamMap.values());

        return allResults;
    }

    public List<DashboardPlayerDTO> getAllPlayersByAvgScore() {
        return resultRepository.findAllPlayersByAvgScore();
    }

    public TournamentResultsResponseDTO getTournamentResultsTable(Integer tournamentId, Integer modalityId, Integer roundNumber) {

        List<PlayerResultTableDTO> results = getPlayerResultsForTable(tournamentId, modalityId, roundNumber);

        // Ordenar por promedio de mayor a menor y asignar posiciones
        results.sort((a, b) -> Double.compare(b.getPromedio(), a.getPromedio()));
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setPosition(i + 1);
        }

        // 2 - Modalidades jugadas
        List<ModalityDTO> modalities = tournamentRepository.findById(tournamentId)
                .map(Tournament::getModalities)
                .orElse(List.of())
                .stream()
                .filter(tm -> tm.getModality() != null)
                .map(tm -> ModalityDTO.builder()
                        .modalityId(tm.getModality().getModalityId())
                        .name(tm.getModality().getName())
                        .description(tm.getModality().getDescription())
                        .status(tm.getModality().getStatus())
                        .build())
                .collect(Collectors.toList());

        // 3 - Rondas jugadas en este torneo y modalidad
        List<Integer> rounds = resultRepository.findDistinctRoundsByTournamentAndModality(tournamentId, modalityId);

        // 4 - Promedios por línea
        List<Object[]> avgByLineRaw = resultRepository.findAvgByLineRaw(tournamentId, modalityId, roundNumber);
        Map<String, Double> avgByLine = avgByLineRaw.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).doubleValue()
                ));

// 5 - Promedio por ronda
        Double avgByRound = resultRepository.findAvgByRound(tournamentId, modalityId, roundNumber);

// 6 - Mayor línea (ahora devuelve lista)
        List<Object[]> highestLineList = resultRepository.findHighestLine(tournamentId, modalityId, roundNumber);
        Object[] highestLineRaw = highestLineList.isEmpty() ? null : highestLineList.get(0);

        HighestLineDTO highestLine = null;
        if (highestLineRaw != null) {
            highestLine = HighestLineDTO.builder()
                    .score((Integer) highestLineRaw[0])
                    .playerName((String) highestLineRaw[1])
                    .lineNumber((Integer) highestLineRaw[2])
                    .build();
        }

        TournamentResultsResponseDTO.TournamentSummary tournamentSummary = null;

        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isPresent()) {
            Tournament t = tournamentOpt.get();
            tournamentSummary = TournamentResultsResponseDTO.TournamentSummary.builder()
                    .tournamentId(t.getTournamentId())
                    .tournamentName(t.getName())
                    .startDate(t.getStartDate())
                    .endDate(t.getEndDate())
                    .location(t.getLocation())
                    .imageUrl(t.getImageUrl())
                    .status(t.getStatus())
                    .build();
        }

        // 7 - Respuesta
        return TournamentResultsResponseDTO.builder()
                .tournament(tournamentSummary)
                .modalities(modalities)
                .rounds(rounds)
                .results(results)
                .avgByLine(avgByLine)
                .avgByRound(avgByRound)
                .highestLine(highestLine)
                .build();
    }

    public TournamentResultsResponseDTO getResultsByModality(Integer tournamentId, Integer roundNumber, Integer branchId) {
        TournamentResultsResponseDTO.TournamentSummary tournamentSummary = tournamentRepository.findById(tournamentId)
                .map(t -> TournamentResultsResponseDTO.TournamentSummary.builder()
                        .tournamentId(t.getTournamentId())
                        .tournamentName(t.getName())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .location(t.getLocation())
                        .imageUrl(t.getImageUrl())
                        .status(t.getStatus())
                        .build())
                .orElse(null);

        List<ModalityDTO> modalities = tournamentRepository.findById(tournamentId)
                .map(Tournament::getModalities)
                .orElse(List.of())
                .stream()
                .filter(tm -> tm.getModality() != null)
                .map(tm -> ModalityDTO.builder()
                        .modalityId(tm.getModality().getModalityId())
                        .name(tm.getModality().getName())
                        .description(tm.getModality().getDescription())
                        .status(tm.getModality().getStatus())
                        .build())
                .collect(Collectors.toList());

        List<Integer> rounds = resultRepository.findDistinctRoundsByTournament(tournamentId);

        List<Object[]> rawData = resultRepository.findPlayerTotalsByModalityAndBranch(tournamentId, roundNumber, branchId);

        Map<Integer, PlayerByModalityDTO.PlayerByModalityDTOBuilder> playerMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            Integer personId = (Integer) row[0];
            String playerName = (String) row[1];
            String clubName = (String) row[2];
            String modalityName = (String) row[3];

            // Conversión segura de Long a Integer
            Long totalScoreLong = (Long) row[4];
            Long linesPlayedLong = (Long) row[5];

            Integer totalScore = totalScoreLong != null ? totalScoreLong.intValue() : 0;
            Integer linesPlayed = linesPlayedLong != null ? linesPlayedLong.intValue() : 0;

            PlayerByModalityDTO.PlayerByModalityDTOBuilder builder = playerMap.get(personId);

            if (builder == null) {
                builder = PlayerByModalityDTO.builder()
                        .personId(personId)
                        .playerName(playerName)
                        .clubName(clubName)
                        .modalityScores(new HashMap<>())
                        .linesPlayed(0)
                        .total(0);
                playerMap.put(personId, builder);
            }

            Map<String, Integer> modalityScores = builder.build().getModalityScores();
            modalityScores.put(modalityName, totalScore);

            builder
                    .total(builder.build().getTotal() + totalScore)
                    .linesPlayed(builder.build().getLinesPlayed() + linesPlayed)
                    .modalityScores(modalityScores);
        }

        List<PlayerByModalityDTO> resultsByModality = playerMap.values().stream()
                .map(builder -> {
                    PlayerByModalityDTO dto = builder.build();
                    double promedio = dto.getLinesPlayed() > 0
                            ? (double) dto.getTotal() / dto.getLinesPlayed()
                            : 0.0;
                    dto.setPromedio(promedio);
                    return dto;
                })
                .collect(Collectors.toList());

        return TournamentResultsResponseDTO.builder()
                .tournament(tournamentSummary)
                .modalities(modalities)
                .rounds(rounds)
                .resultsByModality(resultsByModality)
                .build();
    }


}
