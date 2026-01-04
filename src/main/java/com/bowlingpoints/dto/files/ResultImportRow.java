package com.bowlingpoints.dto.files;

/**
 * Representa una fila del archivo CSV de importación masiva de resultados.
 * Los resultados deben pertenecer todos al mismo torneo.
 */
public record ResultImportRow(
        String documento,
        String nombreTorneo,
        String categoria,
        String modalidad,
        String rama,
        String equipo,          // puede ser null o vacío
        Integer numeroRonda,
        Integer numeroCarril,
        Integer numeroLinea,
        Integer puntaje,
        int lineNumber          // para tracking de errores
) {
}
