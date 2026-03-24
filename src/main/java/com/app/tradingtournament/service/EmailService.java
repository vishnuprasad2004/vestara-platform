package com.app.tradingtournament.service;

import com.app.tradingtournament.entity.Tournament;
import com.app.tradingtournament.entity.TournamentParticipant;
import com.app.tradingtournament.entity.User;

public interface EmailService {

    void sendVerificationEmail(User user, String token);

    void sendTournamentJoinedEmail(User user, Tournament tournament);

    void sendTournamentStartedEmail(User user, Tournament tournament);

    void sendTournamentEndedEmail(User user, Tournament tournament, int rank);

    void sendTournamentCancelledEmail(User user, Tournament tournament, String reason);

    void sendDisqualifiedEmail(User user, Tournament tournament, String reason);
}