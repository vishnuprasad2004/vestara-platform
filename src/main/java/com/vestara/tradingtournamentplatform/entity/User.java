package com.vestara.tradingtournamentplatform.entity;

import com.vestara.tradingtournamentplatform.entity.base.BaseEntity;
import com.vestara.tradingtournamentplatform.entity.enums.AuthProvider;
import com.vestara.tradingtournamentplatform.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_github_id", columnList = "githubId")
    }
)
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE id = ?")
@Where(clause = "is_active = true")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Nullable — GitHub OAuth2 users have no password
    @Column
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String displayName;

    // Nullable — only set for GitHub OAuth2 users
    @Column(unique = true)
    private String githubId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // Soft delete flag — @SQLDelete + @Where work together
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Email verification
    @Column(nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider authProvider;
}