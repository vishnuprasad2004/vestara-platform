package com.vestara.tradingtournamentplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class RegistrationException extends AppException {

    @Getter
    public enum Code {
        EMAIL_ALREADY_EXISTS("An account with this email already exists"),
        GITHUB_ACCOUNT_ALREADY_LINKED("This GitHub account is linked to another user");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public RegistrationException(Code code) {
        super(HttpStatus.CONFLICT, code.name(), code.getDefaultMessage());
    }
}