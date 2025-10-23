package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubPersonDTO;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.ClubPersonRepository;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubPersonService {

    private final ClubPersonRepository clubPersonRepository;
    private final ClubRepository clubRepository;
    private final PersonRepository personRepository;

    /**
     * Agrega un nuevo miembro a un club.
     */
    @Transactional
    public boolean addMemberToClub(ClubPersonDTO dto) {
        Optional<Clubs> clubOpt = clubRepository.findById(dto.getClubId());
        Optional<Person> personOpt = personRepository.findById(dto.getPersonId());

        if (clubOpt.isEmpty() || personOpt.isEmpty()) return false;

        ClubPerson clubPerson = ClubPerson.builder()
                .club(clubOpt.get())
                .person(personOpt.get())
                .roleInClub(dto.getRoleInClub())
                .joinedAt(LocalDateTime.now())
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        clubPersonRepository.save(clubPerson);
        return true;
    }

    /**
     * Actualiza el rol de un miembro en un club.
     */
    @Transactional
    public boolean updateMemberRole(Integer clubPersonId, String newRole) {
        Optional<ClubPerson> cpOpt = clubPersonRepository.findById(clubPersonId);
        if (cpOpt.isEmpty()) return false;

        ClubPerson cp = cpOpt.get();
        cp.setRoleInClub(newRole);
        cp.setUpdatedAt(LocalDateTime.now());

        clubPersonRepository.save(cp);
        return true;
    }

    /**
     * Elimina (lógicamente) un miembro de un club.
     */
    @Transactional
    public boolean removeMember(Integer clubPersonId) {
        Optional<ClubPerson> cpOpt = clubPersonRepository.findById(clubPersonId);
        if (cpOpt.isEmpty()) return false;

        ClubPerson cp = cpOpt.get();
        cp.setDeletedAt(LocalDateTime.now());
        cp.setUpdatedAt(LocalDateTime.now());

        clubPersonRepository.save(cp);
        return true;
    }
}
