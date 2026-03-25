package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.response.PlatformAnalyticsDTO;
import com.vestara.tradingtournamentplatform.dto.response.TournamentAnalyticsDTO;
import com.vestara.tradingtournamentplatform.dto.response.UserDTO;
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