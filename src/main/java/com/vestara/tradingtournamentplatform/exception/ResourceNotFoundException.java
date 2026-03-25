package com.vestara.tradingtournamentplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    @Getter
    public enum Code {
        USER_NOT_FOUND("User not found"),
        TOURNAMENT_NOT_FOUND("Tournament not found"),
        PORTFOLIO_NOT_FOUND("Portfolio not found"),
        HOLDING_NOT_FOUND("Holding not found"),
        TRADE_NOT_FOUND("Trade not found"),
        LEADERBOARD_NOT_FOUND("Leaderboard entry not found");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public ResourceNotFoundException(Code code) {
        super(HttpStatus.NOT_FOUND, code.name(), code.getDefaultMessage());
    }

    // Overload — append the id to the message for easier debugging
    public ResourceNotFoundException(Code code, Long id) {
        super(HttpStatus.NOT_FOUND, code.name(),
                code.getDefaultMessage() + " with id: " + id);
    }
}