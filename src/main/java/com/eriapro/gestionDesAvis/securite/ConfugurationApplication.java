package com.eriapro.gestionDesAvis.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ConfugurationApplication {


    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtFilter jwtFilter;
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                 .authorizeHttpRequests(auth -> auth
                         // Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                         // routes publiques
                        .requestMatchers(
                                HttpMethod.POST,
                                "/inscription",
                                "/activation",
                                "/connection",
                                "/refresh",
                                "/updatePassword",
                                "/newPassWord"
                        ).permitAll()
                        // exemple permissions avis
                        .requestMatchers(
                                HttpMethod.GET,
                                "/avis/mes"
                        )
                        .hasAuthority("AVIS_READ_OWN")
                         
                        .requestMatchers(
                                HttpMethod.POST,
                                "/avis"
                        )
                        .hasAuthority("AVIS_CREATE")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/avis/**"
                        )
                        .hasAuthority("AVIS_UPDATE")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/avis"
                        )
                        .hasAuthority("AVIS_READ_ALL")
                   .requestMatchers(
                                HttpMethod.DELETE,
                                "/avis/**"
                        )
                        .hasAuthority("AVIS_DELETE")
                        // gestion des utilisateurs (admin)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/admin/users"
                        )
                        .hasAuthority("USER_CREATE")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin/users/**"
                        )
                        .hasAuthority("USER_READ")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/admin/users/**"
                        )
                        .hasAuthority("USER_UPDATE")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/admin/users/**"
                        )
                        .hasAuthority("USER_DELETE")
                        // gestion des rôles (admin)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/admin/roles"
                        )
                        .hasAuthority("ROLE_CREATE")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin/roles/**"
                        )
                        .hasAuthority("ROLE_READ")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/admin/roles/**"
                        )
                        .hasAuthority("ROLE_UPDATE")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/admin/roles/*/permissions"
                        )
                        .hasAuthority("PERMISSION_ASSIGN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/admin/roles/*/permissions/**"
                        )
                        .hasAuthority("PERMISSION_ASSIGN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/admin/roles/**"
                        )
                        .hasAuthority("ROLE_DELETE")
                        // gestion administration (fallback)
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
	    }
	    @Bean
	    AuthenticationManager authenticationManager(
	            AuthenticationConfiguration configuration
	    ) throws Exception {
	        return configuration.getAuthenticationManager();
	    }
	    @Bean
	    AuthenticationProvider authenticationProvider(
	            UserDetailsService userDetailsService
	    ) {
	        DaoAuthenticationProvider provider =
	                new DaoAuthenticationProvider(userDetailsService);
	        provider.setPasswordEncoder(passwordEncoder);
	        return provider;
	    }
	}