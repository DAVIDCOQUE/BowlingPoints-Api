package com.bowlingpoints.service;

import com.bowlingpoints.dto.PersonImportResponse; // <--- Importa tu nuevo DTO
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
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

    // Formato flexible d/M/yyyy para aceptar 1/5/1990 y 01/05/1990
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

    @Transactional
    public PersonImportResponse importPersonFile(MultipartFile file) throws Exception {
        List<Person> personsToSave = new ArrayList<>();
        List<String> errorDetails = new ArrayList<>(); // <--- Ahora es una lista, no un StringBuilder

        int lineCount = 0;
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                lineCount++;

                // Omitir líneas vacías
                if (line.trim().isEmpty()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // CAMBIO IMPORTANTE: split(",") porque tu archivo ejemplo era CSV
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
                // Validamos índices para evitar ArrayIndexOutOfBoundsException
                String gender = data.length > 4 ? data[4].trim() : null;
                String dateStr = data.length > 5 ? data[5].trim() : null;
                String phone = data.length > 6 ? data[6].trim() : null;

                // Validaciones de Negocio (Unicidad)
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
                    Person person = Person.builder()
                            .document(document)
                            .fullName(names)
                            .fullSurname(surnames)
                            .email(email)
                            .gender(gender)
                            .birthDate(dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr, DATE_FORMATTER) : null)
                            .phone(phone)
                            .status(true)
                            .createdBy(1)
                            .build();

                    personsToSave.add(person);
                    successCount++;

                    // Batch save
                    if (personsToSave.size() >= 500) {
                        personRepository.saveAll(personsToSave);
                        personsToSave.clear();
                    }

                } catch (Exception e) {
                    errorCount++;
                    errorDetails.add("Línea " + lineCount + ": Error de formato -> " + e.getMessage());
                }
            }

            // Guardar los restantes
            if (!personsToSave.isEmpty()) {
                personRepository.saveAll(personsToSave);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error crítico leyendo el archivo: " + e.getMessage());
        }

        // Retornamos el objeto JSON
        return PersonImportResponse.builder()
                .successCount(successCount)
                .errorCount(errorCount)
                .totalProcessed(successCount + errorCount)
                .errors(errorDetails)
                .build();
    }
}