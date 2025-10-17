
package com.bowlingpoints.service;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.*;
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
    private final PersonCategoryRepository personCategoryRepository;
    private final ClubPersonRepository clubPersonRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserFullDTO> getAllUsersWithDetails() {
        return userFullRepository.findAllUserFull().stream().map(proj -> {
            UserFullDTO dto = new UserFullDTO();
            dto.setUserId(proj.getUserId());
            dto.setPersonId(proj.getPersonId());
            dto.setPhotoUrl(proj.getPhotoUrl());
            dto.setNickname(proj.getNickname());
            dto.setDocument(proj.getDocument());
            dto.setEmail(proj.getEmail());
            dto.setFullName(proj.getFullName());
            dto.setFullSurname(proj.getFullSurname());
            dto.setBirthDate(proj.getBirthDate());
            dto.setPhone(proj.getPhone());
            dto.setGender(proj.getGender());

            // Roles detallados
            List<RoleDTO> roleDTOs = userRoleRepository
                    .findAllByUser_UserIdAndStatusTrue(proj.getUserId())
                    .stream()
                    .map(userRole -> {
                        Role role = userRole.getRole();
                        return new RoleDTO(role.getId(), role.getName());
                    })
                    .collect(Collectors.toList());
            dto.setRoles(roleDTOs);

            // Categorías detalladas
            List<CategoryDTO> categoryDTOs = personCategoryRepository
                    .findByPerson_PersonId(proj.getPersonId())
                    .stream()
                    .map(pc -> {
                        Category c = pc.getCategory();
                        return new CategoryDTO(c.getCategoryId(), c.getName(), c.getDescription(), c.getStatus());
                    })
                    .collect(Collectors.toList());
            dto.setCategories(categoryDTOs);

            return dto;
        }).collect(Collectors.toList());
    }

    public UserFullDTO getUserById(Integer id) {
        return getAllUsersWithDetails().stream()
                .filter(user -> user.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public UserFullDTO getByUsername(String username) {
        UserFullDTO dto = getAllUsersWithDetails().stream()
                .filter(u -> u.getNickname().equals(username))
                .findFirst()
                .orElse(null);

        if (dto != null && dto.getPersonId() != null) {
            clubPersonRepository.findFirstByPersonAndStatusIsTrue(new Person(dto.getPersonId()))
                    .ifPresent(clubPerson -> dto.setClubId(clubPerson.getClub().getClubId()));
        }

        return dto;
    }

    public void createUser(UserFullDTO input) {
        if (input.getPassword() == null || input.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (input.getNickname() == null || input.getNickname().isBlank()) {
            throw new IllegalArgumentException("El nickname no puede estar vacío");
        }

        // Guardar persona
        Person person = Person.builder()
                .photoUrl(input.getPhotoUrl() != null ? input.getPhotoUrl() : "/uploads/users/default.png")
                .document(input.getDocument())
                .fullName(input.getFullName())
                .fullSurname(input.getFullSurname())
                .birthDate(input.getBirthDate())
                .email(input.getEmail())
                .phone(input.getPhone())
                .gender(input.getGender())
                .status(true)
                .build();
        personRepository.save(person);

        // Guardar usuario
        User user = User.builder()
                .nickname(input.getNickname())
                .password(passwordEncoder.encode(input.getPassword()))
                .status(true)
                .person(person)
                .build();
        userRepository.save(user);

        // Asignar roles
        if (input.getRoles() != null) {
            input.getRoles().forEach(roleDTO ->
                    roleRepository.findByName(roleDTO.getName()).ifPresent(role -> {
                        UserRole userRole = new UserRole();
                        userRole.setUser(user);
                        userRole.setRole(role);
                        userRole.setStatus(true);
                        userRoleRepository.save(userRole);
                    })
            );
        }

        // Asignar categorías
        if (input.getCategories() != null) {
            input.getCategories().forEach(catDTO -> {
                PersonCategory pc = PersonCategory.builder()
                        .person(person)
                        .category(new Category(catDTO.getCategoryId()))
                        .build();
                personCategoryRepository.save(pc);
            });
        }
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
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        userRoleRepository.deleteByUser_UserId(user.getUserId());
        if (input.getRoles() != null) {
            input.getRoles().forEach(roleDTO ->
                    roleRepository.findByName(roleDTO.getName()).ifPresent(role -> {
                        UserRole userRole = new UserRole();
                        userRole.setUser(user);
                        userRole.setRole(role);
                        userRole.setStatus(true);
                        userRoleRepository.save(userRole);
                    })
            );
        }

        personCategoryRepository.deleteAllByPerson_PersonId(person.getPersonId());
        if (input.getCategories() != null) {
            input.getCategories().forEach(catDTO -> {
                PersonCategory pc = PersonCategory.builder()
                        .person(person)
                        .category(new Category(catDTO.getCategoryId()))
                        .build();
                personCategoryRepository.save(pc);
            });
        }

        return true;
    }

    public boolean deleteUser(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setStatus(false);
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(LocalDateTime.now());

        Person person = user.getPerson();
        person.setStatus(false);
        person.setUpdatedAt(LocalDateTime.now());
        person.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
        personRepository.save(person);

        return true;
    }
}
