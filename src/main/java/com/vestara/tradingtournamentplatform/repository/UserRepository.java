package com.vestara.tradingtournamentplatform.repository;

import com.vestara.tradingtournamentplatform.entity.enums.UserRole;
import com.vestara.tradingtournamentplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGithubId(String githubId);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    Optional<User> findByIdIgnoreActive(@Param("id") Long id);
}