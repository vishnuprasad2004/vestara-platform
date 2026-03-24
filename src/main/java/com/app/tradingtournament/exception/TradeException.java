package com.app.tradingtournament.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class TradeException extends AppException {

    @Getter
    public enum Code {
        INSUFFICIENT_BALANCE("Insufficient balance to execute this trade"),
        INSUFFICIENT_HOLDINGS("Insufficient holdings to execute this sell"),
        INVALID_QUANTITY("Trade quantity must be greater than zero"),
        DUPLICATE_TRADE("This trade has already been submitted"),
        PRICE_UNAVAILABLE("Stock price is currently unavailable, please try again");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
    }

    public TradeException(Code code) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code.name(), code.getDefaultMessage());
    }

    // Overload — for INSUFFICIENT_BALANCE with amounts
    public TradeException(Code code, BigDecimal required, BigDecimal available) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code.name(),
                code.getDefaultMessage() +
                String.format(" — required: $%.2f, available: $%.2f",
                        required, available));
    }
}