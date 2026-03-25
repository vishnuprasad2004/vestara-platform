package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.entity.Tournament;
import com.vestara.tradingtournamentplatform.entity.User;
import com.vestara.tradingtournamentplatform.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ── Verification ──────────────────────────────────────────────

    @Async
    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendVerificationEmail(User user, String token) {
        Context context = new Context();
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("verificationUrl",
                frontendUrl + "/verify-email?token=" + token);

        sendEmail(
                user.getEmail(),
                "Verify your email — Trading Tournament",
                "verify-email",
                context
        );
    }

    // ── Tournament joined ─────────────────────────────────────────

    @Async
    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTournamentJoinedEmail(User user, Tournament tournament) {
        Context context = new Context();
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("tournamentName", tournament.getName());
        context.setVariable("startDate", tournament.getStartDate());
        context.setVariable("initialCapital", tournament.getInitialCapital());

        sendEmail(
                user.getEmail(),
                "You joined " + tournament.getName() + "!",
                "joined-tournament",
                context
        );
    }

    // ── Tournament started ────────────────────────────────────────

    @Async
    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTournamentStartedEmail(User user, Tournament tournament) {
        Context context = new Context();
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("tournamentName", tournament.getName());
        context.setVariable("endDate", tournament.getEndDate());

        sendEmail(
                user.getEmail(),
                tournament.getName() + " has started — start trading!",
                "tournament-started",
                context
        );
    }

    // ── Tournament ended ──────────────────────────────────────────

    @Async
    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTournamentEndedEmail(
            User user,
            Tournament tournament,
            int rank
    ) {
        Context context = new Context();
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("tournamentName", tournament.getName());
        context.setVariable("rank", rank);

        sendEmail(
                user.getEmail(),
                tournament.getName() + " — Final Results",
                "tournament-ended",
                context
        );
    }

    // ── Tournament cancelled ──────────────────────────────────────

    @Async
    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTournamentCancelledEmail(
            User user,
            Tournament tournament,
            String reason
    ) {
        Context context = new Context();
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("tournamentName", tournament.getName());
        context.setVariable("reason", reason);

        sendEmail(
                user.getEmail(),
                tournament.getName() + " has been cancelled",
                "tournament-cancelled",
                context
        );
    }

    // ── Disqualified ──────────────────────────────────────────────

    @Async
    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendDisqualifiedEmail(
            User user,
            Tournament tournament,
            String reason
    ) {
        Context context = new Context();
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("tournamentName", tournament.getName());
        context.setVariable("reason", reason);

        sendEmail(
                user.getEmail(),
                "You have been disqualified from " + tournament.getName(),
                "disqualified",
                context
        );
    }

    // ── Private send helper ───────────────────────────────────────

    private void sendEmail(
            String to,
            String subject,
            String templateName,
            Context context
    ) {
        try {
            String html = templateEngine.process(
                    "email/" + templateName, context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);  // true = isHtml

            mailSender.send(message);
            log.info("Email sent: to={}, subject={}", to, subject);

        } catch (MessagingException e) {
            // Logged at ERROR — never propagates (async + non-fatal)
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}