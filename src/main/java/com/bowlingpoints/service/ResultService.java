package com.bowlingpoints.service;

import com.bowlingpoints.dto.ResultDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<ResultDTO> getAll() {
        return resultRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ResultDTO getById(Integer id) {
        return resultRepository.findById(id)
                .map(this::mapToDTO)
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

    // 🔁 Mapear de Entity a DTO
    private ResultDTO mapEntityToDto(Result r) {
        return ResultDTO.builder()
                .resultId(r.getResultId())
                .personId(r.getPerson() != null ? r.getPerson().getPersonId() : null)
                .teamId(r.getTeam() != null ? r.getTeam().getTeamId() : null)
                .tournamentId(r.getTournament().getTournamentId())
                .roundId(r.getRound().getRoundId())
                .categoryId(r.getCategory().getCategoryId())
                .modalityId(r.getModality().getModalityId())
                .laneNumber(r.getLaneNumber())
                .lineNumber(r.getLineNumber())
                .score(r.getScore())
                .build();
    }

    // 🔁 Mapear de DTO a Entity
    private Result mapDtoToEntity(ResultDTO dto, Result result) {
        if (dto.getPersonId() != null) {
            result.setPerson(personRepository.findById(dto.getPersonId()).orElse(null));
            result.setTeam(null); // 💡 borra equipo si ahora se usa persona
        }

        if (dto.getTeamId() != null) {
            result.setTeam(teamRepository.findById(dto.getTeamId()).orElse(null));
            result.setPerson(null); // 💡 borra persona si ahora se usa equipo
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

    private ResultDTO mapToDTO(Result result) {
        return ResultDTO.builder()
                .resultId(result.getResultId())

                .personId(result.getPerson() != null ? result.getPerson().getPersonId() : null)
                .personName(result.getPerson() != null
                        ? (result.getPerson().getFullName() + " " + result.getPerson().getFullSurname()).trim()
                        : null)

                .teamId(result.getTeam() != null ? result.getTeam().getTeamId() : null)
                .teamName(result.getTeam() != null ? result.getTeam().getNameTeam() : null)

                .tournamentId(result.getTournament().getTournamentId())
                .tournamentName(result.getTournament().getName())

                .roundId(result.getRound().getRoundId())
                .roundNumber(result.getRound().getRoundNumber())

                .categoryId(result.getCategory().getCategoryId())
                .categoryName(result.getCategory().getName())

                .modalityId(result.getModality().getModalityId())
                .modalityName(result.getModality().getName())

                .laneNumber(result.getLaneNumber())
                .lineNumber(result.getLineNumber())
                .score(result.getScore())
                .build();
    }

}
