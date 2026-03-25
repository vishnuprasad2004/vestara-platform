
package com.vestara.tradingtournamentplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {

    @Getter
    public enum Code {
        MARKET_DATA_UNAVAILABLE("Market data service is currently unavailable, please try again later"),
        MARKET_DATA_INVALID_SYMBOL("No market data found for this symbol"),
        AI_SERVICE_UNAVAILABLE("AI assistant is currently unavailable. Please try again.");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public ExternalServiceException(Code code) {
        super(HttpStatus.SERVICE_UNAVAILABLE, code.name(), code.getDefaultMessage());
    }
}