package com.bowlingpoints.config;

import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initData(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PersonRepository personRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            ClubsRepository clubRepository,
            ClubPersonRepository clubPersonRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println("🔧 [Seeder] Iniciando carga de datos...");

            // 1. Crear roles globales
            createRoleIfMissing("ADMIN", roleRepository);
            createRoleIfMissing("ENTRENADOR", roleRepository);
            createRoleIfMissing("JUGADOR", roleRepository);

            // 2. Crear permisos base
            createPermissionIfMissing("VER_DASHBOARD", "Acceso al tablero", permissionRepository);
            createPermissionIfMissing("VER_PERFIL", "Acceso al perfil", permissionRepository);
            createPermissionIfMissing("VER_MIS_TORNEOS", "Consulta torneos", permissionRepository);
            createPermissionIfMissing("GESTIONAR_CLUBES", "Administra clubes", permissionRepository);
            createPermissionIfMissing("VER_USUARIOS", "Gestiona usuarios", permissionRepository);

            // 3. Asignar permisos a ADMIN
            Role adminRole = roleRepository.findByDescription("ADMIN")
                    .orElseThrow(() -> new RuntimeException("❌ Rol ADMIN no encontrado"));

            permissionRepository.findAll().forEach(permission -> {
                if (!rolePermissionRepository.existsByRoleAndPermission(adminRole, permission)) {
                    rolePermissionRepository.save(RolePermission.builder()
                            .role(adminRole)
                            .permission(permission)
                            .granted(true)
                            .build());
                }
            });

            // 4. Crear usuarios
            User admin = createUserIfNotExists("davidcoque", "David", "Armando", "Sánchez", "Sanchez",
                    "david03sc@gmail.com", "ADMIN", passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository);

            User entrenador = createUserIfNotExists("jhon", "Jhon", "Elena", "Martínez", "Pérez",
                    "jhon@gmail.com", "ENTRENADOR", passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository);

            User jugador = createUserIfNotExists("sara", "Sara", null, "Pérez", null,
                    "sara@gmail.com", "JUGADOR", passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository);

            // 5. Crear club
            if (clubRepository.count() == 0) {
                System.out.println("📌 Creando club 'Bowling Club Central'...");
                Clubs  club = Clubs.builder()
                        .name("Bowling Club Central")
                        .description("Club principal de la ciudad")
                        .foundationDate(LocalDate.of(2020, 1, 1))
                        .city("Ciudad Bowling")
                        .imageUrl("/uploads/clubs/default.png")
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                clubRepository.save(club);

                // Agregar miembros al club
                clubPersonRepository.save(ClubPerson.builder()
                        .club(club)
                        .person(entrenador.getPerson())
                        .roleInClub("ENTRENADOR")
                        .joinedAt(LocalDateTime.now())
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build());

                clubPersonRepository.save(ClubPerson.builder()
                        .club(club)
                        .person(jugador.getPerson())
                        .roleInClub("JUGADOR")
                        .joinedAt(LocalDateTime.now())
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build());

                System.out.println("✅ Club creado con miembros.");
            }

            System.out.println("🎉 [Seeder] Carga inicial completada.");
        };
    }

    private void createRoleIfMissing(String name, RoleRepository roleRepository) {
        roleRepository.findByDescription(name).orElseGet(() ->
                roleRepository.save(Role.builder().description(name).build())
        );
    }

    private void createPermissionIfMissing(String name, String description, PermissionRepository repository) {
        repository.findByName(name).orElseGet(() ->
                repository.save(Permission.builder().name(name).description(description).build())
        );
    }

    private User createUserIfNotExists(String nickname, String firstName, String secondName,
                                       String lastname, String secondLastname, String email,
                                       String roleDescription, PasswordEncoder passwordEncoder,
                                       PersonRepository personRepository, UserRepository userRepository,
                                       UserRoleRepository userRoleRepository, RoleRepository roleRepository) {

        Optional<User> existingUser = userRepository.findByNickname(nickname);
        if (existingUser.isPresent()) {
            System.out.printf("🔁 Usuario '%s' ya existe, se omite.%n", nickname);
            return existingUser.get();
        }

        System.out.printf("📌 Creando usuario '%s'...%n", nickname);

        Person person = Person.builder()
                .firstName(firstName)
                .secondName(secondName)
                .lastname(lastname)
                .secondLastname(secondLastname)
                .gender("Masculino")
                .email(email)
                .phone("3100000000")
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();
        personRepository.save(person);

        User user = User.builder()
                .nickname(nickname)
                .password(passwordEncoder.encode("admin"))
                .status(true)
                .attemptsLogin(0)
                .lastLoginAt(LocalDateTime.now())
                .person(person)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        Role role = roleRepository.findByDescription(roleDescription)
                .orElseThrow(() -> new RuntimeException("❌ Rol no encontrado: " + roleDescription));

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .status(true)
                .createdAt(LocalDateTime.now())
                .build());

        System.out.printf("✅ Usuario '%s' creado con rol %s.%n", nickname, roleDescription);
        return user;
    }
}
