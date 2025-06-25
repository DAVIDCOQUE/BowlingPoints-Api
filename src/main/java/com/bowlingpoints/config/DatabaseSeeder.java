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
import java.util.Arrays;
import java.util.List;
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
            CategoryRepository categoryRepository,
            ModalityRepository modalityRepository,
            AmbitRepository ambitRepository,
            PasswordEncoder passwordEncoder,
            TournamentRepository tournamentRepository,
            TournamentModalityRepository tournamentModalityRepository,
            TournamentCategoryRepository tournamentCategoryRepository
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
            User admin = createUserIfNotExists(
                    "davidcoque", "/uploads/users/default.png", "1143993925",
                    "David Armando", "Sánchez Sánchez", LocalDate.of(1993, 4, 12),
                    "david03sc@gmail.com", "Masculino", "ADMIN",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

            User entrenador = createUserIfNotExists(
                    "jhonsoto", "/uploads/users/default.png", "198445652",
                    "Jhon", "Soto", LocalDate.of(1980, 7, 22),
                    "jhon@gmail.com", "Masculino", "ENTRENADOR",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

            User jugador = createUserIfNotExists(
                    "saraarteaga", "/uploads/users/default.png", "11455625",
                    "Sara", "Arteaga", LocalDate.of(2002, 1, 7),
                    "sara@gmail.com", "Femenino", "JUGADOR",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );
            // 5. Crear club
            if (clubRepository.count() == 0) {
                System.out.println("📌 Creando club 'Bowling Club Central'...");
                Clubs club = Clubs.builder()
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

            // 6. Crear categorías
            String[] categorias = {
                    "Masculina",
                    "Femenina",
                    "Mixto",
                    "Sub 8",
                    "Sub 10",
                    "Sub 12",
                    "Sub 14",
                    "Sub 16",
                    "Sub 18",
                    "Sub 21",
                    "Sub 23",
                    "Mayores",
                    "Sénior",
                    "Super Sénior",
                    "Master"
            };

            for (String nombre : categorias) {
                if (!categoryRepository.findByName(nombre).isPresent()) {
                    categoryRepository.save(Category.builder()
                            .name(nombre)
                            .description("Categoría " + nombre)
                            .status(true)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }

            // 7. Crear modalidades
            String[] modalidades = {
                    "Individual",
                    "Parejas",
                    "Ternas",
                    "Equipos (cuartetos)",
                    "Equipos (quintetos)",
                    "Todo Evento",
                    "Doble Mixto",
                    "Baker"
            };

            for (String nombre : modalidades) {
                if (!modalityRepository.findByName(nombre).isPresent()) {
                    modalityRepository.save(Modality.builder()
                            .name(nombre)
                            .description("Modalidad de " + nombre)
                            .status(true)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }

            // 8. Crear Ámbitos
            String[] ambitos = {
                    "Internacional",
                    "Nacional",
                    "Departamental",
                    "Regional",
                    "Municipal",
                    "Empresarial",
                    "Universitario"
            };
            for (String nombre : ambitos) {
                if (!ambitRepository.findByName(nombre).isPresent()) {
                    ambitRepository.save(Ambit.builder()
                            .name(nombre)
                            .description("Ámbito " + nombre)
                            .imageUrl("/uploads/ambits/" + nombre.replace(" ", "_").toLowerCase() + ".png")
                            .status(true)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }

            // 8. Crear torneos de ejemplo si no existen

            // Buscar categorías y modalidades
            List<Category> allCategories = categoryRepository.findAll();
            List<Modality> allModalities = modalityRepository.findAll();
            List<Ambit> allAmbits = ambitRepository.findAll();

            // --- CREAR TORNEOS ---
            String[] nombresTorneos = {
                    "Torneo Apertura Nacional",
                    "Copa Regional Andina",
                    "Masters del Caribe"
            };

            for (int i = 0; i < nombresTorneos.length; i++) {
                String nombreTorneo = nombresTorneos[i];
                if (tournamentRepository.findByName(nombreTorneo).isEmpty()) {
                    Tournament torneo = Tournament.builder()
                            .name(nombreTorneo)
                            .ambit(allAmbits.get(i % allAmbits.size())) // Asigna un ámbito distinto
                            .imageUrl("/uploads/tournaments/" + nombreTorneo.replace(" ", "_").toLowerCase() + ".png")
                            .startDate(LocalDate.of(2025, 8 + i, 10 + i))
                            .endDate(LocalDate.of(2025, 8 + i, 15 + i))
                            .location(i == 0 ? "Bogotá, Colombia" : (i == 1 ? "Medellín, Colombia" : "Cartagena, Colombia"))
                            .causeStatus("Programado")
                            .status(true)
                            .build();
                    tournamentRepository.save(torneo);

                    // --- ASIGNAR 2-3 MODALIDADES Y CATEGORÍAS ---
                    // Modality
                    List<Modality> mods = allModalities.subList(0, Math.min(3, allModalities.size()));
                    for (Modality mod : mods) {
                        TournamentModality tm = TournamentModality.builder()
                                .tournament(torneo)
                                .modality(mod)
                                .build();
                        tournamentModalityRepository.save(tm);
                    }
                    // Category
                    List<Category> cats = allCategories.subList(i, Math.min(i + 3, allCategories.size()));
                    for (Category cat : cats) {
                        TournamentCategory tc = TournamentCategory.builder()
                                .tournament(torneo)
                                .category(cat)
                                .build();
                        tournamentCategoryRepository.save(tc);
                    }
                }
            }


            System.out.println("✅ Torneos de ejemplo cargados");
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

    private User createUserIfNotExists(
            String nickname,
            String photoUrl,
            String document,
            String fullName,
            String fullSurname,
            LocalDate birthDate,
            String email,
            String gender,
            String roleDescription,
            PasswordEncoder passwordEncoder,
            PersonRepository personRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository
    ) {
        Optional<User> existingUser = userRepository.findByNickname(nickname);
        if (existingUser.isPresent()) {
            System.out.printf("🔁 Usuario '%s' ya existe, se omite.%n", nickname);
            return existingUser.get();
        }

        System.out.printf("📌 Creando usuario '%s'...%n", nickname);

        Person person = Person.builder()
                .fullName(fullName)
                .fullSurname(fullSurname)
                .birthDate(birthDate)
                .gender(gender)
                .email(email)
                .phone("3100000000")
                .status(true)
                .createdAt(LocalDateTime.now())
                .photoUrl(photoUrl)
                .document(document)
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
