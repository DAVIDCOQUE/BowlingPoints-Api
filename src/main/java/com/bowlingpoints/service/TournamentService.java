package com.bowlingpoints.service;


import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {

    @Autowired
    TournamentRepository tournamentRepository;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public List<TournamentDTO> getAllTournaments(){

        List<TournamentDTO> tournamentDTOList = new ArrayList<>();

        List<Tournament> tournamentList = tournamentRepository.findAll();

        tournamentList.forEach(tournament -> {
            String tournamentName = tournament.getTournamentName().toLowerCase();

            String category;
            String modality;

            if (tournamentName.contains("clasificatorio")) {
                category = "Abierto";
                modality = "Individual";
            } else if (tournamentName.contains("sub-21")) {
                category = "Sub-21";
                modality = "Individual";
            } else if (tournamentName.contains("senior")) {
                category = "Senior";
                modality = "Individual";
            } else {
                category = "Abierto";
                modality = "Individual";
            }

            LocalDate now = LocalDate.now();
            LocalDate start = tournament.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate end = tournament.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String status;
            if (now.isBefore(start)) {
                status = "Sin iniciar";
            } else if (!now.isAfter(end)) {
                status = "En curso";
            } else {
                status = "Finalizado";
            }

            tournamentDTOList.add(TournamentDTO.builder()
                    .tournamentName(tournament.getTournamentName())
                    .category(category)
                    .endDate(String.valueOf(tournament.getEndDate()))
                    .startDate(String.valueOf(tournament.getStartDate()))
                    .modality(modality)
                    .status(status)
                    .place(tournament.getPlace())
                    .build());
        });


        return tournamentDTOList;
    }

    public boolean updateTournament(Integer id, TournamentDTO dto) {
        Optional<Tournament> optional = tournamentRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }

        Tournament tournament = optional.get();

        // Solo actualiza si viene valor
        if (dto.getTournamentName() != null) tournament.setTournamentName(dto.getTournamentName());

        if (dto.getStartDate() != null) {
            try {
                Date start = formatter.parse(dto.getStartDate());
                tournament.setStartDate(start);
            } catch (Exception e) {
                throw new IllegalArgumentException("Fecha de inicio inválida: " + dto.getStartDate());
            }
        }

        if (dto.getEndDate() != null) {
            try {
                Date end = formatter.parse(dto.getEndDate());
                tournament.setEndDate(end);
            } catch (Exception e) {
                throw new IllegalArgumentException("Fecha de fin inválida: " + dto.getEndDate());
            }
        }

        if (dto.getPlace() != null) tournament.setPlace(dto.getPlace());

        tournamentRepository.save(tournament);
        return true;
    }

    public boolean deleteTournament(Integer id) {
        if (!tournamentRepository.existsById(id)) {
            return false;
        }
        tournamentRepository.deleteById(id);
        return true;
    }

    public Tournament saveTournament(TournamentDTO dto, Integer userId) {
        try {
            Tournament tournament = new Tournament();
            tournament.setTournamentName(dto.getTournamentName());
            tournament.setPlace(dto.getPlace());
            tournament.setCauseStatus(dto.getCauseStatus());

            if (dto.getStartDate() != null) {
                Date start = formatter.parse(dto.getStartDate());
                tournament.setStartDate(start);
            }

            if (dto.getEndDate() != null) {
                Date end = formatter.parse(dto.getEndDate());
                tournament.setEndDate(end);
            }

            tournament.setCreatedAt(new Date());
            tournament.setCreatedBy(userId);
            tournament.setStatus(true); // activo por defecto

            return tournamentRepository.save(tournament);

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al guardar torneo: " + e.getMessage());
        }
    }

}
