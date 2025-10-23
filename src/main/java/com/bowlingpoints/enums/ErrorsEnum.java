package com.bowlingpoints.enums;

public enum ErrorsEnum {
    // --- Errores de recursos ---
    NOT_FOUND("ERR-404", "Recurso no encontrado"),
    USER_NOT_FOUND("ERR-404-01", "Usuario no encontrado"),
    CLUB_NOT_FOUND("ERR-404-02", "Club no encontrado"),
    TOURNAMENT_NOT_FOUND("ERR-404-03", "Torneo no encontrado"),

    // --- Errores de validación ---
    INVALID_REQUEST("ERR-400", "Solicitud inválida"),
    MISSING_REQUIRED_FIELD("ERR-400-01", "Faltan campos obligatorios"),
    INVALID_FORMAT("ERR-400-02", "Formato de dato inválido"),

    // --- Errores de autenticación / autorización ---
    UNAUTHORIZED("ERR-401", "Usuario no autorizado"),
    FORBIDDEN("ERR-403", "Acceso denegado"),
    TOKEN_EXPIRED("ERR-401-01", "Token expirado o inválido"),

    // --- Errores de conflicto o lógica de negocio ---
    DUPLICATE_RESOURCE("ERR-409", "El recurso ya existe"),
    INVALID_STATE("ERR-409-01", "El estado actual no permite esta acción"),

    // --- Errores internos ---
    INTERNAL_SERVER_ERROR("ERR-500", "Error interno del servidor"),
    DATABASE_ERROR("ERR-500-01", "Error en la base de datos"),
    SERVICE_UNAVAILABLE("ERR-503", "Servicio no disponible temporalmente");

    private final String code;
    private final String message;

    ErrorsEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
