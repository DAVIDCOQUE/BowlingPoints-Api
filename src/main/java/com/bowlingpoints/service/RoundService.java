package com.bowlingpoints.service;

import com.bowlingpoints.dto.RoundDTO;
import com.bowlingpoints.entity.Round;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.RoundRepository;
import com.bowlingpoints.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;
    private final TournamentRepository tournamentRepository;

    public List<RoundDTO> getAll() {
        return roundRepository.findAll()
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public RoundDTO getById(Integer id) {
        return roundRepository.findById(id)
                .map(this::mapEntityToDto)
                .orElse(null);
    }

    public RoundDTO create(RoundDTO dto) {
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Round round = new Round();
        round.setTournament(tournament);
        round.setRoundNumber(dto.getRoundNumber());

        return mapEntityToDto(roundRepository.save(round));
    }

    public boolean update(Integer id, RoundDTO dto) {
        Optional<Round> existingOpt = roundRepository.findById(id);
        if (existingOpt.isEmpty()) return false;

        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Round round = existingOpt.get();
        round.setTournament(tournament);
        round.setRoundNumber(dto.getRoundNumber());

        roundRepository.save(round);
        return true;
    }

    public boolean delete(Integer id) {
        if (!roundRepository.existsById(id)) return false;
        roundRepository.deleteById(id);
        return true;
    }

    // DTO Mapper
    private RoundDTO mapEntityToDto(Round round) {
        return RoundDTO.builder()
                .roundId(round.getRoundId())
                .tournamentId(round.getTournament().getTournamentId())
                .roundNumber(round.getRoundNumber())
                .build();
    }
}
