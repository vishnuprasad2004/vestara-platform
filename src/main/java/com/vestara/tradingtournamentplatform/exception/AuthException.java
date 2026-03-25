package com.vestara.tradingtournamentplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AuthException extends AppException {

    @Getter
    public enum Code {
        INVALID_CREDENTIALS("Invalid email or password"),
        EMAIL_NOT_VERIFIED("Please verify your email before logging in"),
        ACCOUNT_DISABLED("Your account has been disabled"),
        OAUTH2_EMAIL_MISSING("GitHub did not return an email address");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public AuthException(Code code) {
        super(HttpStatus.UNAUTHORIZED, code.name(), code.getDefaultMessage());
    }
}