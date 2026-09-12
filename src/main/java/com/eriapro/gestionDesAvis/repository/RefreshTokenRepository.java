package com.eriapro.gestionDesAvis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByValue(String value);

}
