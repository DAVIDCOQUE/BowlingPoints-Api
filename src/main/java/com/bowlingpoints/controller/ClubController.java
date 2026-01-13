package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ClubDTO;
import com.bowlingpoints.dto.ClubPersonDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.ClubPersonService;
import com.bowlingpoints.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubes", description = "Gestión de clubes y sus miembros")
public class ClubController {

    private final ClubService clubService;
    private final ClubPersonService clubPersonService;

    @GetMapping
    @Operation(summary = "Obtener todos los clubes no eliminados (activos e inactivos)")
    public ResponseEntity<ResponseGenericDTO<List<ClubDTO>>> getAllClubs() {
        List<ClubDTO> clubs = clubService.getAllClubsNotDeleted();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Clubes obtenidos correctamente", clubs));
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener todos los clubes activos (no eliminados)")
    public ResponseEntity<ResponseGenericDTO<List<ClubDTO>>> getActiveClubs() {
        List<ClubDTO> clubs = clubService.getAllActiveClubs();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Clubes activos obtenidos correctamente", clubs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un club por su ID")
    public ResponseEntity<ResponseGenericDTO<ClubDTO>> getClubById(@PathVariable Integer id) {
        ClubDTO club = clubService.getClubById(id);
        if (club == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Club obtenido correctamente", club));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo club")
    public ResponseEntity<ResponseGenericDTO<Void>> createClub(@RequestBody ClubDTO input) {
        clubService.createClub(input);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Club creado correctamente", null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un club existente")
    public ResponseEntity<ResponseGenericDTO<Void>> updateClub(@PathVariable Integer id, @RequestBody ClubDTO input) {
        boolean updated = clubService.updateClub(id, input);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Club actualizado correctamente", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lógicamente un club")
    public ResponseEntity<ResponseGenericDTO<Void>> deleteClub(@PathVariable Integer id) {
        boolean deleted = clubService.deleteClub(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Club eliminado correctamente", null));
    }

    @PostMapping("/members")
    @Operation(summary = "Agregar miembro a un club")
    public ResponseEntity<?> addMemberToClub(@RequestBody ClubPersonDTO dto) {
        boolean success = clubPersonService.addMemberToClub(dto);
        if (!success) {
            return ResponseEntity.badRequest()
                    .body("No se pudo agregar el miembro. Verifica que el club y la persona existan.");
        }
        return ResponseEntity.ok("Miembro agregado correctamente al club");
    }

    @DeleteMapping("/members/{clubPersonId}")
    @Operation(summary = "Eliminar miembro de un club")
    public ResponseEntity<?> removeMemberFromClub(@PathVariable Integer clubPersonId) {
        boolean removed = clubPersonService.removeMember(clubPersonId);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Miembro eliminado correctamente del club");
    }

    @PatchMapping("/members/{clubPersonId}/role")
    @Operation(summary = "Actualizar el rol de un miembro en un club")
    public ResponseEntity<?> updateMemberRole(
            @PathVariable Integer clubPersonId,
            @RequestParam String newRole
    ) {
        boolean updated = clubPersonService.updateMemberRole(clubPersonId, newRole);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Rol del miembro actualizado correctamente");
    }
}
