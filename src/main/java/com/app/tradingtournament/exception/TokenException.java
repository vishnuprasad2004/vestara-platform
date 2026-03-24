package com.app.tradingtournament.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class TokenException extends AppException {

    @Getter
    public enum Code {
        REFRESH_TOKEN_EXPIRED("Refresh token has expired, please log in again"),
        REFRESH_TOKEN_REVOKED("Refresh token has been revoked, please log in again"),
        REFRESH_TOKEN_ALREADY_USED("Refresh token reuse detected, please log in again"),
        VERIFICATION_TOKEN_EXPIRED("Verification link has expired, please request a new one"),
        VERIFICATION_TOKEN_ALREADY_USED("Email has already been verified"),
        INVALID_TOKEN("Token is invalid or malformed");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public TokenException(Code code) {
        super(HttpStatus.GONE, code.name(), code.getDefaultMessage());
    }
}