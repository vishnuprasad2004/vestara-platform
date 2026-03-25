
package com.vestara.tradingtournamentplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {

    @Getter
    public enum Code {
        YAHOO_FINANCE_UNAVAILABLE("Market data service is currently unavailable, please try again later"),
        YAHOO_FINANCE_INVALID_SYMBOL("No market data found for this symbol");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public ExternalServiceException(Code code) {
        super(HttpStatus.SERVICE_UNAVAILABLE, code.name(), code.getDefaultMessage());
    }
}