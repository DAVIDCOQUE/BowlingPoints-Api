package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.repository.ClubsRepository;
import com.bowlingpoints.service.ClubMemberService;
import com.bowlingpoints.service.ClubsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubsController {

    private final ClubsService clubsService;
    private final ClubMemberService clubMemberService;
    private final ClubsRepository clubsRepository;

    // ✅ Obtener todos los clubes activos con sus miembros
    @GetMapping("/with-members")
    public ResponseEntity<List<ClubDetailsDTO>> getAllClubsWithMembers() {
        List<ClubDetailsDTO> clubs = clubsService.getAllClubsWithMembers();
        return ResponseEntity.ok(clubs);
    }

    // ✅ Obtener un club por ID con sus miembros
    @GetMapping("/{id}/details")
    public ResponseEntity<ClubDetailsDTO> getClubDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(clubsService.getClubDetails(id));
    }

    // ✅ Crear un nuevo club con miembros
    @PostMapping("/create-with-members")
    public ResponseEntity<ClubDetailsDTO> createClubWithMembers(@RequestBody ClubsDTO clubsDTO) {
        Clubs club = clubsService.createClubWithMembers(clubsDTO);
        ClubDetailsDTO dto = clubsService.getClubDetails(club.getClubId());
        return ResponseEntity.ok(dto);
    }

    // ✅ Actualizar un club (solo campos del club, no miembros)
    @PutMapping("/{id}")
    public ResponseEntity<Clubs> updateClub(@PathVariable Integer id, @RequestBody CreateClubRequestDTO dto) {
        return clubsRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setCity(dto.getCity());
                    existing.setFoundationDate(dto.getFoundationDate());
                    existing.setDescription(dto.getDescription());
                    existing.setStatus(dto.getStatus());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(clubsRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Eliminar lógicamente un club
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Integer id) {
        boolean deleted = clubsService.deleteClub(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // ✅ Obtener miembros de un club por ID
    @GetMapping("/{id}/members")
    public ResponseEntity<List<ClubMemberDTO>> getClubMembers(@PathVariable Integer id) {
        List<ClubMemberDTO> members = clubMemberService.getMembersByClubId(id);
        return ResponseEntity.ok(members);
    }

    // ✅ Agregar miembro a un club (individual)
    @PostMapping("/{clubId}/members")
    public ResponseEntity<?> addMemberToClub(@PathVariable Integer clubId, @RequestBody ClubMemberRequestDTO request) {
        request.setClubId(clubId);
        var created = clubMemberService.addMemberToClub(request);
        return ResponseEntity.ok(created);
    }
}
