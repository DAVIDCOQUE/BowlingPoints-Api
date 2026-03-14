package com.bowlingpoints.util;

import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileReaderUtils {

    public static boolean isExcel(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    public static boolean isSupportedFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".csv") || lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    /**
     * Lee todas las filas de un archivo CSV o Excel.
     * Las filas vacías se retornan como String[0] para preservar la numeración.
     */
    public static List<String[]> readAllRows(MultipartFile file, String csvSeparator) throws IOException {
        if (isExcel(file)) {
            return readExcelRows(file);
        }
        return readCsvRows(file, csvSeparator);
    }

    private static List<String[]> readCsvRows(MultipartFile file, String separator) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    rows.add(new String[0]);
                } else {
                    rows.add(line.split(separator, -1));
                }
            }
        }
        return rows;
    }

    private static List<String[]> readExcelRows(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                int lastCell = row.getLastCellNum();
                if (lastCell <= 0) {
                    rows.add(new String[0]);
                    continue;
                }
                String[] cells = new String[lastCell];
                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    cells[i] = cell == null ? "" : getCellStringValue(cell);
                }
                rows.add(cells);
            }
        }
        return rows;
    }

    private static String getCellStringValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate()
                            .format(DateTimeFormatter.ofPattern("d/M/yyyy"));
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }
}
