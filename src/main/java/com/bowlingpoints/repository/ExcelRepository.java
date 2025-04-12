package com.bowlingpoints.repository;

import com.bowlingpoints.util.Jugador;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExcelRepository {

    public List<Jugador> leerJugadoresDeExcel(MultipartFile file) {
        List<Jugador> jugadores = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar encabezado

                Cell nombreCell = row.getCell(0);
                Cell clubCell = row.getCell(1);

                Jugador jugador = new Jugador(nombreCell.getStringCellValue(), clubCell.getStringCellValue());

                for (int i = 2; i < row.getLastCellNum(); i++) {
                    Cell puntajeCell = row.getCell(i);
                    if (puntajeCell != null && puntajeCell.getCellType() == CellType.NUMERIC) {
                        jugador.agregarPuntaje((int) puntajeCell.getNumericCellValue());
                    }
                }

                jugadores.add(jugador);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return jugadores;
    }
}