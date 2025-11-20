package com.bowlingpoints.service;

import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j; // Para logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PersonImportService {

    @Autowired
    private PersonRepository personRepository;
    
    // Usamos "d/M/yyyy" (con una sola letra) para que acepte tanto "01/05/1990" como "1/5/1990"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
    @Transactional
    public String importPersonFile(MultipartFile file) throws Exception {
        List<Person> personsToSave = new ArrayList<>();
        int lineCount = 0;
        int successCount = 0;
        int errorCount = 0;
        StringBuilder errors = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            // Saltamos la cabecera si existe
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                lineCount++;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Asumiendo separador punto y coma (;). Cambiar a "," si es CSV estándar.
                String[] data = line.split(";");

                // Validación básica de longitud
                if (data.length < 4) {
                    errorCount++;
                    errors.append("Línea ").append(lineCount).append(": Datos incompletos. \n");
                    continue;
                }

                String document = data[0].trim();
                String names = data[1].trim();
                String surnames = data[2].trim();
                String email = data[3].trim();
                String gender = data.length > 4 ? data[4].trim() : null;
                String dateStr = data.length > 5 ? data[5].trim() : null;
                String phone = data.length > 6 ? data[6].trim() : null;

                // Validaciones de Negocio (Unicidad)
                if (personRepository.existsByDocument(document)) {
                    errorCount++;
                    errors.append("Línea ").append(lineCount).append(": Documento ").append(document).append(" ya existe. \n");
                    continue;
                }
                if (personRepository.existsByEmail(email)) {
                    errorCount++;
                    errors.append("Línea ").append(lineCount).append(": Email ").append(email).append(" ya existe. \n");
                    continue;
                }

                try {
                    // Construcción del objeto usando el Builder de Lombok
                    Person person = Person.builder()
                            .document(document)
                            .fullName(names)
                            .fullSurname(surnames)
                            .email(email)
                            .gender(gender)
                            .birthDate(dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr, DATE_FORMATTER) : null)
                            .phone(phone)
                            .status(true) // Default activo
                            .createdBy(1) // ID del admin o usuario sistema por defecto
                            .build();

                    personsToSave.add(person);
                    successCount++;

                    // Guardado por lotes (Batch) para no saturar memoria
                    if (personsToSave.size() >= 500) {
                        personRepository.saveAll(personsToSave);
                        personsToSave.clear();
                    }

                } catch (Exception e) {
                    errorCount++;
                    errors.append("Línea ").append(lineCount).append(": Error al procesar datos -> ").append(e.getMessage()).append("\n");
                }
            }

            // Guardar los restantes
            if (!personsToSave.isEmpty()) {
                personRepository.saveAll(personsToSave);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error crítico leyendo el archivo: " + e.getMessage());
        }

        return String.format("Proceso finalizado. Exitosos: %d. Fallidos: %d. \nDetalles de errores:\n%s",
                successCount, errorCount, errors.toString());
    }
}