package com.eriapro.gestionDesAvis.securite;

import java.security.Key; 
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


import javax.crypto.SecretKey;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.eriapro.gestionDesAvis.entite.Jwt;
import com.eriapro.gestionDesAvis.entite.RefreshToken;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.repository.JwtRepository;
import com.eriapro.gestionDesAvis.repository.RefreshTokenRepository;
import com.eriapro.gestionDesAvis.service.impl.UserServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService {


    private static final String BEARER = "bearer";
    private static final String REFRESH = "refresh";
    
    
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;


    private final UserServiceImpl userServiceImpl;
    private final JwtRepository jwtRepository;
    private final RefreshTokenRepository refreshTokenRepository;



    public Map<String,String> generate(String username){

        User user = (User) userServiceImpl.loadUserByUsername(username);


      

        String accessToken = generatedJwt(username);
        

        RefreshToken refreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .creation(Instant.now())
                .expire(Instant.now().plus(Duration.ofDays(7)))
                .user(user)
                .build();


       



        Jwt jwt = Jwt.builder()
                .value(accessToken)
                .expire(false)
                .desactive(false)
                .user(user)
                .refreshToken(refreshToken)
                .build();

        refreshToken.setJwt(jwt);

        jwtRepository.save(jwt);
        refreshTokenRepository.save(refreshToken);



        return Map.of(
                BEARER, accessToken,
                REFRESH, refreshToken.getValue()
        );
    }
    
    
 // creation du Jwt proprement dit 
    
    
    public String generatedJwt(String username){


        long now = System.currentTimeMillis();

        long expiration = now +jwtExpiration;



        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(getKey())
                .compact();

    }
    

    // algorithme d encodage de la cles du jwt
    

    private Key getKey(){


        byte[] decode =
                Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(decode);

    }
    
    
    // fonction pour desactiver et expirer les jwt lors de l appel d un refreshToken

    
    private void disableToken(Jwt jwt){

        jwt.setExpire(true);
        jwt.setDesactive(true);

        jwtRepository.save(jwt);

    }





// fonction pour rafraichir le token qui expire en pleine utilisation
    

    public Map<String,String> refreshToken(String refreshValue){


        RefreshToken refreshToken =
                refreshTokenRepository.findByValue(refreshValue)
                .orElseThrow(() ->
                        new RuntimeException("Refresh token invalide"));

        if(refreshToken.getExpire().isBefore(Instant.now())){

            throw new RuntimeException("Refresh token expiré");

        }


        Jwt jwtAncien = refreshToken.getJwt();

        disableToken(jwtAncien);
  
        User user = jwtAncien.getUser();

        String accessToken = generatedJwt(user.getEmail());

        RefreshToken nouveauRefresh =
                RefreshToken.builder()
                        .value(UUID.randomUUID().toString())
                        .creation(Instant.now())
                        .expire(Instant.now().plus(Duration.ofDays(7)))
                        .user(user)
                        .build();

        Jwt nouveauJwt =
                Jwt.builder()
                        .value(accessToken)
                        .expire(false)
                        .desactive(false)
                        .user(user)
                        .refreshToken(nouveauRefresh)
                        .build();

        nouveauRefresh.setJwt(nouveauJwt);

        refreshTokenRepository.save(nouveauRefresh);
        jwtRepository.save(nouveauJwt);
        refreshTokenRepository.delete(refreshToken);
        return Map.of(
                BEARER, accessToken,
                REFRESH, nouveauRefresh.getValue()
        );
    }



/*
 * les verification faire sur le jwt
 * extraction de claims 
 * verifier si il est valide 
 * verifie si il est inspiré
 * 
 * 
 * */

    public boolean isTokenExpired(String token){

        return extractExpiration(token)
                .before(new Date());

    }




    public String extractUsername(String token){

        return extractClaim(token, Claims::getSubject);

    }



    public Date extractExpiration(String token){

        return extractClaim(token, Claims::getExpiration);

    }



    private <T>T extractClaim(
            String token,
            Function<Claims,T> function){


        Claims claims = extractAllClaim(token);

        return function.apply(claims);

    }




    private Claims extractAllClaim(String token){


        return Jwts.parser()
                .verifyWith((SecretKey)getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }




    public boolean isValidToken(
            String token,
            UserDetails user){


        String username = extractUsername(token);
        
        if (username == null) {
        	
        	throw new RuntimeException(" le username de votre token est null");
        }


        return username.equals(user.getUsername())
                && !isTokenExpired(token);

    }




    public Jwt verifyTokenValue(String value){

    	 
        return jwtRepository
                .findByValueAndDesactiveAndExpire(
                        value,
                        false,
                        false
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Votre token est invalide"));

    }



    
    //  fonction s occupent de la deconnection 
    

    public void deconnection(){


        User userConnect =
                (User)
                SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();



        Jwt jwtUser =
                jwtRepository
                .findByUtilisateurValidToken(
                        userConnect.getEmail(),
                        false,
                        false
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Vous n'êtes pas connecté"));



        jwtUser.setExpire(true);
        jwtUser.setDesactive(true);



        jwtRepository.save(jwtUser);

    }



    // fonction qui supprime apres chaque 10 min les token expiré a la base de donnée 

    @Scheduled(cron = "0 */10 * * * *")
    public void RemoveUseLessJwt(){


        jwtRepository.deleteAllByExpireAndDesactive(
                true,
                true
        );


        log.info(
          "Nettoyage des tokens expirés ou désactivés effectué"
        );

    }

    
    /*
     * cette fonction sera utile quand il faudra deconnecter tout les appareils connecter 
     * suspition de piratage 
     * 
     * private void disableAllUserTokens(User user){

    List<Jwt> jwtList =
            jwtRepository.findByUserEmail(user.getEmail())
            .collect(Collectors.toList());


    jwtList.forEach(jwt -> {
        jwt.setExpire(true);
        jwt.setDesactive(true);
    });

  ////Ensuite on passera à la partie ADMIN/MANAGER (gestion des avis + gestion des rôles dynamiques).
    jwtRepository.saveAll(jwtList);

}
     * 
     * 
     * */
}