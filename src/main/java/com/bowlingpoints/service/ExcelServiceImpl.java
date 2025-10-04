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
    private final RoundRepository roundRepository;

    @Override
    public List<PlayerResultUploadDTO> uploadResultsFromExcel(MultipartFile file) {
        List<PlayerResultUploadDTO> uploadedResults = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // Leer metadata
            String tournamentName = sheet.getRow(0).getCell(0).getStringCellValue().trim();
            String modalityName = sheet.getRow(1).getCell(0).getStringCellValue().trim();
            String gender = sheet.getRow(2).getCell(0).getStringCellValue().trim();
            String categoryName = sheet.getRow(3).getCell(0).getStringCellValue().trim();

            // Obtener o crear entidades base
            Tournament tournament = tournamentRepository.findByName(tournamentName)
                    .orElseGet(() -> tournamentRepository.save(Tournament.builder()
                            .name(tournamentName)
                            .status(true)
                            .build()));

            Modality modality = modalityRepository.findByName(modalityName)
                    .orElseGet(() -> modalityRepository.save(Modality.builder()
                            .name(modalityName)
                            .build()));

            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(categoryName)
                            .build()));

            // Leer encabezados
            Row roundRow = sheet.getRow(4);
            Row headerRow = sheet.getRow(5);

            Map<Integer, Integer> columnToRoundMap = new HashMap<>();
            Map<Integer, Integer> columnToLineMap = new HashMap<>();
            Map<String, Round> roundLabelToEntity = new HashMap<>();

            for (int col = 4; col < headerRow.getLastCellNum(); col++) {
                Cell roundCell = roundRow.getCell(col);
                Cell lineCell = headerRow.getCell(col);
                if (roundCell == null || lineCell == null) continue;

                String roundLabel = roundCell.getStringCellValue();
                int lineNumber = (int) lineCell.getNumericCellValue();

                Round round = roundLabelToEntity.computeIfAbsent(roundLabel, label -> {
                    int roundNumber = roundLabelToEntity.size() + 1;
                    return roundRepository.findByTournamentAndRoundNumber(tournament, roundNumber)
                            .orElseGet(() -> {
                                Round r = new Round();
                                r.setTournament(tournament);
                                r.setRoundNumber(roundNumber);
                                return roundRepository.save(r);
                            });
                });

                columnToRoundMap.put(col, round.getRoundId());
                columnToLineMap.put(col, lineNumber);
            }

            // Leer jugadores
            for (int rowIndex = 6; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || row.getCell(1) == null) continue;

                String document = row.getCell(0) != null ? row.getCell(0).getStringCellValue().trim() : null;
                String fullName = row.getCell(1).getStringCellValue().trim();
                String surname = row.getCell(2).getStringCellValue().trim();
                String club = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : null;

                Person person;
                if (document != null && !document.isEmpty()) {
                    person = personRepository.findByDocument(document)
                            .orElseGet(() -> createPerson(document, fullName, surname));
                } else {
                    person = personRepository.findByFullNameAndFullSurname(fullName, surname)
                            .orElseGet(() -> createPerson(null, fullName, surname));
                }

                Set<Integer> rounds = new HashSet<>();
                int totalLines = 0;

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
                        result.setModality(modality);
                        result.setRound(roundRepository.findById(roundId).orElseThrow());
                        result.setScore(score);
                        result.setLineNumber(lineNumber);
                        result.setRama(gender);
                        resultRepository.save(result);
                        rounds.add(roundId);
                        totalLines++;
                    }
                }

                uploadedResults.add(new PlayerResultUploadDTO(
                        document,
                        fullName + " " + surname,
                        club,
                        rounds,
                        totalLines
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uploadedResults;
    }

    private Person createPerson(String document, String fullName, String surname) {
        Person p = new Person();
        p.setDocument(document);
        p.setFullName(fullName);
        p.setFullSurname(surname);
        String safeEmail = (document != null ? document : UUID.randomUUID().toString()) + "@bowlingpoints.com";
        p.setEmail(safeEmail);
        p.setStatus(true);
        return personRepository.save(p);
    }
}
