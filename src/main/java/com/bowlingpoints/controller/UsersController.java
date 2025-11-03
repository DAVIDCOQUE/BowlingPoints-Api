package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.service.UserFullService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para la gestión de usuarios del sistema")
public class UsersController {

    private final UserFullService userFullService;
    private final JwtService jwtService;

    //  Obtener todos los usuarios
    @GetMapping
    @Operation(summary = " Obtener todos los usuarios")
    public ResponseEntity<ResponseGenericDTO<List<UserFullDTO>>> getAllUsers() {
        List<UserFullDTO> users = userFullService.getAllUsersWithDetails();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuarios obtenidos correctamente", users));
    }

    // Obtener todos los usuarios activos (sin importar rol)
    @GetMapping("/actives")
    @Operation(summary = "Obtener todos los usuarios activos")
    public ResponseEntity<ResponseGenericDTO<List<UserFullDTO>>> getActiveUsers() {
        List<UserFullDTO> activeUsers = userFullService.getAllActiveUsers();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuarios activos obtenidos correctamente", activeUsers));
    }

    //  Obtener usuario actual desde el token
    @GetMapping("/me")
    @Operation(summary = "Obtener el usuario autenticado")
    public ResponseEntity<ResponseGenericDTO<UserFullDTO>> getCurrentUser(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsernameFromToken(token.replace("Bearer ", ""));
        UserFullDTO user = userFullService.getByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Usuario no encontrado", null));
        }

        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuario obtenido correctamente", user));
    }

    // Obtener todos los usuarios activos con rol "JUGADOR"
    @GetMapping("/jugadores")
    public ResponseEntity<ResponseGenericDTO<List<UserFullDTO>>> getActivePlayers() {
        List<UserFullDTO> jugadores = userFullService.getAllActivePlayers();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Jugadores activos encontrados", jugadores));
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    @Operation(summary = " Buscar usuario por ID")
    public ResponseEntity<ResponseGenericDTO<UserFullDTO>> getUserById(@PathVariable Integer id) {
        UserFullDTO user = userFullService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Usuario no encontrado", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuario obtenido correctamente", user));
    }

    // Crear nuevo usuario
    @PostMapping
    @Operation(summary = "Crear un nuevo usuario")
    public ResponseEntity<ResponseGenericDTO<Void>> createUser(@RequestBody UserFullDTO input) {
        try {
            userFullService.createUser(input);
            return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuario creado correctamente", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseGenericDTO<>(false, "Error al crear el usuario", null));
        }
    }

    // Actualizar usuario existente
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario existente")
    public ResponseEntity<ResponseGenericDTO<Void>> updateUser(@PathVariable Integer id, @RequestBody UserFullDTO input) {
        boolean updated = userFullService.updateUser(id, input);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Usuario no encontrado", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuario actualizado correctamente", null));
    }

    //  Eliminar usuario lógicamente
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario (soft delete)")
    public ResponseEntity<ResponseGenericDTO<Void>> deleteUser(@PathVariable Integer id) {
        boolean deleted = userFullService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Usuario no encontrado", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Usuario eliminado correctamente", null));
    }
}
