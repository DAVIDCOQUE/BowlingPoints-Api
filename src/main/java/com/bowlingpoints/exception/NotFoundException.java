package com.bowlingpoints.exception;

import com.bowlingpoints.enums.ErrorsEnum;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String code;

    public NotFoundException(ErrorsEnum error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

}
