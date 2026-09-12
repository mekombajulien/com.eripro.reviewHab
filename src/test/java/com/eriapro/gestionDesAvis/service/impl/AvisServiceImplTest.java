package com.eriapro.gestionDesAvis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisRequestDTO;
import com.eriapro.gestionDesAvis.StatusAvis;
import com.eriapro.gestionDesAvis.entite.Avis;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.repository.AvisRepository;

/*
 * AvisServiceImpl lit l'utilisateur connecté via SecurityContextHolder
 * (et non via un paramètre de méthode) : il faut donc simuler
 * l'authentification avant chaque test qui en dépend, comme le ferait
 * le JwtFilter en conditions réelles.
 */
@ExtendWith(MockitoExtension.class)
class AvisServiceImplTest {

    @Mock
    private AvisRepository avisRepository;

    @InjectMocks
    private AvisServiceImpl avisServiceImpl;

    @AfterEach
    void nettoyerContexteSecurite() {
        SecurityContextHolder.clearContext();
    }

    private User connecter(Long id, String email) {
        User user = User.builder()
                .id(id)
                .email(email)
                .name("Client Test")
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return user;
    }

    @Test
    void shouldCreateAvisForConnectedUser() {
        // ARRANGE
        User client = connecter(1L, "client@test.com");

        AvisRequestDTO dto = AvisRequestDTO.builder()
                .message("Un avis suffisamment long pour passer la validation")
                .build();

        when(avisRepository.save(any(Avis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Avis resultat = avisServiceImpl.creer(dto);

        // ASSERT
        assertThat(resultat.getMessage()).isEqualTo(dto.getMessage());
        assertThat(resultat.getStatus()).isEqualTo(StatusAvis.EN_ATTENTE);
        assertThat(resultat.getUser()).isEqualTo(client);
        assertThat(resultat.getCreatedAt()).isNotNull();

        verify(avisRepository, times(1)).save(any(Avis.class));
    }

    @Test
    void shouldReturnMyAvisForConnectedUser() {
        // ARRANGE
        connecter(1L, "client@test.com");

        Avis avis = Avis.builder().id(10L).message("Bien").build();
        Page<Avis> page = new PageImpl<>(List.of(avis));

        when(avisRepository.findByUserEmail(eq("client@test.com"), any(Pageable.class)))
                .thenReturn(page);

        // ACT
        Page<Avis> resultat = avisServiceImpl.mesAvis(0, 10, "createdAt", "desc");

        // ASSERT
        assertThat(resultat.getContent()).hasSize(1);
        verify(avisRepository).findByUserEmail(eq("client@test.com"), any(Pageable.class));
    }

    @Test
    void shouldFallBackToDefaultSortWhenSortByIsInvalid() {
        // ARRANGE : aucune connexion nécessaire ici, on teste juste construirePageable()
        connecter(1L, "client@test.com");

        when(avisRepository.findByUserEmail(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        // ACT
        avisServiceImpl.mesAvis(0, 10, "champInexistant", "desc");

        // ASSERT : la whitelist doit retomber sur "createdAt"
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(avisRepository).findByUserEmail(any(), captor.capture());

        assertThat(captor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void shouldCapPageSizeAtFifty() {
        // ARRANGE
        connecter(1L, "client@test.com");

        when(avisRepository.findByUserEmail(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        // ACT
        avisServiceImpl.mesAvis(0, 500, "createdAt", "desc");

        // ASSERT
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(avisRepository).findByUserEmail(any(), captor.capture());

        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void shouldReturnAllAvisForAdmin() {
        // ARRANGE
        Avis avis = Avis.builder().id(1L).message("Test").build();

        when(avisRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(avis)));

        // ACT
        Page<Avis> resultat = avisServiceImpl.tousLesAvis(0, 10, "createdAt", "desc");

        // ASSERT
        assertThat(resultat.getContent()).hasSize(1);
        verify(avisRepository).findAll(any(Pageable.class));
    }

    @Test
    void shouldModifyOwnAvis() {
        // ARRANGE
        User client = connecter(1L, "client@test.com");

        Avis avisExistant = Avis.builder()
                .id(5L)
                .message("Ancien message")
                .status(StatusAvis.VALIDE)
                .user(client)
                .build();

        AvisRequestDTO dto = AvisRequestDTO.builder()
                .message("Nouveau message")
                .build();

        when(avisRepository.findById(5L)).thenReturn(Optional.of(avisExistant));
        when(avisRepository.save(any(Avis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Avis resultat = avisServiceImpl.modifier(5L, dto);

        // ASSERT
        assertThat(resultat.getMessage()).isEqualTo("Nouveau message");
        // une modification repasse l'avis en attente de revalidation
        assertThat(resultat.getStatus()).isEqualTo(StatusAvis.EN_ATTENTE);
    }

    @Test
    void shouldThrowWhenModifyingSomeoneElseAvis() {
        // ARRANGE
        connecter(1L, "client@test.com");

        User autreUser = User.builder().id(2L).email("autre@test.com").build();
        Avis avisDeQuelquunDautre = Avis.builder()
                .id(5L)
                .message("Message")
                .user(autreUser)
                .build();

        when(avisRepository.findById(5L)).thenReturn(Optional.of(avisDeQuelquunDautre));

        AvisRequestDTO dto = AvisRequestDTO.builder().message("Tentative").build();

        // ACT + ASSERT
        assertThatThrownBy(() -> avisServiceImpl.modifier(5L, dto))
                .isInstanceOf(RuntimeException.class);

        verify(avisRepository, never()).save(any(Avis.class));
    }

    @Test
    void shouldThrowWhenModifyingNonExistentAvis() {
        // ARRANGE
        connecter(1L, "client@test.com");
        when(avisRepository.findById(99L)).thenReturn(Optional.empty());

        AvisRequestDTO dto = AvisRequestDTO.builder().message("Peu importe").build();

        // ACT + ASSERT
        assertThatThrownBy(() -> avisServiceImpl.modifier(99L, dto))
                .isInstanceOf(RuntimeException.class);

        verify(avisRepository, never()).save(any(Avis.class));
    }

    @Test
    void shouldDeleteOwnAvis() {
        // ARRANGE
        User client = connecter(1L, "client@test.com");

        Avis avis = Avis.builder().id(5L).message("À supprimer").user(client).build();
        when(avisRepository.findById(5L)).thenReturn(Optional.of(avis));

        // ACT
        avisServiceImpl.supprimer(5L);

        // ASSERT
        verify(avisRepository, times(1)).delete(avis);
    }

    @Test
    void shouldThrowWhenDeletingSomeoneElseAvis() {
        // ARRANGE
        connecter(1L, "client@test.com");

        User autreUser = User.builder().id(2L).email("autre@test.com").build();
        Avis avis = Avis.builder().id(5L).message("Pas le mien").user(autreUser).build();

        when(avisRepository.findById(5L)).thenReturn(Optional.of(avis));

        // ACT + ASSERT
        assertThatThrownBy(() -> avisServiceImpl.supprimer(5L))
                .isInstanceOf(RuntimeException.class);

        verify(avisRepository, never()).delete(any(Avis.class));
    }

    @Test
    void shouldThrowWhenDeletingNonExistentAvis() {
        // ARRANGE
        connecter(1L, "client@test.com");
        when(avisRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> avisServiceImpl.supprimer(99L))
                .isInstanceOf(RuntimeException.class);

        verify(avisRepository, never()).delete(any(Avis.class));
    }
}
