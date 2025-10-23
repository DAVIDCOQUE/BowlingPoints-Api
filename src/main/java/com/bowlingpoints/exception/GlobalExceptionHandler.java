package com.bowlingpoints.exception;

import com.bowlingpoints.dto.ResponseGenericDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public ResponseEntity<ResponseGenericDTO<Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        ResponseGenericDTO<Object> response = new ResponseGenericDTO<>(
                false,
                String.format("[%s] %s", ex.getCode(), ex.getMessage()),
                Map.of("timestamp", LocalDateTime.now(), "path", request.getDescription(false))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseGenericDTO<Object>> handleBadRequest(
            BadRequestException ex, WebRequest request) {
        ResponseGenericDTO<Object> response = new ResponseGenericDTO<>(
                false,
                String.format("[%s] %s", ex.getCode(), ex.getMessage()),
                Map.of("timestamp", LocalDateTime.now(), "path", request.getDescription(false))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseGenericDTO<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        ResponseGenericDTO<Object> response = new ResponseGenericDTO<>(
                false,
                String.format("[INTERNAL_ERROR] %s", ex.getMessage()),
                Map.of("timestamp", LocalDateTime.now(), "path", request.getDescription(false))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


