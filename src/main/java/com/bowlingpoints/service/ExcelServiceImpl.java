package com.bowlingpoints.service.impl;

import com.bowlingpoints.dto.PlayerResultUploadDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import com.bowlingpoints.service.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements FileService {

    private final PersonRepository personRepository;
    private final ResultRepository resultRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final ModalityRepository modalityRepository;

    @Override
    public List<PlayerResultUploadDTO> uploadResultsFromExcel(MultipartFile file) {
        List<PlayerResultUploadDTO> uploadedResults = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // 🧾 Leer metadata
            String tournamentName = sheet.getRow(0).getCell(0).getStringCellValue().trim();
            String modalityName = sheet.getRow(1).getCell(0).getStringCellValue().trim();
            String gender = sheet.getRow(2).getCell(0).getStringCellValue().trim();
            String categoryName = sheet.getRow(3).getCell(0).getStringCellValue().trim();

            // 🏆 Obtener o crear torneo
            Tournament tournament = tournamentRepository.findByName(tournamentName)
                    .orElseGet(() -> tournamentRepository.save(Tournament.builder()
                            .name(tournamentName)
                            .status(true)
                            .build()));

            // 🎯 Obtener o crear categoría
            Category category = categoryRepository.findByNameAndDeletedAtIsNull(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(categoryName)
                            .build()));

            // 📊 Leer encabezados
            Row headerRow = sheet.getRow(5);
            if (headerRow == null) return uploadedResults;

            // ✅ Rellenar mapas (asegura que no estén vacíos)
            Map<Integer, Integer> columnToRoundMap = new HashMap<>();
            Map<Integer, Integer> columnToLineMap = new HashMap<>();
            for (int col = 4; col < headerRow.getLastCellNum(); col++) {
                columnToRoundMap.put(col, 1); // todos en la ronda 1
                columnToLineMap.put(col, col - 3); // línea = índice de columna - 3
            }

            // 👥 Leer jugadores
            for (int rowIndex = 6; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String document = getCellValueAsString(row.getCell(0));
                String firstName = getCellValueAsString(row.getCell(1));
                String lastName = getCellValueAsString(row.getCell(2));
                String club = getCellValueAsString(row.getCell(3));

                if (firstName.isEmpty() && lastName.isEmpty()) continue;

                // 🔍 Buscar o crear persona
                Person person = personRepository.findByDocument(document)
                        .orElseGet(() -> {
                            Person p = new Person();
                            p.setDocument(document);
                            p.setFullName(firstName);
                            p.setFullSurname(lastName);
                            p.setEmail(firstName.toLowerCase() + "@mail.com");
                            return personRepository.save(p);
                        });

                Set<Integer> rounds = new HashSet<>();
                int totalLines = 0;

                // 🎳 Leer puntajes
                for (int col = 4; col < headerRow.getLastCellNum(); col++) {
                    Cell cell = row.getCell(col);
                    if (cell == null || cell.getCellType() != CellType.NUMERIC) continue;

                    Integer roundId = columnToRoundMap.get(col);
                    Integer lineNumber = columnToLineMap.get(col);
                    int score = (int) cell.getNumericCellValue();

                    if (roundId != null && lineNumber != null) {
                        Result result = new Result();
                        result.setPerson(person);
                        result.setTournament(tournament);
                        result.setCategory(category);
                        result.setScore(score);
                        result.setLineNumber(lineNumber);

                        // ✅ Este save() ahora se ejecuta realmente
                        resultRepository.save(result);
                        rounds.add(roundId);
                        totalLines++;
                    }
                }

                uploadedResults.add(new PlayerResultUploadDTO(
                        document,
                        firstName + " " + lastName,
                        club,
                        rounds,
                        totalLines
                ));
            }

        } catch (Exception e) {
            System.err.println("❌ Error leyendo Excel: " + e.getMessage());
        }

        return uploadedResults;
    }

     String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

}
