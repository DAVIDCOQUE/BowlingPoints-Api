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
import java.util.*;

/**
 * Carga inicial de datos base del sistema (roles, usuarios, torneos, ramas y resultados).
 */
@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

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
                    "David Armando", "Sánchez Sánchez", LocalDate.of(1993, 4, 12),
                    "david03sc@gmail.com", "Masculino", "ADMIN",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

            // Agregar también rol de JUGADOR a David
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

            User jugador = createUserIfNotExists(
                    "11455625", "/uploads/users/default.png", "11455625",
                    "Sara", "Arteaga", LocalDate.of(2002, 1, 7),
                    "sara@gmail.com", "Femenino", "JUGADOR",
                    passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository
            );

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

            // Asociar jugadores al club
            if (clubPersonRepository.count() == 0) {
                clubPersonRepository.saveAll(List.of(
                        ClubPerson.builder()
                                .club(club)
                                .person(entrenador.getPerson())
                                .roleInClub("ENTRENADOR")
                                .joinedAt(LocalDateTime.now())
                                .status(true)
                                .build(),
                        ClubPerson.builder()
                                .club(club)
                                .person(jugador.getPerson())
                                .roleInClub("JUGADOR")
                                .joinedAt(LocalDateTime.now())
                                .status(true)
                                .build(),
                        ClubPerson.builder()
                                .club(club)
                                .person(admin.getPerson())
                                .roleInClub("JUGADOR")
                                .joinedAt(LocalDateTime.now())
                                .status(true)
                                .build()
                ));
            }

            // 4️⃣ Categorías (actualizadas)
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

            // 5️⃣ Modalidades (actualizadas)
            String[] modalidades = {"Individual", "Parejas", "Equipos"};
            for (String nombre : modalidades) {
                modalityRepository.findByNameAndDeletedAtIsNull(nombre)
                        .orElseGet(() -> modalityRepository.save(Modality.builder()
                                .name(nombre)
                                .description("Modalidad " + nombre)
                                .status(true)
                                .createdAt(LocalDateTime.now())
                                .build()));
            }

            // 6️⃣ Ámbitos
            String[] ambitos = {"Nacional", "Departamental", "Municipal"};
            for (String nombre : ambitos) {
                ambitRepository.findByName(nombre)
                        .orElseGet(() -> ambitRepository.save(Ambit.builder()
                                .name(nombre)
                                .description("Ámbito " + nombre)
                                .imageUrl("/uploads/tournament/" + nombre + ".png")
                                .status(true)
                                .createdAt(LocalDateTime.now())
                                .build()));
            }

            // 7️⃣ Ramas
            String[] ramas = {"Masculino", "Femenino", "Mixto"};
            for (String nombre : ramas) {
                branchRepository.findByName(nombre)
                        .orElseGet(() -> branchRepository.save(
                                Branch.builder()
                                        .name(nombre)
                                        .description("Rama " + nombre)
                                        .status(true)
                                        .createdAt(LocalDateTime.now())
                                        .build()));
            }

            List<Branch> allBranches = branchRepository.findAll();
            List<Category> allCategories = categoryRepository.findAll();
            List<Modality> allModalities = modalityRepository.findAll();
            List<Ambit> allAmbits = ambitRepository.findAll();

            // 8️⃣ Torneos
            String[] torneos = {"Torneo Apertura", "Copa Nacional Elite"};
            for (int i = 0; i < torneos.length; i++) {
                final int index = i;
                String nombre = torneos[i];

                tournamentRepository.findByName(nombre)
                        .orElseGet(() -> {
                            Tournament torneo = Tournament.builder()
                                    .name(nombre)
                                    .organizer("Federación Nacional de Bowling")
                                    .ambit(allAmbits.get(index % allAmbits.size()))
                                    .imageUrl("/uploads/tournaments/" + nombre.replace(" ", "_").toLowerCase() + ".png")
                                    .startDate(LocalDate.of(2025, 5 + index, 10))
                                    .endDate(LocalDate.of(2025, 5 + index, 15))
                                    .location(index == 0 ? "Bogotá" : "Medellín")
                                    .stage("Programado")
                                    .status(true)
                                    .build();

                            Tournament saved = tournamentRepository.save(torneo);

                            // ✅ Asignar categorías y modalidades y ramas
                            allCategories.stream().limit(2).forEach(cat ->
                                    tournamentCategoryRepository.save(TournamentCategory.builder()
                                            .tournament(saved)
                                            .category(cat)
                                            .build()));

                            allModalities.stream().limit(2).forEach(mod ->
                                    tournamentModalityRepository.save(TournamentModality.builder()
                                            .tournament(saved)
                                            .modality(mod)
                                            .build()));

                            allBranches.forEach(branch ->
                                    tournamentBranchRepository.save(TournamentBranch.builder()
                                            .tournament(saved)
                                            .branch(branch)
                                            .build()));

                            return saved;
                        });
            }

            // Obtener el torneo principal
            Tournament torneoEjemplo = tournamentRepository.findByName("Torneo Apertura").orElseThrow();

            Branch ramaFemenina = branchRepository.findByName("Femenino").orElseThrow();
            Branch ramaMasculina = branchRepository.findByName("Masculino").orElseThrow();

            // 9️⃣ Registrar jugadores en el torneo
            List<User> jugadoresTorneo = List.of(admin, entrenador, jugador);
            for (User user : jugadoresTorneo) {
                Branch rama = user.getPerson().getGender().equalsIgnoreCase("Femenino") ? ramaFemenina : ramaMasculina;

                registrationRepository.save(TournamentRegistration.builder()
                        .person(user.getPerson())
                        .tournament(torneoEjemplo)
                        .category(allCategories.get(0))
                        .modality(allModalities.get(0))
                        .branch(rama)
                        .status(true)
                        .registrationDate(new Date())
                        .createdAt(new Date())
                        .createdBy("seeder")
                        .build());
            }

            // 🔟 Simular resultados: 3 rondas x 7 líneas por jugador
            Random random = new Random();
            for (User user : jugadoresTorneo) {
                Branch rama = user.getPerson().getGender().equalsIgnoreCase("Femenino") ? ramaFemenina : ramaMasculina;

                for (int ronda = 1; ronda <= 3; ronda++) {
                    for (int linea = 1; linea <= 7; linea++) {
                        int score = 120 + random.nextInt(101); // puntaje entre 120 y 220
                        int pista = 1 + random.nextInt(12);   // pista entre 1 y 12

                        resultRepository.save(Result.builder()
                                .tournament(torneoEjemplo)
                                .person(user.getPerson())
                                .category(allCategories.get(0))
                                .modality(allModalities.get(0))
                                .branch(rama)
                                .roundNumber(ronda)
                                .laneNumber(pista)
                                .lineNumber(linea)
                                .score(score)
                                .createdBy(1)
                                .build());
                    }
                }
            }

            System.out.println("✅ Seeder completado con registros de torneo, jugadores y resultados simulados.");
        };
    }

    // =========================================================
    // Métodos auxiliares
    // =========================================================
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
        if (existingUser.isPresent()) return existingUser.get();

        Person person = personRepository.save(Person.builder()
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
                .build());

        User user = userRepository.save(User.builder()
                .nickname(nickname)
                .password(passwordEncoder.encode("admin"))
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
