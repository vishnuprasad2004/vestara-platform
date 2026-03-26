package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.config.MarketTools;
import com.vestara.tradingtournamentplatform.dto.response.AiAssistantResponse;
import com.vestara.tradingtournamentplatform.dto.response.HoldingDTO;
import com.vestara.tradingtournamentplatform.dto.response.PortfolioDTO;
import com.vestara.tradingtournamentplatform.dto.response.TournamentDTO;
import com.vestara.tradingtournamentplatform.exception.ExternalServiceException;
import com.vestara.tradingtournamentplatform.repository.HoldingRepository;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.AiAssistantService;
import com.vestara.tradingtournamentplatform.service.PortfolioService;
import com.vestara.tradingtournamentplatform.service.TournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ChatClient chatClient;
    private final PortfolioService portfolioService;
    private final TournamentService tournamentService;
    private final MarketTools marketTools;

    String systemPrompt = """
        You are a trading coach inside a paper trading tournament. 
        Answer helpfully and concisely in max 3 sentences. 
        Give specific actionable advice. No financial disclaimers - this is a game.
        
        STRICT RULES:
        - Do not generate false or made-up information.
        - Do not use abusive, offensive, or inappropriate language.
        - Always maintain a professional tone.
        - Only provide financial insights based on available data.
        
        TOOL USAGE:
        - Use tools only when necessary.
        - Never call tools more than once per query.
    
    """;

    @Override
    public AiAssistantResponse chat(Long tournamentId, String message, UserPrincipal principal) {
        PortfolioDTO portfolio = portfolioService.getMyPortfolio(tournamentId, principal);
        TournamentDTO tournament = tournamentService.getById(tournamentId);

        String prompt = buildPrompt(portfolio, tournament, message);

        try {
            String reply = chatClient.prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .tools(marketTools)
                    .call()
                    .content();
            log.info(reply);
            return AiAssistantResponse.builder().reply(reply).build();
        } catch (Exception e) {
            log.error("AI assistant call failed: {}", e.getMessage());
            throw new ExternalServiceException(ExternalServiceException.Code.AI_SERVICE_UNAVAILABLE);
        }
    }

    private String buildPrompt(PortfolioDTO portfolio, TournamentDTO tournament, String userMessage) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== PLAYER CONTEXT ===\n")
                .append("Player: ").append(portfolio.getDisplayName()).append("\n")
                .append("Tournament: ").append(portfolio.getTournamentName()).append("\n")
                .append("Allowed Symbols: ").append(String.join(", ", tournament.getAllowedSymbols()))
                .append("Ends: ").append(tournament.getEndDate()).append("\n")
                .append("Cash available: $").append(portfolio.getCashBalance()).append("\n")
                .append("Total portfolio value: $").append(portfolio.getTotalPortfolioValue()).append("\n")
                .append("Return so far: ").append(portfolio.getReturnPercentage()).append("%\n\n");

        sb.append("=== CURRENT HOLDINGS ===\n");
        if (portfolio.getHoldings().isEmpty()) {
            sb.append("No holdings — fully in cash.\n");
        } else {

            for (HoldingDTO h : portfolio.getHoldings()) {
                sb.append(String.format("- %s: %d shares | avg buy $%.2f | current $%.2f | P&L %.2f%%\n",
                        h.getSymbol(), h.getQuantity(),
                        h.getAverageBuyPrice(), h.getCurrentPrice(),
                        h.getUnrealizedPnlPercent()));
            }
        }

        sb.append("\n=== TOOL USAGE ===\n")
                .append("You can fetch live stock prices using the getStockPrice tool. ")
                .append("Use it whenever needed before answering.\n");
        sb.append("You can call the getStockPrice tool only once per query.\n" +
                "Do not call it multiple times.");

        sb.append("You can fetch recent company news using the getCompanyNews tool. ")
                .append("Use it when the user asks about latest updates, events, or sentiment related to a company.\n");

        sb.append("Do not use getCompanyNews for stock price or financial data queries.\n")
                .append("Limit usage to once per query and summarize the results concisely.\n");

        sb.append("\n=== PLAYER QUESTION ===\n").append(userMessage);
        return sb.toString();
    }
}