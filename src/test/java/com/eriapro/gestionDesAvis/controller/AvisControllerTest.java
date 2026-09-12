package com.eriapro.gestionDesAvis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eriapro.gestionDesAvis.StatusAvis;
import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisRequestDTO;
import com.eriapro.gestionDesAvis.contoller.AvisController;
import com.eriapro.gestionDesAvis.entite.Avis;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.service.impl.AvisServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * addFilters = false : @WebMvcTest ne charge pas notre SecurityFilterChain
 * personnalisée (voir RoleControllerTest pour l'explication détaillée) —
 * sans ce flag chaque requête recevrait un 401 avant même d'atteindre le
 * contrôleur.
 */
@WebMvcTest(AvisController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AvisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AvisServiceImpl avisServiceImpl;

    // Le contrôleur appelle AvisMapper.toResponseDTO(avis), qui lit avis.getUser().getName()/getEmail() :
    // l'Avis renvoyé par le mock doit donc toujours porter un User complet, sinon NullPointerException.
    private Avis avisAvecAuteur(Long id, String message) {
        User auteur = User.builder()
                .id(1L)
                .name("julien Nang")
                .email("juliennang@test.com")
                .build();

        return Avis.builder()
                .id(id)
                .message(message)
                .status(StatusAvis.EN_ATTENTE)
                .createdAt(Instant.now())
                .user(auteur)
                .build();
    }

    @Test
    void shouldCreateAvisAndReturn201() throws Exception {
        AvisRequestDTO dto = AvisRequestDTO.builder()
                .message("Un avis suffisamment long pour la validation")
                .build();

        when(avisServiceImpl.creer(any(AvisRequestDTO.class)))
                .thenReturn(avisAvecAuteur(1L, dto.getMessage()));

        mockMvc.perform(post("/avis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(dto.getMessage()))
                .andExpect(jsonPath("$.auteurEmail").value("juliennang@test.com"));

        verify(avisServiceImpl).creer(any(AvisRequestDTO.class));
    }

    @Test
    void shouldReturn400WhenMessageTooShort() throws Exception {
        AvisRequestDTO invalide = AvisRequestDTO.builder().message("Non").build();

        mockMvc.perform(post("/avis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldModifyAvis() throws Exception {
        AvisRequestDTO dto = AvisRequestDTO.builder()
                .message("Message corrigé et assez long")
                .build();

        when(avisServiceImpl.modifier(eq(1L), any(AvisRequestDTO.class)))
                .thenReturn(avisAvecAuteur(1L, dto.getMessage()));

        mockMvc.perform(put("/avis/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(dto.getMessage()));

        verify(avisServiceImpl).modifier(eq(1L), any(AvisRequestDTO.class));
    }

    @Test
    void shouldReturnMyAvis() throws Exception {
        when(avisServiceImpl.mesAvis(0, 10, "createdAt", "desc"))
                .thenReturn(new PageImpl<>(List.of(avisAvecAuteur(1L, "Mon avis"))));

        mockMvc.perform(get("/avis/mes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(avisServiceImpl).mesAvis(0, 10, "createdAt", "desc");
    }

    @Test
    void shouldReturnAllAvis() throws Exception {
        when(avisServiceImpl.tousLesAvis(0, 10, "createdAt", "desc"))
                .thenReturn(new PageImpl<>(List.of(avisAvecAuteur(1L, "Un avis"))));

        mockMvc.perform(get("/avis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(avisServiceImpl).tousLesAvis(0, 10, "createdAt", "desc");
    }

    @Test
    void shouldDeleteAvisAndReturn204() throws Exception {
        mockMvc.perform(delete("/avis/1"))
                .andExpect(status().isNoContent());

        verify(avisServiceImpl).supprimer(1L);
    }
}
