package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.repository.ClubsRepository;
import com.bowlingpoints.service.ClubMemberService;
import com.bowlingpoints.service.ClubsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    // ✅ Actualizar un club
    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> updateClub(@PathVariable Integer id, @RequestBody ClubsDTO dto) {
        try {
            clubsService.updateClubWithMembers(id, dto);
            return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Club actualizado correctamente", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, e.getMessage(), null));
        }
    }

    // ✅ Eliminar lógicamente un club
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Integer id) {
        boolean deleted = clubsService.deleteClub(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // ✅ Obtener miembros de un club por ID
    @GetMapping("/{id}/members")
    public ResponseGenericDTO<List<ClubMemberDTO>> getClubMembers(@PathVariable Integer id) {

        return clubMemberService.getMembersByClubId(id);
    }

    // ✅ Agregar miembro a un club (individual)
    @PostMapping("/{clubId}/members")
    public ResponseEntity<?> addMemberToClub(@PathVariable Integer clubId, @RequestBody ClubMemberRequestDTO request) {
        request.setClubId(clubId);
        var created = clubMemberService.addMemberToClub(request);
        return ResponseEntity.ok(created);
    }
}
