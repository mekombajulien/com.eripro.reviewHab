package com.eriapro.gestionDesAvis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserCreateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserUpdateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserResponseDTO;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.entite.UserRole;
import com.eriapro.gestionDesAvis.exception.RoleNotFoundException;
import com.eriapro.gestionDesAvis.exception.UserAlreadyExistsException;
import com.eriapro.gestionDesAvis.exception.UserNotFoundException;
import com.eriapro.gestionDesAvis.repository.RoleRepository;
import com.eriapro.gestionDesAvis.repository.UserRepository;
import com.eriapro.gestionDesAvis.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserServiceImpl;

    @Test
    void shouldCreateUserWithDefaultRoleWhenRoleNotProvided() {
        // ARRANGE
        AdminUserCreateDTO dto = AdminUserCreateDTO.builder()
                .name("Marie")
                .email("marie@test.com")
                .password("motDePasse123")
                .build(); // role non fourni

        Role roleClient = Role.builder().id(1L).name("CLIENT").build();
        User userSauve = User.builder().id(1L).name("Marie").email("marie@test.com").build();

        when(userRepository.findByEmailWithAuthorities("marie@test.com"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("CLIENT"))
                .thenReturn(Optional.of(roleClient));
        when(passwordEncoder.encode("motDePasse123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(userSauve);

        // ACT
        UserResponseDTO resultat = adminUserServiceImpl.creer(dto);

        // ASSERT
        assertThat(resultat.getEmail()).isEqualTo("marie@test.com");
        verify(roleRepository).findByName("CLIENT");
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void shouldCreateUserWithGivenRole() {
        // ARRANGE
        AdminUserCreateDTO dto = AdminUserCreateDTO.builder()
                .name("Admin")
                .email("admin@test.com")
                .password("motDePasse123")
                .role("ADMIN")
                .build();

        Role roleAdmin = Role.builder().id(2L).name("ADMIN").build();
        User userSauve = User.builder().id(2L).name("Admin").email("admin@test.com").build();

        when(userRepository.findByEmailWithAuthorities("admin@test.com"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(roleAdmin));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(userSauve);

        // ACT
        UserResponseDTO resultat = adminUserServiceImpl.creer(dto);

        // ASSERT
        assertThat(resultat.getEmail()).isEqualTo("admin@test.com");
        verify(roleRepository).findByName("ADMIN");
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        // ARRANGE
        AdminUserCreateDTO dto = AdminUserCreateDTO.builder()
                .name("Marie")
                .email("marie@test.com")
                .password("motDePasse123")
                .build();

        when(userRepository.findByEmailWithAuthorities("marie@test.com"))
                .thenReturn(Optional.of(User.builder().id(1L).build()));

        // ACT + ASSERT
        assertThrows(UserAlreadyExistsException.class,
                () -> adminUserServiceImpl.creer(dto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowWhenRoleNotFoundAtCreation() {
        // ARRANGE
        AdminUserCreateDTO dto = AdminUserCreateDTO.builder()
                .name("Marie")
                .email("marie@test.com")
                .password("motDePasse123")
                .role("INEXISTANT")
                .build();

        when(userRepository.findByEmailWithAuthorities("marie@test.com"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("INEXISTANT")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(RoleNotFoundException.class,
                () -> adminUserServiceImpl.creer(dto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnUserById() {
        // ARRANGE
        User user = User.builder().id(1L).name("Marie").email("marie@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // ACT
        UserResponseDTO resultat = adminUserServiceImpl.trouverParId(1L);

        // ASSERT
        assertThat(resultat.getName()).isEqualTo("Marie");
    }

    @Test
    void shouldThrowWhenUserIdNotFoundAtRead() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminUserServiceImpl.trouverParId(99L));
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        // ARRANGE
        User user = User.builder().id(1L).name("Ancien").email("ancien@test.com").actif(true).build();
        AdminUserUpdateDTO dto = AdminUserUpdateDTO.builder()
                .name("Nouveau")
                .build(); // email, actif, role non fournis -> ne doivent pas changer

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        UserResponseDTO resultat = adminUserServiceImpl.modifier(1L, dto);

        // ASSERT
        assertThat(resultat.getName()).isEqualTo("Nouveau");
        assertThat(user.getEmail()).isEqualTo("ancien@test.com");
        assertThat(user.isActif()).isTrue();
        verify(roleRepository, never()).findByName(anyString());
        verify(userRoleRepository, never()).deleteAll(any());
    }

    @Test
    void shouldUpdateUserRoleAndReplaceOldAssociations() {
        // ARRANGE
        User user = User.builder().id(1L).name("Marie").email("marie@test.com").build();
        Role nouveauRole = Role.builder().id(3L).name("MANAGER").build();
        List<UserRole> anciennesAssociations = List.of(UserRole.builder().id(10L).build());

        AdminUserUpdateDTO dto = AdminUserUpdateDTO.builder().role("MANAGER").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(nouveauRole));
        when(userRoleRepository.findByUser_Id(1L)).thenReturn(anciennesAssociations);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        adminUserServiceImpl.modifier(1L, dto);

        // ASSERT
        verify(userRoleRepository).deleteAll(anciennesAssociations);
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void shouldThrowWhenUpdatingWithUnknownRole() {
        // ARRANGE
        User user = User.builder().id(1L).build();
        AdminUserUpdateDTO dto = AdminUserUpdateDTO.builder().role("INEXISTANT").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("INEXISTANT")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(RoleNotFoundException.class,
                () -> adminUserServiceImpl.modifier(1L, dto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AdminUserUpdateDTO dto = AdminUserUpdateDTO.builder().name("Peu importe").build();

        assertThrows(UserNotFoundException.class,
                () -> adminUserServiceImpl.modifier(99L, dto));
    }

    @Test
    void shouldDeleteUser() {
        // ARRANGE
        when(userRepository.existsById(1L)).thenReturn(true);

        // ACT
        adminUserServiceImpl.supprimer(1L);

        // ASSERT
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> adminUserServiceImpl.supprimer(99L));

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldReturnPagedUsersList() {
        // ARRANGE
        User user = User.builder().id(1L).name("Marie").build();
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        // ACT
        Page<User> resultat = adminUserServiceImpl.listerTous(0, 10, "createdAt", "desc");

        // ASSERT
        assertThat(resultat.getContent()).hasSize(1);
    }

    @Test
    void shouldFallBackToDefaultSortWhenSortByIsInvalid() {
        // ARRANGE
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // ACT
        adminUserServiceImpl.listerTous(0, 10, "champInexistant", "asc");

        // ASSERT
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }
}
