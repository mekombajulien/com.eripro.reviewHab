package com.eriapro.gestionDesAvis.repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eriapro.gestionDesAvis.entite.Jwt;


public interface JwtRepository extends JpaRepository<Jwt, Long> {


    Optional<Jwt>findByValueAndDesactiveAndExpire( String value , boolean desactive , boolean expire);
    @Query("""
    	    SELECT j
    	    FROM Jwt j
    	    WHERE j.expire = :expire
    	    AND j.desactive = :desactive
    	    AND j.user.email = :email
    	    """)
    	Optional<Jwt> findByUtilisateurValidToken(
    	        @Param("email") String email,
    	        @Param("desactive") boolean desactive,
    	        @Param("expire") boolean expire);

    Stream<Jwt> findByUserEmail(String email);
    
    Optional<Jwt> findByRefreshToken(String valeur);
    
    void deleteAllByExpireAndDesactive( boolean expire , boolean desactive);
}
