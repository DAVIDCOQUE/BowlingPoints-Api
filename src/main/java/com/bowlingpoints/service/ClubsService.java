package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.ClubPersonRepository;
import com.bowlingpoints.repository.ClubsRepository;
import com.bowlingpoints.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubsService {

    private final ClubsRepository clubsRepository;
    private final PersonRepository personRepository;
    private final ClubPersonRepository clubPersonRepository;

    // ✅ Crear club con miembros
    @Transactional
    public Clubs createClubWithMembers(ClubsDTO clubsDTO) {
        Clubs club = new Clubs();
        club.setName(clubsDTO.getName());
        club.setCity(clubsDTO.getCity());
        club.setDescription(clubsDTO.getDescription());
        club.setFoundationDate(clubsDTO.getFoundationDate());
        if (clubsDTO.getImageUrl() == null || clubsDTO.getImageUrl().isBlank()) {
            club.setImageUrl("/uploads/clubs/default.png");
        } else {
            club.setImageUrl(clubsDTO.getImageUrl());
        }
        club.setStatus(Boolean.TRUE);

        List<ClubPerson> members = new ArrayList<>();
        if (clubsDTO.getMembers() != null) {
            for (ClubMemberRequestDTO memberDTO : clubsDTO.getMembers()) {
                ClubPerson cp = new ClubPerson();
                Person person = personRepository.findById(memberDTO.getPersonId())
                        .orElseThrow(() -> new RuntimeException("Persona no encontrada: " + memberDTO.getPersonId()));
                cp.setPerson(person);
                cp.setRoleInClub(memberDTO.getRoleInClub());
                cp.setJoinedAt(LocalDateTime.now());
                cp.setClub(club);
                members.add(cp);
            }
        }

        club.setMembers(members);
        return clubsRepository.save(club);
    }

    // ✅ Obtener un club por ID con miembros
    public ClubDetailsDTO getClubDetails(Integer clubId) {
        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado con ID: " + clubId));

        List<ClubMemberDTO> members = club.getMembers().stream()
                .map(ClubMemberDTO::from) // 👈 Usamos el método estático limpio
                .toList();

        return ClubDetailsDTO.builder()
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

    // ✅ Obtener todos los clubes activos con sus miembros
    public List<ClubDetailsDTO> getAllClubsWithMembers() {
        return clubsRepository.findAll().stream()
                .filter(club -> Boolean.TRUE.equals(club.getStatus()))
                .map(club -> {
                    List<ClubMemberDTO> members = club.getMembers().stream()
                            .map(ClubMemberDTO::from) // 👈 También aquí
                            .toList();

                    return ClubDetailsDTO.builder()
                            .clubId(club.getClubId())
                            .name(club.getName())
                            .foundationDate(club.getFoundationDate())
                            .city(club.getCity())
                            .description(club.getDescription())
                            .imageUrl(club.getImageUrl())
                            .status(club.getStatus())
                            .members(members)
                            .build();
                }).toList();
    }

    // ✅ Actualizar un club
    @Transactional
    public void updateClubWithMembers(Integer clubId, ClubsDTO dto) {
        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("❌ Club no encontrado con ID " + clubId));

        // 1. Actualiza campos básicos
        club.setName(dto.getName());
        club.setCity(dto.getCity());
        club.setDescription(dto.getDescription());
        club.setFoundationDate(dto.getFoundationDate());
        club.setImageUrl(dto.getImageUrl());
        club.setStatus(dto.getStatus());
        club.setUpdatedAt(LocalDateTime.now());

        // 2. Miembros actuales en BD
        List<ClubPerson> currentMembers = new ArrayList<>(club.getMembers());
        // Para búsqueda rápida
        var currentByPersonId = currentMembers.stream()
                .collect(Collectors.toMap(cp -> cp.getPerson().getPersonId(), cp -> cp));

        // 3. IDs de los nuevos miembros
        var incomingIds = dto.getMembers().stream()
                .map(ClubMemberRequestDTO::getPersonId)
                .collect(Collectors.toSet());

        // 4. Elimina miembros que ya no están
        for (ClubPerson oldMember : currentMembers) {
            if (!incomingIds.contains(oldMember.getPerson().getPersonId())) {
                clubPersonRepository.delete(oldMember);
            }
        }

        // 5. Inserta o actualiza miembros nuevos
        for (ClubMemberRequestDTO m : dto.getMembers()) {
            ClubPerson existing = currentByPersonId.get(m.getPersonId());
            if (existing != null) {
                // Si el rol cambió, actualízalo
                if (!existing.getRoleInClub().equals(m.getRoleInClub())) {
                    existing.setRoleInClub(m.getRoleInClub());
                    clubPersonRepository.save(existing);
                }
            } else {
                // Nuevo miembro
                Person person = personRepository.findById(m.getPersonId())
                        .orElseThrow(() -> new RuntimeException("❌ Persona no encontrada con ID " + m.getPersonId()));
                ClubPerson nuevo = ClubPerson.builder()
                        .club(club)
                        .person(person)
                        .roleInClub(m.getRoleInClub())
                        .joinedAt(LocalDateTime.now())
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                clubPersonRepository.save(nuevo);
            }
        }
        clubsRepository.save(club);
    }


    // ✅ Eliminar lógicamente un club
    public boolean deleteClub(Integer id) {
        return clubsRepository.findById(id).map(club -> {
            club.setStatus(false);
            club.setDeletedAt(LocalDateTime.now());
            clubsRepository.save(club);
            return true;
        }).orElse(false);
    }
}
