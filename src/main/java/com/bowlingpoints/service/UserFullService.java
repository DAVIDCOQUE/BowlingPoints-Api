package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.entity.UserRole;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFullService {

    private final UserFullRepository userFullRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClubPersonRepository clubPersonRepository;
    private final EmailService emailService;

    public List<UserFullDTO> getAllUsersWithDetails() {
        return userFullRepository.getUserFullInfoRaw().stream().map(obj -> {
            UserFullDTO dto = new UserFullDTO();
            dto.setUserId((Integer) obj[0]);
            dto.setPersonId((Integer) obj[1]);
            dto.setPhotoUrl((String) obj[2]);
            dto.setNickname((String) obj[3]);
            dto.setDocument((String) obj[4]);
            dto.setEmail((String) obj[5]);
            dto.setFullName((String) obj[6]);
            dto.setFullSurname((String) obj[7]);
            dto.setBirthDate(obj[8] != null ? ((java.sql.Date) obj[8]).toLocalDate() : null); // birthDate
            dto.setPhone((String) obj[9]);
            dto.setGender((String) obj[10]);
            dto.setRoleDescription((String) obj[11]);
            dto.setRoles(null);
            return dto;
        }).collect(Collectors.toList());
    }

    public UserFullDTO getByUsername(String username) {
        UserFullDTO dto = getAllUsersWithDetails().stream().filter(u -> u.getNickname().equals(username)).findFirst().orElse(null);

        // Si existe el usuario, buscamos su club activo
        if (dto != null && dto.getPersonId() != null) {
            clubPersonRepository.findFirstByPersonAndStatusIsTrue(new Person(dto.getPersonId()) // Constructor mínimo, solo con el ID
            ).ifPresent(clubPerson -> dto.setClubId(clubPerson.getClub().getClubId()));
        }
        return dto;
    }

    public UserFullDTO getUserById(Integer id) {
        return getAllUsersWithDetails().stream().filter(user -> user.getUserId().equals(id)).findFirst().orElse(null);
    }

    public boolean updateUser(Integer id, UserFullDTO input) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        Person person = user.getPerson();

        person.setPhotoUrl(input.getPhotoUrl());
        person.setDocument(input.getDocument());
        person.setFullName(input.getFullName());
        person.setFullSurname(input.getFullSurname());
        person.setBirthDate(input.getBirthDate());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());
        personRepository.save(person);

        user.setNickname(input.getNickname());

        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        // Manejo de roles igual...

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }


    public boolean deleteUser(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setStatus(false); // 🟡 Opcional: marcar como inactivo

        // 🔴 Soft delete en User
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(LocalDateTime.now()); // ⬅️ Asegúrate de tener este campo en la entidad

        // 🔴 Soft delete en Person
        Person person = user.getPerson();
        person.setStatus(false); // Opcional también
        person.setUpdatedAt(LocalDateTime.now());
        person.setDeletedAt(LocalDateTime.now()); // ⬅️ Necesitas este campo también

        // Guardar los cambios
        userRepository.save(user);
        personRepository.save(person);

        return true;
    }

    public void createUser(UserFullDTO input) {

        // ✅ VALIDACIONES PRIMERO - antes de guardar nada
        if (input.getPassword() == null || input.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (input.getNickname() == null || input.getNickname().isBlank()) {
            throw new IllegalArgumentException("El nickname no puede estar vacío");
        }

        // Guardar persona
        Person person = new Person();
        if (input.getPhotoUrl() == null || input.getPhotoUrl().isBlank()) {
            person.setPhotoUrl("/uploads/users/default.png");
        } else {
            person.setPhotoUrl(input.getPhotoUrl());
        }
        person.setDocument(input.getDocument());
        person.setFullName(input.getFullName());
        person.setFullSurname(input.getFullSurname());
        person.setBirthDate(input.getBirthDate());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());
        person.setStatus(true);
        personRepository.save(person);

        // Guardar usuario
        User user = new User();
        user.setNickname(input.getNickname());
        user.setPerson(person);
        user.setPassword(passwordEncoder.encode(input.getPassword())); // Ya validado arriba
        user.setStatus(true);
        userRepository.save(user);

        // Guardar roles
        if (input.getRoles() != null && !input.getRoles().isEmpty()) {
            for (String roleName : input.getRoles()) {
                Optional<Role> role = roleRepository.findByDescription(roleName);
                role.ifPresent(r -> {
                    UserRole userRole = new UserRole();
                    userRole.setUser(user);
                    userRole.setRole(r);
                    userRole.setStatus(true);
                    userRoleRepository.save(userRole);
                });
            }
        }

        // Enviar correo al usuario
//    try {
//        String html = "<h2>¡Bienvenido a BowlingPoints!</h2>" +
//                      "<p>Tu usuario ha sido creado exitosamente.</p>" +
//                      "<p><b>Usuario:</b> " + input.getNickname() + "</p>" +
//                      "<p><b>Contraseña:</b> " + input.getPassword() + "</p>";
//
//        emailService.sendHtmlMessage(input.getEmail(), "Cuenta creada", html);
//    } catch (Exception e) {
//        System.err.println("⚠️ Error enviando correo: " + e.getMessage());
//    }
    }
}