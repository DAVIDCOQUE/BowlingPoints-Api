package com.bowlingpoints.service;

import com.bowlingpoints.dto.PersonImportResponse;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.entity.UserRole;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.RoleRepository;
import com.bowlingpoints.repository.UserRepository;
import com.bowlingpoints.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@Slf4j
@Service
public class PersonImportService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Value("${password.default}")
    private String passwordDefault;

    // Formato flexible d/M/yyyy para aceptar 1/5/1990 y 01/05/1990
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

    @Transactional
    public PersonImportResponse importPersonFile(MultipartFile file) throws Exception {
        List<Person> personsToSave = new ArrayList<>();
        List<User> usersToSave = new ArrayList<>();
        List<UserRole> userRolesToSave = new ArrayList<>();

        List<String> errorDetails = new ArrayList<>();

        int lineCount = 0;
        int successCount = 0;
        int errorCount = 0;

        // 1. Buscar el rol "jugador" una sola vez
        Role playerRole = roleRepository.findByName("JUGADOR")
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol 'jugador' en la base de datos"));

        // 2. Calcular el hash SHA-256 de la contraseña por defecto una sola vez
        // Si la propiedad inyectada no está presente (tests sin contexto Spring),
        // usamos un valor por defecto seguro.
        String defaultPlainPassword = (passwordDefault != null && !passwordDefault.isEmpty())
                ? passwordDefault
                : "BowlingPoints2025";
        String defaultPasswordHash = hashSha256(defaultPlainPassword);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                lineCount++;

                // Omitir líneas vacías
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(";");

                // Validación básica de longitud
                if (data.length < 4) {
                    errorCount++;
                    errorDetails.add("Línea " + lineCount + ": Datos incompletos o formato incorrecto.");
                    continue;
                }

                String document = data[0].trim();
                String names = data[1].trim();
                String surnames = data[2].trim();
                String email = data[3].trim();
                String gender = data.length > 4 ? data[4].trim() : null;
                String dateStr = data.length > 5 ? data[5].trim() : null;
                String phone = data.length > 6 ? data[6].trim() : null;

                // Validaciones de Negocio (Unicidad persona)
                if (personRepository.existsByDocument(document)) {
                    errorCount++;
                    errorDetails.add("Línea " + lineCount + ": Documento " + document + " ya existe.");
                    continue;
                }
                if (personRepository.existsByEmail(email)) {
                    errorCount++;
                    errorDetails.add("Línea " + lineCount + ": Email " + email + " ya existe.");
                    continue;
                }

                try {
                    // 1) Crear entidad Person
                    Person person = Person.builder()
                            .document(document)
                            .fullName(names)
                            .fullSurname(surnames)
                            .email(email)
                            .gender(normalizeGender(gender))
                            .birthDate(dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr, DATE_FORMATTER)
                                    : null)
                            .phone(phone)
                            .status(true)
                            .createdBy(1)
                            .createdAt(LocalDate.now().atStartOfDay())
                            .build();

                    personsToSave.add(person);

                    // 2) Crear entidad User asociada
                    // Usamos el documento como nickname, cámbialo si prefieres otra lógica
                    String nickname = document;

                    // Verificar que no exista user con ese nickname (por seguridad extra)
                    if (userRepository.existsByNickname(nickname)) {
                        errorCount++;
                        errorDetails
                                .add("Línea " + lineCount + ": Ya existe un usuario con nickname " + nickname + ".");
                        // No añadimos esta persona al batch, eliminamos la última persona agregada
                        personsToSave.remove(person);
                        continue;
                    }

                    User user = User.builder()
                            .person(person)
                            .password(defaultPasswordHash)
                            .status(true) // ajusta al valor que uses en tu sistema
                            .attemptsLogin(0)
                            .lastLoginAt(null)
                            .nickname(nickname)
                            .createdBy(1)
                            .createdAt(LocalDate.now().atStartOfDay())
                            .build();

                    usersToSave.add(user);

                    // 3) Crear relación UserRole (usuario -> rol jugador)
                    UserRole userRole = UserRole.builder()
                            .user(user)
                            .role(playerRole)
                            .status(true)
                            .createdAt(LocalDate.now().atStartOfDay())
                            .build();

                    userRolesToSave.add(userRole);

                    successCount++;

                    // Si quieres manejo batch por tamaño:
                    if (personsToSave.size() >= 500) {
                        // Importante: guardar en orden para respetar FKs
                        personRepository.saveAll(personsToSave);
                        userRepository.saveAll(usersToSave);
                        userRoleRepository.saveAll(userRolesToSave);

                        personsToSave.clear();
                        usersToSave.clear();
                        userRolesToSave.clear();
                    }

                } catch (Exception e) {
                    errorCount++;
                    errorDetails.add("Línea " + lineCount + ": Error de formato o creación -> " + e.getMessage());
                    log.error("Error procesando línea {}: {}", lineCount, e.getMessage(), e);
                }
            }

            // Guardar los restantes al final
            if (!personsToSave.isEmpty()) {
                personRepository.saveAll(personsToSave);
            }
            if (!usersToSave.isEmpty()) {
                userRepository.saveAll(usersToSave);
            }
            if (!userRolesToSave.isEmpty()) {
                userRoleRepository.saveAll(userRolesToSave);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error crítico leyendo el archivo: " + e.getMessage(), e);
        }

        // Retornamos el objeto JSON
        return PersonImportResponse.builder()
                .successCount(successCount)
                .errorCount(errorCount)
                .totalProcessed(successCount + errorCount)
                .errors(errorDetails)
                .build();
    }

    /**
     * Genera hash SHA-256 en formato hex.
     */
    private String hashSha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            // Fallback: intentar SHA-1 si SHA-256 no está disponible
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
                return bytesToHex(digest);
            } catch (Exception ex) {
                // Último recurso: devolver hex de los bytes UTF-8 (no es un hash criptográfico)
                try {
                    return bytesToHex(value.getBytes(StandardCharsets.UTF_8));
                } catch (Exception exc) {
                    throw new RuntimeException("Error generando hash SHA-256", exc);
                }
            }
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Género vacío o nulo");
        }

        return switch (gender.trim().toUpperCase()) {
            case "M", "MAS", "MASCULINO" ->
                "Masculino";
            case "F", "FEM", "FEMENINO" ->
                "Femenino";
            default ->
                throw new IllegalArgumentException("Género inválido: " + gender);
        };
    }

}
