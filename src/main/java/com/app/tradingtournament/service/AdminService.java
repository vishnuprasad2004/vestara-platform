package com.app.tradingtournament.service;

import com.app.tradingtournament.dto.response.PlatformAnalyticsDTO;
import com.app.tradingtournament.dto.response.TournamentAnalyticsDTO;
import com.app.tradingtournament.dto.response.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<UserDTO> getAllUsers(Pageable pageable);

    UserDTO banUser(Long userId);

    UserDTO unbanUser(Long userId);

    UserDTO promoteToAdmin(Long userId);

    PlatformAnalyticsDTO getPlatformAnalytics();

    TournamentAnalyticsDTO getTournamentAnalytics(Long tournamentId);
}