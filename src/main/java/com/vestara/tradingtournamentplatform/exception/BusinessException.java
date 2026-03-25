package com.vestara.tradingtournamentplatform.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {

    public BusinessException(String code, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}