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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserCreateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserUpdateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserResponseDTO;
import com.eriapro.gestionDesAvis.contoller.AdminUserController;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.service.impl.AdminUserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * addFilters = false : voir RoleControllerTest pour l'explication —
 * @WebMvcTest ne charge pas notre SecurityFilterChain personnalisée.
 */
@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserServiceImpl adminUserServiceImpl;

    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        AdminUserCreateDTO dto = AdminUserCreateDTO.builder()
                .name("Marie")
                .email("marie@test.com")
                .password("motDePasse123")
                .build();

        when(adminUserServiceImpl.creer(any(AdminUserCreateDTO.class)))
                .thenReturn(UserResponseDTO.builder().id(1L).name("Marie").email("marie@test.com").build());

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("marie@test.com"));

        verify(adminUserServiceImpl).creer(any(AdminUserCreateDTO.class));
    }

    @Test
    void shouldReturnPagedUsersList() throws Exception {
        User user = User.builder().id(1L).name("Marie").email("marie@test.com").build();

        when(adminUserServiceImpl.listerTous(0, 10, "createdAt", "desc"))
                .thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("marie@test.com"));

        verify(adminUserServiceImpl).listerTous(0, 10, "createdAt", "desc");
    }

    @Test
    void shouldReturnUserById() throws Exception {
        when(adminUserServiceImpl.trouverParId(1L))
                .thenReturn(UserResponseDTO.builder().id(1L).name("Marie").email("marie@test.com").build());

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marie"));

        verify(adminUserServiceImpl).trouverParId(1L);
    }

    @Test
    void shouldUpdateUser() throws Exception {
        AdminUserUpdateDTO dto = AdminUserUpdateDTO.builder()
                .name("Marie Modifiée")
                .build();

        when(adminUserServiceImpl.modifier(eq(1L), any(AdminUserUpdateDTO.class)))
                .thenReturn(UserResponseDTO.builder().id(1L).name("Marie Modifiée").email("marie@test.com").build());

        mockMvc.perform(put("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marie Modifiée"));

        verify(adminUserServiceImpl).modifier(eq(1L), any(AdminUserUpdateDTO.class));
    }

    @Test
    void shouldDeleteUserAndReturn204() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(adminUserServiceImpl).supprimer(1L);
    }
}
