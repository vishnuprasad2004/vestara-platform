package com.vestara.tradingtournamentplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

import java.time.Instant;

import static com.vestara.tradingtournamentplatform.filter.CorrelationIdFilter.CORRELATION_ID_MDC_KEY;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorPayload error;
    private final MetaPayload meta;
    private final String traceId;
    private final Instant timestamp;

    // ── Static factories ──────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .traceId(MDC.get(CORRELATION_ID_MDC_KEY))
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, MetaPayload meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .meta(meta)
                .traceId(MDC.get(CORRELATION_ID_MDC_KEY))
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorPayload.of(code, message))
                .traceId(MDC.get(CORRELATION_ID_MDC_KEY))
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(
            String code,
            String message,
            java.util.List<FieldError> fieldErrors
    ) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorPayload.withFieldErrors(code, message, fieldErrors))
                .traceId(MDC.get(CORRELATION_ID_MDC_KEY))
                .timestamp(Instant.now())
                .build();
    }

    // ── Nested types ──────────────────────────────────────────────

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorPayload {
        private final String code;
        private final String message;
        private final java.util.List<FieldError> fieldErrors;

        public static ErrorPayload of(String code, String message) {
            return ErrorPayload.builder()
                    .code(code)
                    .message(message)
                    .build();
        }

        public static ErrorPayload withFieldErrors(
                String code,
                String message,
                java.util.List<FieldError> fieldErrors
        ) {
            return ErrorPayload.builder()
                    .code(code)
                    .message(message)
                    .fieldErrors(fieldErrors)
                    .build();
        }
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {
        private final String field;
        private final String message;

        public static FieldError of(String field, String message) {
            return FieldError.builder()
                    .field(field)
                    .message(message)
                    .build();
        }
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaPayload {
        private final Integer page;
        private final Integer size;
        private final Long totalElements;
        private final Integer totalPages;

        public static MetaPayload of(
                int page,
                int size,
                long totalElements,
                int totalPages
        ) {
            return MetaPayload.builder()
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .build();
        }
    }
}