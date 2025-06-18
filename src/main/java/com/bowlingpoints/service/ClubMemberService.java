package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubMemberDTO;
import com.bowlingpoints.dto.ClubMemberRequestDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubMemberService {

    private final ClubPersonRepository clubPersonRepository;
    private final ClubsRepository clubsRepository;
    private final PersonRepository personRepository;

    public List<ClubMemberDTO> getMembersByClubId(Integer clubId) {
        Clubs club = clubsRepository.findById(clubId).orElseThrow(() ->
                new RuntimeException("Club no encontrado"));

        List<ClubPerson> miembros = clubPersonRepository.findByClubAndStatusIsTrue(club);

        return miembros.stream().map(cp -> ClubMemberDTO.builder()
                .personId(cp.getPerson().getPersonId())
                .fullName(cp.getPerson().getFirstName() + " " + cp.getPerson().getLastname())
                .email(cp.getPerson().getEmail())
                .roleInClub(cp.getRoleInClub())
                .joinedAt(cp.getJoinedAt())
                .build()
        ).collect(Collectors.toList());
    }

    public ClubPerson addMemberToClub(ClubMemberRequestDTO request) {
        Clubs club = clubsRepository.findById(request.getClubId())
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        Person person = personRepository.findById(request.getPersonId())
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        ClubPerson member = ClubPerson.builder()
                .club(club)
                .person(person)
                .roleInClub(request.getRoleInClub())
                .status(true)
                .joinedAt(LocalDateTime.now())
                .createdBy(1) // ID del usuario autenticado (si aplica)
                .build();

        return clubPersonRepository.save(member);
    }
}
