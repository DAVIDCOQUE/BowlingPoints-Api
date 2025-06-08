package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.entity.UserRole;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.RoleRepository;
import com.bowlingpoints.repository.UserFullRepository;
import com.bowlingpoints.repository.UserRepository;
import com.bowlingpoints.repository.UserRoleRepository;
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

    public List<UserFullDTO> getAllUsersWithDetails() {
        return userFullRepository.getUserFullInfoRaw().stream()
                .map(obj -> {
                    UserFullDTO dto = new UserFullDTO();
                    dto.setUserId((Integer) obj[0]);
                    dto.setNickname((String) obj[1]);
                    dto.setEmail((String) obj[2]);
                    dto.setFirstname((String) obj[3]);
                    dto.setSecondname((String) obj[4]);
                    dto.setLastname((String) obj[5]);
                    dto.setSecondlastname((String) obj[6]);
                    dto.setPhone((String) obj[7]);
                    dto.setGender((String) obj[8]);
                    dto.setRoleDescription((String) obj[9]);
                    dto.setRoles(null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public UserFullDTO getUserById(Integer id) {
        return getAllUsersWithDetails().stream()
                .filter(user -> user.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean updateUser(Integer id, UserFullDTO input) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        Person person = user.getPerson();

        // Actualiza los datos personales
        person.setFirstName(input.getFirstname());
        person.setSecondName(input.getSecondname());
        person.setLastname(input.getLastname());
        person.setSecondLastname(input.getSecondlastname());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());
        personRepository.save(person);

        // Actualiza el nickname
        user.setNickname(input.getNickname());

        // 🔐 Si viene contraseña, actualizarla codificada
        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    public boolean deleteUser(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setStatus(false);
        user.setUpdatedAt(LocalDateTime.now());

        Person person = user.getPerson();
        person.setStatus(false);
        person.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        personRepository.save(person);

        return true;
    }

    public void createUser(UserFullDTO input) {
        // Crear persona
        Person person = new Person();
        person.setFirstName(input.getFirstname());
        person.setSecondName(input.getSecondname());
        person.setLastname(input.getLastname());
        person.setSecondLastname(input.getSecondlastname());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());
        person.setStatus(true);
        personRepository.save(person);

        // Crear usuario
        User user = new User();
        user.setNickname(input.getNickname());
        user.setPerson(person);

        // Aquí encriptamos la contraseña recibida
        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        } else {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        user.setStatus(true);
        userRepository.save(user);

        // Asociar roles
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
    }
}
