package com.vestara.tradingtournamentplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class TournamentException extends AppException {

    @Getter
    public enum Code {
        TOURNAMENT_FULL("Tournament has reached its maximum number of participants"),
        ALREADY_JOINED("You have already joined this tournament"),
        REGISTRATION_CLOSED("Tournament is not open for registration"),
        TOURNAMENT_NOT_ACTIVE("Tournament is not currently active"),
        SYMBOL_NOT_ALLOWED("This symbol is not allowed in this tournament"),
        INVALID_DATE_RANGE("Tournament end date must be after start date"),
        CANNOT_CANCEL("Only DRAFT or REGISTRATION_OPEN tournaments can be cancelled"),
        PARTICIPANT_NOT_ACTIVE("Participant is not active in this tournament");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public TournamentException(Code code) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code.name(), code.getDefaultMessage());
    }

    // Overload — for messages that need dynamic content e.g. symbol name
    public TournamentException(Code code, String detail) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code.name(),
                code.getDefaultMessage() + ": " + detail);
    }
}