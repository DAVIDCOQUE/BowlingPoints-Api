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

    // =========================
    // CRUD BÁSICO
    // =========================

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

    // =========================
    // MAPEOS DTO <-> ENTITY
    // =========================

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
                .rama(r.getRama())
                .build();
    }

    private Result mapDtoToEntity(ResultDTO dto, Result result) {
        if (dto.getPersonId() != null && dto.getTeamId() != null) {
            throw new IllegalArgumentException("No se puede asignar persona y equipo al mismo tiempo.");
        }

        if (dto.getPersonId() != null) {
            result.setPerson(personRepository.findById(dto.getPersonId())
                    .orElseThrow(() -> new RuntimeException("Persona no encontrada")));
            result.setTeam(null);
        }

        if (dto.getTeamId() != null) {
            result.setTeam(teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado")));
            result.setPerson(null);
        }

        result.setTournament(tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado")));

        result.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada")));

        result.setModality(modalityRepository.findById(dto.getModalityId())
                .orElseThrow(() -> new RuntimeException("Modalidad no encontrada")));

        // 🔁 Ronda como número (sin relación)
        result.setRoundNumber(dto.getRoundNumber());

        result.setLaneNumber(dto.getLaneNumber());
        result.setLineNumber(dto.getLineNumber());
        result.setScore(dto.getScore());
        result.setRama(dto.getRama());

        return result;
    }

    // =========================
    // FUNCIONES AVANZADAS
    // =========================

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

    public List<PlayerResultTableDTO> getPlayerResultsForTable(Integer tournamentId, Integer modalityId) {
        List<Object[]> raw = resultRepository.findRawPlayerResultsForTable(tournamentId, modalityId);

        Map<Integer, PlayerResultTableDTO> playerMap = new LinkedHashMap<>();

        for (Object[] row : raw) {
            Integer personId = (Integer) row[0];
            String playerName = (String) row[1];
            String clubName = (String) row[2];
            Integer roundNumber = (Integer) row[3];
            Integer score = (Integer) row[4];

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
        }

        for (PlayerResultTableDTO dto : playerMap.values()) {
            if (!dto.getScores().isEmpty()) {
                dto.setPromedio(dto.getTotal() / (double) dto.getScores().size());
            }
        }

        return new ArrayList<>(playerMap.values());
    }

    public List<DashboardPlayerDTO> getAllPlayersByAvgScore() {
        return resultRepository.findAllPlayersByAvgScore();
    }
}
