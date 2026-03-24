package com.app.tradingtournament.entity.enums;

public enum TournamentStatus {
    DRAFT,                // created, not yet open for registration
    REGISTRATION_OPEN,    // participants can join
    ACTIVE,               // trading is live
    COMPLETED,            // ended normally
    CANCELLED             // ended early by admin
}