package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio principal para gestión de resultados, ranking y resúmenes de torneos.
 */
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final PersonRepository personRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final RoundRepository roundRepository;
    private final CategoryRepository categoryRepository;
    private final ModalityRepository modalityRepository;

    /**
     * Retorna TODOS los resultados (todas las filas de la tabla result).
     */
    public List<ResultDTO> getAll() {
        return resultRepository.findAll()
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca un resultado específico por su ID.
     */
    public ResultDTO getById(Integer id) {
        return resultRepository.findById(id)
                .map(this::mapEntityToDto)
                .orElse(null);
    }

    /**
     * Crea un nuevo resultado a partir de un DTO.
     */
    public ResultDTO create(ResultDTO dto) {
        Result result = mapDtoToEntity(dto, new Result());
        Result saved = resultRepository.save(result);
        return mapEntityToDto(saved);
    }

    /**
     * Actualiza un resultado existente por ID.
     */
    public boolean update(Integer id, ResultDTO dto) {
        Optional<Result> existingOpt = resultRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Result updated = mapDtoToEntity(dto, existingOpt.get());
        resultRepository.save(updated);
        return true;
    }

    /**
     * Elimina un resultado por su ID.
     */
    public boolean delete(Integer id) {
        if (!resultRepository.existsById(id)) return false;
        resultRepository.deleteById(id);
        return true;
    }

    /**
     * Mapea una entidad Result a su DTO correspondiente.
     */
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
                // Ronda
                .roundId(r.getRound() != null ? r.getRound().getRoundId() : null)
                .roundNumber(r.getRound() != null ? r.getRound().getRoundNumber() : null)
                // Categoría
                .categoryId(r.getCategory() != null ? r.getCategory().getCategoryId() : null)
                .categoryName(r.getCategory() != null ? r.getCategory().getName() : null)
                // Modalidad
                .modalityId(r.getModality() != null ? r.getModality().getModalityId() : null)
                .modalityName(r.getModality() != null ? r.getModality().getName() : null)
                // Datos propios del resultado
                .laneNumber(r.getLaneNumber())
                .lineNumber(r.getLineNumber())
                .score(r.getScore())
                .build();
    }

    /**
     * Mapea un DTO a una entidad Result (para crear o actualizar resultados).
     */
    private Result mapDtoToEntity(ResultDTO dto, Result result) {
        if (dto.getPersonId() != null) {
            result.setPerson(personRepository.findById(dto.getPersonId()).orElse(null));
            result.setTeam(null); // Si se establece persona, elimina equipo
        }

        if (dto.getTeamId() != null) {
            result.setTeam(teamRepository.findById(dto.getTeamId()).orElse(null));
            result.setPerson(null); // Si se establece equipo, elimina persona
        }

        result.setTournament(tournamentRepository.findById(dto.getTournamentId()).orElseThrow());
        result.setRound(roundRepository.findById(dto.getRoundId()).orElseThrow());
        result.setCategory(categoryRepository.findById(dto.getCategoryId()).orElseThrow());
        result.setModality(modalityRepository.findById(dto.getModalityId()).orElseThrow());
        result.setLaneNumber(dto.getLaneNumber());
        result.setLineNumber(dto.getLineNumber());
        result.setScore(dto.getScore());

        return result;
    }

    /**
     * Agrupa los resultados de un torneo por género y por jugador.
     * Devuelve para cada género una lista de resúmenes por jugador.
     */
    public Map<String, List<PlayerResultSummaryDTO>> getTournamentResultsByGender(Integer tournamentId) {
        List<Object[]> rows = resultRepository.findPlayerModalitySummariesByTournament(tournamentId);

        // Agrupamos por género y luego por jugador
        Map<String, Map<Integer, List<Object[]>>> grouped = rows.stream().collect(
                Collectors.groupingBy(
                        row -> ((String) row[2]).toLowerCase(), // gender: masculino/femenino
                        Collectors.groupingBy(
                                row -> ((Number) row[0]).intValue() // playerId
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
                                ((Number) row[3]).intValue(), // modalityId
                                (String) row[4], // modalityName
                                ((Number) row[5]).intValue(), // total
                                ((Number) row[6]).doubleValue(), // promedio
                                ((Number) row[7]).intValue() // lineas
                        )
                ).collect(Collectors.toList());

                // Calcular totales globales
                int totalGlobal = modalities.stream().mapToInt(PlayerModalitySummaryDTO::getTotal).sum();
                int lineasGlobal = modalities.stream().mapToInt(PlayerModalitySummaryDTO::getLineas).sum();
                double promedioGlobal = lineasGlobal > 0 ? (double) totalGlobal / lineasGlobal : 0;

                players.add(new PlayerResultSummaryDTO(
                        playerId, playerName, modalities, totalGlobal, promedioGlobal, lineasGlobal
                ));
            }
            result.put(gender, players);
        }
        return result;
    }

    /**
     * Devuelve los resultados individuales de los jugadores de un torneo/modality para tabla de resultados.
     */
    public List<PlayerResultTableDTO> getPlayerResultsForTable(Integer tournamentId, Integer modalityId) {
        List<Object[]> raw = resultRepository.findRawPlayerResultsForTable(tournamentId, modalityId);

        // Mapear por jugadorId a su DTO
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

        // Calcula el promedio
        for (PlayerResultTableDTO dto : playerMap.values()) {
            if (!dto.getScores().isEmpty()) {
                dto.setPromedio(dto.getTotal() / (double) dto.getScores().size());
            }
        }

        return new ArrayList<>(playerMap.values());
    }
    /**
     * Devuelve todos los jugadores ordenados por promedio de score descendente.
     * Útil para ranking general (ojo con listas grandes, puede paginar).
     */
    public List<PlayerRankingDTO> getAllPlayersByAvgScore() {
        return resultRepository.findAllPlayersByAvgScore();
    }

}
