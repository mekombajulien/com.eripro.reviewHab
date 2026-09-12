package com.eriapro.gestionDesAvis.securite;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eriapro.gestionDesAvis.entite.Jwt;
import com.eriapro.gestionDesAvis.repository.JwtRepository;
import com.eriapro.gestionDesAvis.service.impl.UserServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserServiceImpl userServiceImpl;
    private final JwtRepository jwtRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }


         String token = authHeader.substring(7);


        try {

            /*
             * Vérification de la présence du token en base
             * (token existant, non expiré et non désactivé)
             */
            Jwt jwtBDD = jwtService.verifyTokenValue(token);
               
         

            /*
             * Vérification expiration JWT
             */
            if (jwtService.isTokenExpired(token)) {
            
                jwtBDD.setExpire(true);
                jwtBDD.setDesactive(true);

                jwtRepository.save(jwtBDD);

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expiré");

                return;
            }



            String username = jwtService.extractUsername(token);

           

            /*
             * Si personne n'est encore authentifié
             * 
             * 
             */
            if (
                username != null &&
                jwtBDD.getUser().getEmail().equals(username) &&
                SecurityContextHolder.getContext().getAuthentication() == null
            ) {
            

                UserDetails user =
                        userServiceImpl.loadUserByUsername(username);


               
                if (jwtService.isValidToken(token, user)) {

                	
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );

                 
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                   
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                    
                }

            }


        }
        catch (ExpiredJwtException e) {


            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expiré");

            return;


        }
        catch (JwtException e) {


            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT invalide");

            return;


        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }


        filterChain.doFilter(request, response);

    }

}