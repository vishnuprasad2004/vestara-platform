package com.vestara.tradingtournamentplatform.controller;

import com.vestara.tradingtournamentplatform.dto.ApiResponse;
import com.vestara.tradingtournamentplatform.dto.request.AiAssistantRequest;
import com.vestara.tradingtournamentplatform.dto.response.AiAssistantResponse;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/assistant")
    @PreAuthorize("hasAuthority('trade:execute')")
    public ResponseEntity<ApiResponse<AiAssistantResponse>> chat(
            @Valid @RequestBody AiAssistantRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        AiAssistantResponse response = aiAssistantService.chat(
                request.getTournamentId(),
                request.getMessage(),
                principal
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}