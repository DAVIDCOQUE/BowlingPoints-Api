package com.bowlingpoints.service;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.projection.UserFullProjection;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Obtiene todos los usuarios con información detallada (persona, roles, categorías, club).
     */
    public List<UserFullDTO> getAllUsersWithDetails() {
        Map<Integer, UserFullDTO> userMap = new LinkedHashMap<>();
        List<UserFullProjection> projections = userFullRepository.findAllUserFull();

        // Construcción base con datos personales + roles
        for (UserFullProjection proj : projections) {
            Integer userId = proj.getUserId();

            UserFullDTO dto = userMap.computeIfAbsent(userId, id -> {
                UserFullDTO u = new UserFullDTO();
                u.setUserId(id);
                u.setPersonId(proj.getPersonId());
                u.setNickname(proj.getNickname());
                u.setDocument(proj.getDocument());
                u.setEmail(proj.getEmail());
                u.setFullName(proj.getFullName());
                u.setFullSurname(proj.getFullSurname());
                u.setBirthDate(proj.getBirthDate());
                u.setPhone(proj.getPhone());
                u.setGender(proj.getGender());
                u.setPhotoUrl(proj.getPhotoUrl());
                u.setStatus(proj.getStatus());
                u.setRoles(new ArrayList<>());
                u.setCategories(new ArrayList<>());
                return u;
            });

            // Añadir roles (id + nombre)
            Integer roleId = proj.getRoleId();
            String roleName = proj.getRoleName();

            if (roleName != null && dto.getRoles().stream().noneMatch(r -> r.getName().equals(roleName))) {
                dto.getRoles().add(new RoleDTO(roleId, roleName));
            }
        }

        // Cargar categorías y club
        for (UserFullDTO dto : userMap.values()) {
            // Categorías
            List<CategoryDTO> categoryDTOs = personCategoryRepository
                    .findByPerson_PersonId(dto.getPersonId())
                    .stream()
                    .filter(PersonCategory::getStatus)
                    .map(pc -> {
                        Category c = pc.getCategory();
                        return new CategoryDTO(c.getCategoryId(), c.getName(), c.getDescription(), c.getStatus());
                    })
                    .collect(Collectors.toList());
            dto.setCategories(categoryDTOs);

            // Club (si aplica)
            clubPersonRepository.findFirstByPerson_PersonIdAndStatusTrue(dto.getPersonId())
                    .ifPresent(clubPerson -> dto.setClubId(clubPerson.getClub().getClubId()));
        }

        return new ArrayList<>(userMap.values());
    }


    /**
     * Obtiene todos los usuarios activos (sin importar el rol).
     */
    public List<UserFullDTO> getAllActiveUsers() {
        return getAllUsersWithDetails().stream()
                .filter(user -> Boolean.TRUE.equals(user.getStatus())) // Solo usuarios con estado activo
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los usuarios activos con el rol "JUGADOR".
     */
    public List<UserFullDTO> getAllActivePlayers() {
        return getAllUsersWithDetails().stream()
                .filter(user -> user.getRoles() != null &&
                        user.getRoles().stream().anyMatch(role -> "JUGADOR".equalsIgnoreCase(role.getName())))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por ID, con roles, categorías y club.
     */
    public UserFullDTO getUserById(Integer id) {
        return getAllUsersWithDetails().stream()
                .filter(user -> user.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene un usuario por su nombre de usuario (nickname), con detalles completos.
     */
    public UserFullDTO getByUsername(String username) {
        return getAllUsersWithDetails().stream()
                .filter(u -> u.getNickname().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Crea un nuevo usuario junto con su persona, roles y categorías.
     */
    @Transactional
    public void createUser(UserFullDTO input) {
        if (input.getPassword() == null || input.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        String nickname = (input.getNickname() == null || input.getNickname().isBlank())
                ? input.getDocument()
                : input.getNickname();

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Se requiere nickname o documento");
        }

        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getNickname() != null && u.getNickname().equals(nickname) && u.isStatus());
        if (exists) {
            throw new IllegalArgumentException("El nickname/documento ya está en uso");
        }

        // Crear persona
        Person person = Person.builder()
                .photoUrl(Optional.ofNullable(input.getPhotoUrl()).orElse("/uploads/users/default.png"))
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

        // Crear usuario
        User user = User.builder()
                .nickname(nickname)
                .password(passwordEncoder.encode(input.getPassword()))
                .status(true)
                .person(person)
                .build();
        userRepository.save(user);

        // Asignar roles
        if (input.getRoles() != null) {
            input.getRoles().forEach(roleDTO ->
                    roleRepository.findById(roleDTO.getRoleId()).ifPresent(role -> {
                        UserRole ur = UserRole.builder()
                                .user(user)
                                .role(role)
                                .status(true)
                                .build();
                        userRoleRepository.save(ur);
                    }));
        }

        // Asignar categorías
        if (input.getCategories() != null) {
            input.getCategories().forEach(catDTO -> {
                PersonCategory pc = PersonCategory.builder()
                        .person(person)
                        .category(new Category(catDTO.getCategoryId()))
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                personCategoryRepository.save(pc);
            });
        }
    }

    /**
     * Actualiza un usuario existente, incluyendo sus roles y categorías.
     */
    @Transactional
    public boolean updateUser(Integer id, UserFullDTO input) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        Person person = user.getPerson();

        // Actualizar persona
        person.setPhotoUrl(input.getPhotoUrl());
        person.setDocument(input.getDocument());
        person.setFullName(input.getFullName());
        person.setFullSurname(input.getFullSurname());
        person.setBirthDate(input.getBirthDate());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());
        personRepository.save(person);

        // Validar y actualizar nickname
        String newNickname = (input.getNickname() != null && !input.getNickname().isBlank())
                ? input.getNickname()
                : input.getDocument();

        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getNickname() != null &&
                        u.getNickname().equals(newNickname) &&
                        u.isStatus() &&
                        u.getUserId() != user.getUserId());
        if (exists) {
            throw new IllegalArgumentException("El nickname/documento ya está en uso");
        }

        user.setNickname(newNickname);

        user.setStatus(input.getStatus() != null ? input.getStatus() : user.isStatus());

        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Actualizar roles
        userRoleRepository.deleteByUser_UserId(user.getUserId());
        if (input.getRoles() != null) {
            input.getRoles().forEach(roleDTO ->
                    roleRepository.findById(roleDTO.getRoleId()).ifPresent(role -> {
                        UserRole ur = UserRole.builder()
                                .user(user)
                                .role(role)
                                .status(true)
                                .build();
                        userRoleRepository.save(ur);
                    }));
        }

        // Actualizar categorías
        personCategoryRepository.deleteAllByPerson_PersonId(person.getPersonId());
        if (input.getCategories() != null) {
            input.getCategories().forEach(catDTO -> {
                PersonCategory pc = PersonCategory.builder()
                        .person(person)
                        .category(new Category(catDTO.getCategoryId()))
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                personCategoryRepository.save(pc);
            });
        }

        return true;
    }

    /**
     * Desactiva lógicamente un usuario y su persona.
     */
    @Transactional
    public boolean deleteUser(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setStatus(false);
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        Person person = user.getPerson();
        person.setStatus(false);
        person.setUpdatedAt(LocalDateTime.now());
        person.setDeletedAt(LocalDateTime.now());
        personRepository.save(person);

        // Desactivar roles
        userRoleRepository.findAllByUser_UserIdAndStatusTrue(user.getUserId())
                .forEach(ur -> {
                    ur.setStatus(false);
                    userRoleRepository.save(ur);
                });

        // Desactivar categorías
        personCategoryRepository.findByPerson_PersonId(person.getPersonId())
                .forEach(pc -> {
                    pc.setStatus(false);
                    personCategoryRepository.save(pc);
                });

        return true;
    }
}
