package com.vestara.tradingtournamentplatform.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)       // runs first — before security, before JWT filter
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = extractOrGenerate(request);

        // Put into MDC — every log line in this thread now includes correlationId
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // Echo back on response so client can trace their request
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL — always clean up MDC
            // MDC uses ThreadLocal — if not cleared, values leak into
            // the next request handled by this thread (thread pool reuse)
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String extractOrGenerate(HttpServletRequest request) {
        String incoming = request.getHeader(CORRELATION_ID_HEADER);
        if (StringUtils.hasText(incoming)) {

            // Client sent one — use it (enables end-to-end tracing)
            return incoming;
        }
        // Generate one — server-initiated tracing
        return UUID.randomUUID().toString();
    }
}