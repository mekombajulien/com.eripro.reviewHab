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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eriapro.gestionDesAvis.DTO.roleDTO.AssignPermissionDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleCreateDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleResponseDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleUpdateDTO;
import com.eriapro.gestionDesAvis.contoller.RoleController;
import com.eriapro.gestionDesAvis.service.impl.RoleServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * addFilters = false : sans ça, la chaine de filtres de sécurité par défaut
 * de Spring Boot (HTTP Basic sur toutes les routes) s'applique car
 * @WebMvcTest ne charge PAS notre ConfugurationApplication (le
 * @Configuration qui déclare notre SecurityFilterChain). Résultat sans ce
 * flag : chaque appel ci-dessous recevrait un 401, jamais le code attendu.
 * On teste ici uniquement le contrôleur, pas les règles de sécurité.
 */
@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleServiceImpl roleServiceImpl;

    @Test
    void shouldCreateRoleAndReturn201() throws Exception {
        RoleCreateDTO r1 = RoleCreateDTO.builder()
                .name("SUPPORT")
                .build();

        when(roleServiceImpl.creer(any(RoleCreateDTO.class)))
                .thenReturn(RoleResponseDTO.builder()
                        .name("SUPPORT")
                        .build());

        mockMvc.perform(post("/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1)))
                .andExpect(status().isCreated());

        verify(roleServiceImpl).creer(any(RoleCreateDTO.class));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        RoleCreateDTO invalide = RoleCreateDTO.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAllRoles() throws Exception {
        RoleResponseDTO r1 = RoleResponseDTO.builder().id(1L).name("ADMIN").build();
        RoleResponseDTO r2 = RoleResponseDTO.builder().id(2L).name("CLIENT").build();

        when(roleServiceImpl.listerTous()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));

        verify(roleServiceImpl).listerTous();
    }

    @Test
    void shouldReturnRoleById() throws Exception {
        when(roleServiceImpl.trouverParId(1L))
                .thenReturn(RoleResponseDTO.builder().id(1L).name("ADMIN").build());

        mockMvc.perform(get("/admin/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN"));

        verify(roleServiceImpl).trouverParId(1L);
    }

    @Test
    void shouldUpdateRole() throws Exception {
        RoleUpdateDTO dto = RoleUpdateDTO.builder()
                .name("SUPERVISEUR")
                .active(false)
                .build();

        when(roleServiceImpl.modifier(eq(1L), any(RoleUpdateDTO.class)))
                .thenReturn(RoleResponseDTO.builder().id(1L).name("SUPERVISEUR").active(false).build());

        mockMvc.perform(put("/admin/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SUPERVISEUR"));

        verify(roleServiceImpl).modifier(eq(1L), any(RoleUpdateDTO.class));
    }

    @Test
    void shouldDeleteRoleAndReturn204() throws Exception {
        mockMvc.perform(delete("/admin/roles/1"))
                .andExpect(status().isNoContent());

        verify(roleServiceImpl).supprimer(1L);
    }

    @Test
    void shouldAssignPermissionToRole() throws Exception {
        AssignPermissionDTO dto = AssignPermissionDTO.builder()
                .permissionCode("AVIS_CREATE")
                .build();

        when(roleServiceImpl.assignerPermission(1L, "AVIS_CREATE"))
                .thenReturn(RoleResponseDTO.builder()
                        .id(1L)
                        .name("ASSISTANT")
                        .permissions(List.of("AVIS_CREATE"))
                        .build());

        mockMvc.perform(post("/admin/roles/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0]").value("AVIS_CREATE"));

        verify(roleServiceImpl).assignerPermission(1L, "AVIS_CREATE");
    }

    @Test
    void shouldReturn400WhenPermissionCodeIsBlank() throws Exception {
        AssignPermissionDTO invalide = AssignPermissionDTO.builder()
                .permissionCode("")
                .build();

        mockMvc.perform(post("/admin/roles/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRemovePermissionFromRole() throws Exception {
        when(roleServiceImpl.retirerPermission(1L, "AVIS_CREATE"))
                .thenReturn(RoleResponseDTO.builder()
                        .id(1L)
                        .name("ASSISTANT")
                        .permissions(List.of())
                        .build());

        mockMvc.perform(delete("/admin/roles/1/permissions/AVIS_CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions.length()").value(0));

        verify(roleServiceImpl).retirerPermission(1L, "AVIS_CREATE");
    }
}
