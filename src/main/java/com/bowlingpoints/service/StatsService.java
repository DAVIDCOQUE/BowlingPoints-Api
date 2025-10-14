package com.bowlingpoints.service;

import com.bowlingpoints.dto.TopTournamentDTO;
import com.bowlingpoints.dto.UserStatisticsDTO;
import com.bowlingpoints.dto.UserStatsProjection;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ResultRepository resultRepository;
    private final PersonRepository personRepository;

    /**
     * Devuelve resumen de estadísticas generales + info básica del jugador.
     *
     * @param userId ID del jugador (personId)
     * @return UserStatisticsDTO con stats + datos personales
     */
    public UserStatisticsDTO calculateUserStats(Integer userId) {
        // Consulta stats numéricas de la BD
        UserStatsProjection projection = resultRepository.findStatsByUserId(userId);

        // Busca persona por ID
        Person person = personRepository.findById(userId).orElse(null);

        // Intenta encontrar el club principal y foto de la persona
        String clubName = null;
        String photoUrl = null;
        if (person != null) {
            // Intenta club
            if (person.getClubPersons() != null && !person.getClubPersons().isEmpty()) {
                clubName = person.getClubPersons().stream()
                        .filter(cp -> cp.getStatus() != null && cp.getStatus())
                        .map(cp -> cp.getClub().getName())
                        .findFirst()
                        .orElse(null);
            }
            // Siempre busca foto
            photoUrl = person.getPhotoUrl();
        }

        // Calcular la edad como String (o "N/A" si no hay fecha)
        String age = null;
        if (person != null && person.getBirthDate() != null) {
            age = String.valueOf(Period.between(person.getBirthDate(), LocalDate.now()).getYears());
        }

        // Arma y retorna el DTO
        return UserStatisticsDTO.builder()
                .totalTournaments(projection != null ? projection.getTotalTournaments() : 0)
                .totalStrikes(projection != null ? projection.getTotalStrikes() : 0)
                .avgScore(projection != null ? projection.getAvgScore() : 0.0)
                .bestGame(projection != null ? projection.getBestGame() : 0)
                .tournamentsWon(projection != null ? projection.getTournamentsWon() : 0)
                .personId(person != null ? person.getPersonId() : null)
                .fullName(person != null ? person.getFullName() + " " + person.getFullSurname() : null)
                .club(clubName)
                .age(age)
                .photoUrl(photoUrl)
                .build();
    }

    /**
     * Devuelve los mejores torneos jugados por el usuario.
     *
     * @param userId ID del jugador
     * @return Lista de TopTournamentDTO
     */
    public List<TopTournamentDTO> getTopTournaments(Integer userId) {
        return resultRepository.findTopTournamentsByUser(userId);
    }

}
