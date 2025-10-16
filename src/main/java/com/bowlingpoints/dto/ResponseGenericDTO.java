package com.bowlingpoints.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ResponseGenericDTO<T> {

    private Boolean success;
    private String message;
    private T data;

    public ResponseGenericDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

}
