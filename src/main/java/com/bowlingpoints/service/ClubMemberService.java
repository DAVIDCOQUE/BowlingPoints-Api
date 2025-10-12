package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubMemberDTO;
import com.bowlingpoints.dto.ClubMemberRequestDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;

import com.bowlingpoints.repository.ClubPersonRepository;
import com.bowlingpoints.repository.ClubsRepository;
import com.bowlingpoints.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMemberService {

    private final ClubPersonRepository clubPersonRepository;
    private final ClubsRepository clubsRepository;
    private final PersonRepository personRepository;

    public ResponseGenericDTO<List<ClubMemberDTO>> getMembersByClubId(Integer clubId) {
        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club no encontrado con ID: " + clubId));

        List<ClubPerson> miembros = clubPersonRepository.findByClubAndStatusIsTrue(club);
        if (miembros == null || miembros.isEmpty()) {
            return new ResponseGenericDTO<>(true, "El club no tiene miembros activos", List.of());
        }
        List<ClubMemberDTO> dtos = miembros.stream().map(ClubMemberDTO::from).toList();
        return new ResponseGenericDTO<>(true, "Miembros cargados correctamente", dtos);
    }

    public ClubPerson addMemberToClub(ClubMemberRequestDTO request) {
        Clubs club = clubsRepository.findById(request.getClubId())
                .orElseThrow(() -> new RuntimeException("Club no encontrado con ID: " + request.getClubId()));

        Person person = personRepository.findById(request.getPersonId())
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        // 👇 Verifica si ya existe la relación club-persona
        if (clubPersonRepository.findByClubAndPerson(club, person).isPresent()) {
            throw new RuntimeException("El miembro ya está asignado a este club.");
        }

        ClubPerson member = ClubPerson.builder()
                .club(club)
                .person(person)
                .roleInClub(request.getRoleInClub())
                .status(true)
                .joinedAt(LocalDateTime.now())
                .createdBy(1)
                .build();

        return clubPersonRepository.save(member);
    }
}
