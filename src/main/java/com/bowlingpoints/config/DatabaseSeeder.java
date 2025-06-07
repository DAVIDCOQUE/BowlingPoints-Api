package com.bowlingpoints.config;

import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initData(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PersonRepository personRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println("🔧 [Seeder] Iniciando carga de datos...");

            // Crear roles si no existen
            if (roleRepository.count() == 0) {
                System.out.println("📌 Insertando roles...");
                roleRepository.save(Role.builder().description("ADMIN").build());
                roleRepository.save(Role.builder().description("ENTRENADOR").build());
                roleRepository.save(Role.builder().description("JUGADOR").build());
                System.out.println("✅ Roles insertados.");
            }

            // Crear permisos si no existen
            if (permissionRepository.count() == 0) {
                System.out.println("📌 Insertando permisos...");
                permissionRepository.save(Permission.builder().name("VER_DASHBOARD").description("Acceso al tablero").build());
                permissionRepository.save(Permission.builder().name("VER_PERFIL").description("Acceso al perfil").build());
                permissionRepository.save(Permission.builder().name("VER_MIS_TORNEOS").description("Consulta torneos").build());
                permissionRepository.save(Permission.builder().name("GESTIONAR_CLUBES").description("Administra clubes").build());
                permissionRepository.save(Permission.builder().name("VER_USUARIOS").description("Gestiona usuarios").build());
                System.out.println("✅ Permisos insertados.");
            }

            // Asignar todos los permisos al rol ADMIN
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
            System.out.println("✅ Permisos asignados al rol ADMIN.");

            // Crear usuario ADMIN
            createUserIfNotExists("davidcoque", "David", "Armando", "Sánchez", "Sanchez",
                    "david03sc@gmail.com", "ADMIN", passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository);

            // Crear usuario ENTRENADOR
            createUserIfNotExists("jhon", "jhon", "Elena", "Martínez", "Pérez",
                    "jhon@gmail.com", "ENTRENADOR", passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository);

            // Crear usuario JUGADOR
            createUserIfNotExists("sara", "sara", null, "Pérez", null,
                    "sara@gmail.com", "JUGADOR", passwordEncoder, personRepository, userRepository, userRoleRepository, roleRepository);

            System.out.println("🎉 [Seeder] Carga inicial completada con éxito.");
        };
    }

    private void createUserIfNotExists(String nickname, String firstName, String secondName,
                                       String lastname, String secondLastname, String email,
                                       String roleDescription, PasswordEncoder passwordEncoder,
                                       PersonRepository personRepository, UserRepository userRepository,
                                       UserRoleRepository userRoleRepository, RoleRepository roleRepository) {

        if (!userRepository.existsByNickname(nickname)) {
            System.out.printf("📌 Creando usuario '%s'...%n", nickname);

            Person person = Person.builder()
                    .firstName(firstName)
                    .secondName(secondName)
                    .lastname(lastname)
                    .secondLastname(secondLastname)
                    .gender("M")
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
                    .build();
            userRepository.save(user);

            Role role = roleRepository.findByDescription(roleDescription)
                    .orElseThrow(() -> new RuntimeException("❌ Rol no encontrado: " + roleDescription));

            userRoleRepository.save(UserRole.builder()
                    .user(user)
                    .role(role)
                    .status(true)
                    .build());

            System.out.printf("✅ Usuario '%s' creado con rol %s.%n", nickname, roleDescription);
        } else {
            System.out.printf("🔁 Usuario '%s' ya existe, se omite.%n", nickname);
        }
    }
}
