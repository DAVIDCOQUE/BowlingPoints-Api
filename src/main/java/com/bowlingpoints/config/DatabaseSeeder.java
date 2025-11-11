package com.bowlingpoints.config;

import com.bowlingpoints.dto.TournamentRegistrationDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import com.bowlingpoints.service.TournamentRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
            TournamentTeamRepository tournamentTeamRepository,
            TournamentRegistrationService tournamentRegistrationService,
            TournamentRegistrationRepository registrationRepository,
            ResultRepository resultRepository
    ) {
        return args -> {

            System.out.println("🔧 [Seeder] Iniciando carga de datos con modelo híbrido...");

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

            User entrenador = createUserIfNotExists(
                    "198445652", "/uploads/users/default.png", "198445652",
                    "Jhon", "Soto", LocalDate.of(1980, 7, 22),
                    "jhon@gmail.com", "Masculino", "ENTRENADOR",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

            // 3️⃣ Jugadores simulados
            String[][] jugadores = {
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

            List<Person> personas = new ArrayList<>();
            for (String[] d : jugadores) {
                User u = createUserIfNotExists(
                        d[3], "/uploads/users/default.png", d[3],
                        d[0], d[1], LocalDate.of(2002, 1, 1),
                        d[0].toLowerCase() + "@mail.com", d[2], "JUGADOR",
                        passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
                );
                personas.add(u.getPerson());
            }

            // 4️⃣ Club principal
            Clubs club = clubRepository.findByName("Bowling Club Central")
                    .orElseGet(() -> clubRepository.save(Clubs.builder()
                            .name("Bowling Club Central")
                            .description("Club principal de la ciudad")
                            .foundationDate(LocalDate.of(2020, 1, 1))
                            .city("Bogotá")
                            .imageUrl("/uploads/clubs/default.png")
                            .status(true)
                            .createdAt(LocalDateTime.now())
                            .build()));

            for (Person p : personas) {
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

            // 5️⃣ Categorías y ramas
            Category catPrimera = categoryRepository.findByNameAndDeletedAtIsNull("Primera")
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name("Primera")
                            .description("Categoría Primera")
                            .status(true)
                            .createdAt(LocalDateTime.now())
                            .build()));

            Branch ramaMasculina = branchRepository.findByNameIgnoreCase("Masculino")
                    .orElseGet(() -> branchRepository.save(Branch.builder()
                            .name("Masculino").description("Rama Masculina")
                            .status(true).createdAt(LocalDateTime.now()).build()));

            Branch ramaFemenina = branchRepository.findByNameIgnoreCase("Femenino")
                    .orElseGet(() -> branchRepository.save(Branch.builder()
                            .name("Femenino").description("Rama Femenina")
                            .status(true).createdAt(LocalDateTime.now()).build()));

            // 6️⃣ Modalidades
            String[][] modalidades = {
                    {"Sencillos Masculino", "Individual Masculino"},
                    {"Sencillos Femenino", "Individual Femenino"},
                    {"Equipos Masculino", "Por equipos masculinos"},
                    {"Equipos Femenino", "Por equipos femeninos"}
            };
            Map<String, Modality> modalidadMap = new HashMap<>();
            for (String[] mod : modalidades) {
                Modality m = modalityRepository.findByNameAndDeletedAtIsNull(mod[0])
                        .orElseGet(() -> modalityRepository.save(Modality.builder()
                                .name(mod[0])
                                .description(mod[1])
                                .status(true)
                                .createdAt(LocalDateTime.now())
                                .build()));
                modalidadMap.put(mod[0], m);
            }

            // 7️⃣ Torneo principal
            Ambit ambito = ambitRepository.findByName("Nacional")
                    .orElseGet(() -> ambitRepository.save(Ambit.builder()
                            .name("Nacional").description("Ámbito nacional").status(true).build()));

            Tournament torneo = tournamentRepository.findByName("Torneo Apertura")
                    .orElseGet(() -> tournamentRepository.save(Tournament.builder()
                            .name("Torneo Apertura")
                            .organizer("Federación Nacional de Bowling")
                            .ambit(ambito)
                            .startDate(LocalDate.now().minusDays(15))
                            .endDate(LocalDate.now().minusDays(5))
                            .location("Bogotá")
                            .stage("Finalizado")
                            .status(true)
                            .build()));

            // Asociaciones torneo
            tournamentCategoryRepository.save(TournamentCategory.builder().tournament(torneo).category(catPrimera).build());
            tournamentBranchRepository.save(TournamentBranch.builder().tournament(torneo).branch(ramaMasculina).build());
            tournamentBranchRepository.save(TournamentBranch.builder().tournament(torneo).branch(ramaFemenina).build());
            modalidadMap.values().forEach(m ->
                    tournamentModalityRepository.save(TournamentModality.builder().tournament(torneo).modality(m).build()));

            // 8️⃣ Crear equipos y asignar jugadores
            Map<String, int[]> equipos = Map.of(
                    "Equipo Masculino 1", new int[]{0, 1, 2},
                    "Equipo Masculino 2", new int[]{3, 4},
                    "Equipo Femenino 1", new int[]{5, 6},
                    "Equipo Femenino 2", new int[]{7, 8, 9}
            );

            Map<String, Team> teamMap = new HashMap<>();

            for (Map.Entry<String, int[]> e : equipos.entrySet()) {
                Team team = teamRepository.findByNameTeam(e.getKey())
                        .orElseGet(() -> teamRepository.save(Team.builder()
                                .nameTeam(e.getKey()).status(true).createdAt(LocalDateTime.now()).build()));
                teamMap.put(e.getKey(), team);

                for (int idx : e.getValue()) {
                    Person p = personas.get(idx);
                    teamPersonRepository.save(TeamPerson.builder()
                            .team(team).person(p).createdAt(LocalDateTime.now()).build());
                }

                tournamentTeamRepository.save(TournamentTeam.builder()
                        .team(team).tournament(torneo).status(true).build());
            }

            // 9️⃣ Inscripciones individuales + por equipo (modelo híbrido)
            for (Person p : personas) {
                boolean femenino = p.getGender().equalsIgnoreCase("Femenino");
                Branch rama = femenino ? ramaFemenina : ramaMasculina;

                // 🔹 Sencillos
                Modality modSencillos = femenino ? modalidadMap.get("Sencillos Femenino") : modalidadMap.get("Sencillos Masculino");
                registrationRepository.save(TournamentRegistration.builder()
                        .person(p).tournament(torneo).modality(modSencillos)
                        .category(catPrimera).branch(rama).status(true)
                        .registrationDate(new Date()).createdAt(new Date()).createdBy("seeder").build());

                // 🔹 Si pertenece a equipo, también se inscribe en Equipos

                teamPersonRepository.findAll().stream()
                        .filter(tp -> tp.getPerson().getPersonId().equals(p.getPersonId()))
                        .findFirst()
                        .ifPresent(tp -> {
                            Modality modEquipo = femenino ? modalidadMap.get("Equipos Femenino") : modalidadMap.get("Equipos Masculino");

                            System.out.println("↪ Inscribiendo en equipo: " +
                                    "personId=" + p.getPersonId() +
                                    ", teamId=" + tp.getTeam().getTeamId() +
                                    ", tournamentId=" + torneo.getTournamentId() +
                                    ", modalityId=" + modEquipo.getModalityId());

                            tournamentRegistrationService.create(TournamentRegistrationDTO.builder()
                                    .personId(p.getPersonId())
                                    .teamId(tp.getTeam().getTeamId())
                                    .tournamentId(torneo.getTournamentId())
                                    .modalityId(modEquipo.getModalityId())
                                    .categoryId(catPrimera.getCategoryId())
                                    .branchId(rama.getBranchId())
                                    .status(true)
                                    .build());
                        });
            }

            // 🔟 Resultados individuales + por equipo

// Primero, mapeamos las personas con su equipo (si tienen)
            Map<Integer, Team> personaEquipoMap = teamPersonRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            tp -> tp.getPerson().getPersonId(),
                            TeamPerson::getTeam,
                            (a, b) -> a // En caso de duplicados, escoge uno (no debería haber)
                    ));

            for (Person p : personas) {
                Branch rama = p.getGender().equalsIgnoreCase("Femenino") ? ramaFemenina : ramaMasculina;
                Modality modInd = p.getGender().equalsIgnoreCase("Femenino")
                        ? modalidadMap.get("Sencillos Femenino")
                        : modalidadMap.get("Sencillos Masculino");

                // 🟦 Resultados individuales
                for (int ronda = 1; ronda <= 2; ronda++) {
                    for (int linea = 1; linea <= 3; linea++) {
                        resultRepository.save(Result.builder()
                                .person(p).tournament(torneo).branch(rama).modality(modInd)
                                .category(catPrimera).roundNumber(ronda).lineNumber(linea)
                                .laneNumber(1 + RANDOM.nextInt(10))
                                .score(120 + RANDOM.nextInt(100))
                                .createdBy(1).createdAt(LocalDateTime.now()).build());
                    }
                }

                // 🟥 Resultados por equipo (si aplica)
                Team team = personaEquipoMap.get(p.getPersonId());
                if (team != null) {
                    Modality modEquipo = p.getGender().equalsIgnoreCase("Femenino")
                            ? modalidadMap.get("Equipos Femenino")
                            : modalidadMap.get("Equipos Masculino");

                    System.out.println("✅ Guardando resultados por equipo para: " + p.getFullName() + " - Team: " + team.getNameTeam());

                    for (int ronda = 1; ronda <= 2; ronda++) {
                        for (int linea = 1; linea <= 3; linea++) {
                            resultRepository.save(Result.builder()
                                    .person(p).team(team).tournament(torneo)
                                    .branch(rama).modality(modEquipo).category(catPrimera)
                                    .roundNumber(ronda).lineNumber(linea)
                                    .laneNumber(1 + RANDOM.nextInt(10))
                                    .score(200 + RANDOM.nextInt(80))
                                    .createdBy(1).createdAt(LocalDateTime.now()).build());
                        }
                    }
                }
            }

            System.out.println("✅ Seeder híbrido completado con inscripciones individuales y por equipo.");
        };
    }

    // ========== Helpers ==========
    private void createRoleIfMissing(String name, RoleRepository repo) {
        repo.findByName(name).orElseGet(() -> repo.save(Role.builder().name(name).build()));
    }

    private User createUserIfNotExists(
            String nickname, String photo, String doc, String name, String surname,
            LocalDate birth, String email, String gender, String role,
            PasswordEncoder encoder, PersonRepository pr, UserRepository ur,
            UserRoleRepository urr, RoleRepository rr
    ) {
        return ur.findByNickname(nickname).orElseGet(() -> {
            Person p = pr.save(Person.builder()
                    .fullName(name).fullSurname(surname).birthDate(birth)
                    .gender(gender).email(email).phone("3100000000")
                    .document(doc).status(true).createdAt(LocalDateTime.now()).build());
            User u = ur.save(User.builder()
                    .nickname(nickname).password(encoder.encode("admin"))
                    .status(true).attemptsLogin(0).person(p)
                    .createdAt(LocalDateTime.now()).build());
            Role r = rr.findByName(role).orElseThrow();
            urr.save(UserRole.builder().user(u).role(r).status(true).createdAt(LocalDateTime.now()).build());
            return u;
        });
    }
}
