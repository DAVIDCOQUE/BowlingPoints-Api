package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.UserFullService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserFullService userFullService;
    private final JwtService jwtService;

    @GetMapping("/all")
    public ResponseEntity<ResponseGenericDTO<List<UserFullDTO>>> getAllUsers() {
        List<UserFullDTO> users = userFullService.getAllUsersWithDetails();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "User list retrieved successfully", users));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseGenericDTO<UserFullDTO>> getCurrentUser(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsernameFromToken(token.replace("Bearer ", ""));

        UserFullDTO user = userFullService.getByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "User not found", null));
        }

        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "User retrieved", user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<UserFullDTO>> getUserById(@PathVariable Integer id) {
        UserFullDTO user = userFullService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseGenericDTO<>(false, "User not found", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "User retrieved", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> updateUser(@PathVariable Integer id, @RequestBody UserFullDTO input) {
        boolean updated = userFullService.updateUser(id, input);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseGenericDTO<>(false, "User not found", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "User updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> deleteUser(@PathVariable Integer id) {
        boolean deleted = userFullService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseGenericDTO<>(false, "User not found", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "User deleted successfully", null));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseGenericDTO<Void>> createUser(@RequestBody UserFullDTO input) {
        try {
            userFullService.createUser(input);
            return ResponseEntity.ok(new ResponseGenericDTO<>(true, "User created successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseGenericDTO<>(false, "Error creating user", null));
        }
    }
}
