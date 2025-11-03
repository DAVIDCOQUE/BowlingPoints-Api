package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubDTO;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubPersonRepository clubPersonRepository;
    private final PersonRepository personRepository;

    /**
     * Crear un nuevo club junto con sus miembros opcionales.
     */
    @Transactional
    public void createClub(ClubDTO input) {
        Clubs club = Clubs.builder()
                .name(input.getName())
                .foundationDate(input.getFoundationDate())
                .city(input.getCity())
                .description(input.getDescription())
                .imageUrl(input.getImageUrl())
                .status(input.getStatus() != null ? input.getStatus() : true)
                .build();

        clubRepository.save(club);

        if (input.getMembers() != null) {
            for (ClubPersonDTO dto : input.getMembers()) {
                personRepository.findById(dto.getPersonId()).ifPresent(person -> {
                    ClubPerson cp = ClubPerson.builder()
                            .club(club)
                            .person(person)
                            .roleInClub(dto.getRoleInClub())
                            .status(true)
                            .joinedAt(LocalDateTime.now())
                            .build();
                    clubPersonRepository.save(cp);
                });
            }
        }
    }

    /**
     * Obtener todos los clubes no eliminados (sin importar si están activos o inactivos).
     */
    public List<ClubDTO> getAllClubsNotDeleted() {
        return clubRepository.findAll().stream()
                .filter(club -> club.getDeletedAt() == null)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los clubes activos (no eliminados y con status true).
     */
    public List<ClubDTO> getAllActiveClubs() {
        return clubRepository.findAll().stream()
                .filter(club -> club.getDeletedAt() == null && Boolean.TRUE.equals(club.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un club por su ID (incluye sus miembros).
     */
    public ClubDTO getClubById(Integer id) {
        return clubRepository.findById(id)
                .filter(club -> club.getDeletedAt() == null)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Actualizar un club y sus miembros opcionalmente.
     */
    @Transactional
    public boolean updateClub(Integer id, ClubDTO input) {
        Optional<Clubs> clubOpt = clubRepository.findById(id);
        if (clubOpt.isEmpty()) return false;

        Clubs club = clubOpt.get();

        club.setName(input.getName());
        club.setFoundationDate(input.getFoundationDate());
        club.setCity(input.getCity());
        club.setDescription(input.getDescription());
        club.setImageUrl(input.getImageUrl());
        club.setStatus(input.getStatus());
        club.setUpdatedAt(LocalDateTime.now());

        clubRepository.save(club);

        // Eliminar antiguos y agregar nuevos miembros
        clubPersonRepository.deleteAllByClub_ClubId(club.getClubId());

        if (input.getMembers() != null) {
            for (ClubPersonDTO dto : input.getMembers()) {
                personRepository.findById(dto.getPersonId()).ifPresent(person -> {
                    ClubPerson cp = ClubPerson.builder()
                            .club(club)
                            .person(person)
                            .roleInClub(dto.getRoleInClub())
                            .status(true)
                            .joinedAt(LocalDateTime.now())
                            .build();
                    clubPersonRepository.save(cp);
                });
            }
        }

        return true;
    }

    /**
     * Eliminar lógicamente un club (y sus relaciones activas).
     */
    @Transactional
    public boolean deleteClub(Integer id) {
        Optional<Clubs> clubOpt = clubRepository.findById(id);
        if (clubOpt.isEmpty()) return false;

        Clubs club = clubOpt.get();
        club.setDeletedAt(LocalDateTime.now());
        club.setUpdatedAt(LocalDateTime.now());
        clubRepository.save(club);

        // Desactivar también sus miembros
        clubPersonRepository.findAllByClub_ClubIdAndDeletedAtIsNull(club.getClubId())
                .forEach(cp -> {
                    cp.setDeletedAt(LocalDateTime.now());
                    cp.setUpdatedAt(LocalDateTime.now());
                    clubPersonRepository.save(cp);
                });

        return true;
    }

    /**
     * Convertir entidad Clubs a DTO con sus miembros.
     */
    private ClubDTO convertToDTO(Clubs club) {
        List<ClubPersonDTO> members = club.getMembers().stream()
                .filter(m -> m.getDeletedAt() == null && m.getStatus())
                .map(cp -> {
                    Person p = cp.getPerson();
                    return ClubPersonDTO.builder()
                            .clubPersonId(cp.getId())
                            .personId(p.getPersonId())
                            .roleInClub(cp.getRoleInClub())
                            .fullName(p.getFullName())
                            .fullSurname(p.getFullSurname())
                            .document(p.getDocument())
                            .email(p.getEmail())
                            .phone(p.getPhone())
                            .gender(p.getGender())
                            .photoUrl(p.getPhotoUrl())
                            .build();
                }).collect(Collectors.toList());

        return ClubDTO.builder()
                .clubId(club.getClubId())
                .name(club.getName())
                .foundationDate(club.getFoundationDate())
                .city(club.getCity())
                .description(club.getDescription())
                .imageUrl(club.getImageUrl())
                .status(club.getStatus())
                .members(members)
                .build();
    }
}
