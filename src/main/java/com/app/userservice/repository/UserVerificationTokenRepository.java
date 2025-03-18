package com.app.userservice.repository;

import com.app.userservice.entity.user.User;
import com.app.userservice.entity.user.UserVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVerificationTokenRepository extends JpaRepository<UserVerificationToken, Long> {
    Optional<UserVerificationToken> findByToken(String token);
    Optional<UserVerificationToken> findByTokenAndTokenType(String token, String tokenType);
    void deleteByUserAndTokenType(User user, String tokenType);
}