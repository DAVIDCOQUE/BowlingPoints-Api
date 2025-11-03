package com.bowlingpoints.config;

import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Bean
    CommandLineRunner initData(
            RoleRepository roleRepository,
            PersonRepository personRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            ClubRepository clubRepository,
            ClubPersonRepository clubPersonRepository,
            CategoryRepository categoryRepository,
            ModalityRepository modalityRepository,
            AmbitRepository ambitRepository,
            PasswordEncoder passwordEncoder,
            TournamentRepository tournamentRepository,
            TournamentModalityRepository tournamentModalityRepository,
            TournamentCategoryRepository tournamentCategoryRepository,
            BranchRepository branchRepository,
            TournamentBranchRepository tournamentBranchRepository,
            TeamRepository teamRepository,
            TeamPersonRepository teamPersonRepository,
            ResultRepository resultRepository,
            TournamentRegistrationRepository registrationRepository
    ) {
        return args -> {
            System.out.println("🔧 [Seeder] Iniciando carga de datos...");

            // 1️⃣ Roles base
            createRoleIfMissing("ADMIN", roleRepository);
            createRoleIfMissing("ENTRENADOR", roleRepository);
            createRoleIfMissing("JUGADOR", roleRepository);

            // 2️⃣ Usuarios base
            User admin = createUserIfNotExists(
                    "1143993925", "/uploads/users/default.png", "1143993925",
                    "David Armando", "Sánchez", LocalDate.of(1993, 4, 12),
                    "david03sc@gmail.com", "Masculino", "ADMIN",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

            Role jugadorRole = roleRepository.findByName("JUGADOR")
                    .orElseThrow(() -> new RuntimeException("❌ Rol no encontrado: JUGADOR"));
            userRoleRepository.save(UserRole.builder()
                    .user(admin)
                    .role(jugadorRole)
                    .status(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            User entrenador = createUserIfNotExists(
                    "198445652", "/uploads/users/default.png", "198445652",
                    "Jhon", "Soto", LocalDate.of(1980, 7, 22),
                    "jhon@gmail.com", "Masculino", "ENTRENADOR",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

            // ✅ Jugadores simulados (5 hombres, 5 mujeres)
            String[][] jugadoresExtra = {
                {"Carlos", "Gómez", "Masculino", "1000000000"},
                {"Juan", "Fernández", "Masculino", "1000000001"},
                {"Luis", "López", "Masculino", "1000000002"},
                {"Pedro", "Martínez", "Masculino", "1000000003"},
                {"Andrés", "Rodríguez", "Masculino", "1000000004"},
                {"Ana", "Gómez", "Femenino", "1000000100"},
                {"María", "Fernández", "Femenino", "1000000101"},
                {"Luisa", "López", "Femenino", "1000000102"},
                {"Claudia", "Martínez", "Femenino", "1000000103"},
                {"Sofía", "Rodríguez", "Femenino", "1000000104"}
            };

            List<Person> personasSimuladas = new ArrayList<>();
            for (String[] datos : jugadoresExtra) {
                User usuario = createUserIfNotExists(
                        datos[3], "/uploads/users/default.png", datos[3],
                        datos[0], datos[1], LocalDate.of(2002, 1, 1),
                        datos[0].toLowerCase() + "@mail.com", datos[2], "JUGADOR",
                        passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
                );
                personasSimuladas.add(usuario.getPerson());
            }

            // 3️⃣ Club principal
            Clubs club = clubRepository.findByName("Bowling Club Central")
                    .orElseGet(() -> clubRepository.save(
                    Clubs.builder()
                            .name("Bowling Club Central")
                            .description("Club principal de la ciudad")
                            .foundationDate(LocalDate.of(2020, 1, 1))
                            .city("Ciudad Bowling")
                            .imageUrl("/uploads/clubs/default.png")
                            .status(true)
                            .createdAt(LocalDateTime.now())
                            .build()
            ));

            for (Person p : personasSimuladas) {
                if (!clubPersonRepository.existsByClubAndPerson(club, p)) {
                    clubPersonRepository.save(ClubPerson.builder()
                            .club(club)
                            .person(p)
                            .roleInClub("JUGADOR")
                            .joinedAt(LocalDateTime.now())
                            .status(true)
                            .build());
                }
            }

            // 4️⃣ Categorías
            String[] categorias = {"Primera", "Segunda", "Juvenil", "Senior"};
            for (String nombre : categorias) {
                categoryRepository.findByNameAndDeletedAtIsNull(nombre)
                        .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(nombre)
                        .description("Categoría " + nombre)
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build()));
            }

            // 5️⃣ Modalidades (solo masculinas y femeninas)
            String[][] modalidadesExtendidas = {
                {"Sencillos Masculino", "Individual Masculino"},
                {"Sencillos Femenino", "Individual Femenino"},
                {"Dobles Masculino", "Pareja de hombres"},
                {"Dobles Femenino", "Pareja de mujeres"},
                {"Equipos Masculino", "Equipos masculinos"},
                {"Equipos Femenino", "Equipos femeninos"}
            };

            List<Modality> allModalities = new ArrayList<>();
            for (String[] mod : modalidadesExtendidas) {
                Modality m = modalityRepository.findByNameAndDeletedAtIsNull(mod[0])
                        .orElseGet(() -> modalityRepository.save(Modality.builder()
                        .name(mod[0])
                        .description(mod[1])
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build()));
                allModalities.add(m);
            }

            // 6️⃣ Ámbitos
            String[] ambitos = {"Nacional", "Departamental", "Municipal"};
            for (String nombre : ambitos) {
                ambitRepository.findByName(nombre)
                        .orElseGet(() -> ambitRepository.save(Ambit.builder()
                        .name(nombre)
                        .description("Ámbito " + nombre)
                        .imageUrl("/uploads/tournaments/" + nombre.toLowerCase() + ".png")
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build()));
            }

            // 7️⃣ Ramas (solo masculino y femenino)
            String[] ramas = {"Masculino", "Femenino"};
            for (String nombre : ramas) {
                Branch branch = branchRepository.findByName(nombre)
                        .orElseGet(() -> branchRepository.save(
                        Branch.builder()
                                .name(nombre)
                                .description("Rama " + nombre)
                                .status(true)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));
            }

            List<Branch> allBranches = branchRepository.findAll();
            List<Category> allCategories = categoryRepository.findAll();
            List<Ambit> allAmbits = ambitRepository.findAll();

            // 8️⃣ Torneos
            Tournament torneoApertura = tournamentRepository.findByName("Torneo Apertura")
                    .orElseGet(() -> {
                        Tournament t = Tournament.builder()
                                .name("Torneo Apertura")
                                .organizer("Federación Nacional de Bowling")
                                .ambit(allAmbits.get(0))
                                .imageUrl("/uploads/tournaments/torneo_apertura.png")
                                .startDate(LocalDate.of(2025, 5, 10))
                                .endDate(LocalDate.of(2025, 5, 15))
                                .location("Bogotá")
                                .stage("Finalizado")
                                .status(true)
                                .build();
                        Tournament saved = tournamentRepository.save(t);

                        allCategories.forEach(cat
                                -> tournamentCategoryRepository.save(TournamentCategory.builder()
                                        .tournament(saved)
                                        .category(cat)
                                        .build()));

                        allModalities.forEach(mod
                                -> tournamentModalityRepository.save(TournamentModality.builder()
                                        .tournament(saved)
                                        .modality(mod)
                                        .build()));

                        allBranches.forEach(branch
                                -> tournamentBranchRepository.save(TournamentBranch.builder()
                                        .tournament(saved)
                                        .branch(branch)
                                        .build()));
                        return saved;
                    });

            // 9️⃣ Equipos: 2 masculinos, 2 femeninos
            Map<String, int[]> equiposMap = Map.of(
                    "Equipo Masculino 1", new int[]{0, 1},
                    "Equipo Masculino 2", new int[]{2, 3},
                    "Equipo Femenino 1", new int[]{5, 6},
                    "Equipo Femenino 2", new int[]{7, 8}
            );

            Map<String, Team> equiposCreados = new HashMap<>();
            for (Map.Entry<String, int[]> entry : equiposMap.entrySet()) {
                String teamName = entry.getKey();
                int[] miembros = entry.getValue();
                Team equipo = teamRepository.findByNameTeam(teamName)
                        .orElseGet(() -> teamRepository.save(Team.builder()
                        .nameTeam(teamName)
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .build()));
                equiposCreados.put(teamName, equipo);

                for (int idx : miembros) {
                    Person p = personasSimuladas.get(idx);
                    if (!teamPersonRepository.existsByTeamAndPerson(equipo, p)) {
                        teamPersonRepository.save(TeamPerson.builder()
                                .team(equipo)
                                .person(p)
                                .createdAt(LocalDateTime.now())
                                .build());
                    }
                }
            }

            // 🔟 Inscripciones individuales
            Category categoriaBase = categoryRepository.findByNameAndDeletedAtIsNull("Primera").orElseThrow();
     

            for (Person p : personasSimuladas) {
                Branch rama = p.getGender().equalsIgnoreCase("Femenino")
                        ? branchRepository.findByName("Femenino").orElseThrow()
                        : branchRepository.findByName("Masculino").orElseThrow();

                Modality modInd = modalityRepository.findByNameAndDeletedAtIsNull(
                        p.getGender().equalsIgnoreCase("Femenino")
                        ? "Sencillos Femenino" : "Sencillos Masculino"
                ).orElseThrow();

                registrationRepository.save(TournamentRegistration.builder()
                        .person(p)
                        .tournament(torneoApertura)
                        .modality(modInd)
                        .category(categoriaBase)
                        .branch(rama)
                        .status(true)
                        .registrationDate(new Date())
                        .createdAt(new Date())
                        .createdBy("seeder")
                        .build());
            }

            // 1️⃣1️⃣ Inscripciones por equipo
            for (Map.Entry<String, Team> entry : equiposCreados.entrySet()) {
                String name = entry.getKey();
                Team equipo = entry.getValue();

                Branch rama = name.contains("Femenino")
                        ? branchRepository.findByName("Femenino").orElseThrow()
                        : branchRepository.findByName("Masculino").orElseThrow();

                String modName = name.contains("Femenino") ? "Dobles Femenino" : "Dobles Masculino";
                Modality mod = modalityRepository.findByNameAndDeletedAtIsNull(modName).orElseThrow();

                registrationRepository.save(TournamentRegistration.builder()
                        .team(equipo)
                        .tournament(torneoApertura)
                        .modality(mod)
                        .category(categoriaBase)
                        .branch(rama)
                        .status(true)
                        .registrationDate(new Date())
                        .createdAt(new Date())
                        .createdBy("seeder")
                        .build());
            }

            // 1️⃣2️⃣ Resultados individuales
            for (Person p : personasSimuladas) {
                Branch rama = p.getGender().equalsIgnoreCase("Femenino")
                        ? branchRepository.findByName("Femenino").orElseThrow()
                        : branchRepository.findByName("Masculino").orElseThrow();

                Modality modInd = modalityRepository.findByNameAndDeletedAtIsNull(
                        p.getGender().equalsIgnoreCase("Femenino")
                        ? "Sencillos Femenino" : "Sencillos Masculino"
                ).orElseThrow();

                for (int ronda = 1; ronda <= 2; ronda++) {
                    for (int linea = 1; linea <= 4; linea++) {
                        int score = 120 + RANDOM.nextInt(101);
                        int pista = 1 + RANDOM.nextInt(12);
                        resultRepository.save(Result.builder()
                                .person(p)
                                .tournament(torneoApertura)
                                .branch(rama)
                                .modality(modInd)
                                .category(categoriaBase)
                                .roundNumber(ronda)
                                .lineNumber(linea)
                                .laneNumber(pista)
                                .score(score)
                                .createdBy(1)
                                .createdAt(LocalDateTime.now())
                                .build());
                    }
                }
            }

            // 1️⃣3️⃣ Resultados por equipo
            for (Map.Entry<String, Team> entry : equiposCreados.entrySet()) {
                Team equipo = entry.getValue();
                String name = entry.getKey();

                Branch rama = name.contains("Femenino")
                        ? branchRepository.findByName("Femenino").orElseThrow()
                        : branchRepository.findByName("Masculino").orElseThrow();

                String modName = name.contains("Femenino") ? "Dobles Femenino" : "Dobles Masculino";
                Modality modEquipo = modalityRepository.findByNameAndDeletedAtIsNull(modName).orElseThrow();

                for (int ronda = 1; ronda <= 2; ronda++) {
                    for (int linea = 1; linea <= 3; linea++) {
                        int score = 200 + RANDOM.nextInt(101);
                        int pista = 1 + RANDOM.nextInt(12);
                        resultRepository.save(Result.builder()
                                .team(equipo)
                                .tournament(torneoApertura)
                                .branch(rama)
                                .modality(modEquipo)
                                .category(categoriaBase)
                                .roundNumber(ronda)
                                .lineNumber(linea)
                                .laneNumber(pista)
                                .score(score)
                                .createdBy(1)
                                .createdAt(LocalDateTime.now())
                                .build());
                    }
                }
            }

            System.out.println("✅ Seeder completado sin rama mixta, con ramas asignadas por género.");
        };
    }

    // Métodos auxiliares
    private void createRoleIfMissing(String name, RoleRepository roleRepository) {
        roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
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
            return existingUser.get();
        }

        Person person = personRepository.save(Person.builder()
                .fullName(fullName)
                .fullSurname(fullSurname)
                .birthDate(birthDate)
                .gender(gender)
                .email(email)
                .phone("3100000000")
                .status(true)
                .createdAt(LocalDateTime.now())
                .document(document)
                .build());

        String defaultPassword = System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "admin");

        User user = userRepository.save(User.builder()
                .nickname(nickname)
                .password(passwordEncoder.encode(defaultPassword))
                .status(true)
                .attemptsLogin(0)
                .lastLoginAt(LocalDateTime.now())
                .person(person)
                .createdAt(LocalDateTime.now())
                .build());

        Role role = roleRepository.findByName(roleDescription)
                .orElseThrow(() -> new RuntimeException("❌ Rol no encontrado: " + roleDescription));

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .status(true)
                .createdAt(LocalDateTime.now())
                .build());
        return user;
    }
}
