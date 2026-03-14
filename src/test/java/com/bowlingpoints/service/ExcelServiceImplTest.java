package com.bowlingpoints.service;

import com.bowlingpoints.repository.ExcelRepository;
import com.bowlingpoints.util.Jugador;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelServiceImplTest {

    private final ExcelRepository excelRepository = new ExcelRepository();

    private MockMultipartFile buildExcel(String... playerRows) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Nombre");
        header.createCell(1).setCellValue("Club");
        header.createCell(2).setCellValue("Ronda 1");
        header.createCell(3).setCellValue("Ronda 2");

        int rowIdx = 1;
        for (String rowData : playerRows) {
            String[] parts = rowData.split(",");
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(parts[0]);
            row.createCell(1).setCellValue(parts[1]);
            for (int i = 2; i < parts.length; i++) {
                row.createCell(i).setCellValue(Double.parseDouble(parts[i]));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new MockMultipartFile("file", "results.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray());
    }

    @Test
    void leerJugadores_ConUnJugador_RetornaUnRegistro() throws Exception {
        MockMultipartFile file = buildExcel("John Doe,Club ABC,250,200");

        List<Jugador> result = excelRepository.leerJugadoresDeExcel(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("John Doe");
        assertThat(result.get(0).getClub()).isEqualTo("Club ABC");
        assertThat(result.get(0).getPuntajes()).containsExactly(250, 200);
    }

    @Test
    void leerJugadores_ConVariosJugadores_RetornaListaCompleta() throws Exception {
        MockMultipartFile file = buildExcel(
                "Ana Torres,Club Norte,180,220",
                "Luis Ríos,Club Sur,210,195"
        );

        List<Jugador> result = excelRepository.leerJugadoresDeExcel(file);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Jugador::getNombre)
                .containsExactly("Ana Torres", "Luis Ríos");
    }

    @Test
    void leerJugadores_SinPuntajes_RetornaJugadorConListaVacia() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Nombre");
        header.createCell(1).setCellValue("Club");

        Row playerRow = sheet.createRow(1);
        playerRow.createCell(0).setCellValue("Sin Puntajes");
        playerRow.createCell(1).setCellValue("Club Vacío");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile("file", "results.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray());

        List<Jugador> result = excelRepository.leerJugadoresDeExcel(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPuntajes()).isEmpty();
    }

    @Test
    void leerJugadores_ArchivoVacio_LanzaExcepcion() {
        // POI 5.x lanza EmptyFileException (RuntimeException) para bytes vacíos
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]);

        assertThatThrownBy(() -> excelRepository.leerJugadoresDeExcel(file))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void leerJugadores_SoloEncabezado_RetornaListaVacia() throws Exception {
        MockMultipartFile file = buildExcel(); // sin filas de jugadores

        List<Jugador> result = excelRepository.leerJugadoresDeExcel(file);

        assertThat(result).isEmpty();
    }

    @Test
    void leerJugadores_IgnoraCeldasNoCasoNumericas() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");
        sheet.createRow(0); // encabezado

        Row playerRow = sheet.createRow(1);
        playerRow.createCell(0).setCellValue("Juan");
        playerRow.createCell(1).setCellValue("Club X");
        playerRow.createCell(2).setCellValue("texto"); // tipo STRING → debe ignorarse
        playerRow.createCell(3).setCellValue(150);      // tipo NUMERIC → debe incluirse

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile("file", "results.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray());

        List<Jugador> result = excelRepository.leerJugadoresDeExcel(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPuntajes()).containsExactly(150);
    }
}
