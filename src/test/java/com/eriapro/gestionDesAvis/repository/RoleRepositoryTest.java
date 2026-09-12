package com.eriapro.gestionDesAvis.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.repository.RoleRepository;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldFindAllRoles() {
        // arrange : données initialisées via data.sql

        // act
        List<Role> roles = roleRepository.findAll();

        // assert
        assertThat(roles).hasSize(3);
        assertThat(roles)
            .extracting(Role::getName)
            .containsExactlyInAnyOrder("ADMIN", "MANAGER", "PATIENT");
    }

    @Test
    void shouldFindRoleById() {
        // arrange
        Role admin = roleRepository.findByName("ADMIN").orElseThrow();

        // act
        Optional<Role> found = roleRepository.findById(admin.getId());

        // assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    void shouldReturnEmptyWhenRoleIdDoesNotExist() {
        // act
        Optional<Role> found = roleRepository.findById(999L);

        // assert
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindRoleByName() {
        // act
        Optional<Role> found = roleRepository.findByName("MANAGER");

        // assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("MANAGER");
        assertThat(found.get().isSystemRole()).isTrue();
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenRoleNameDoesNotExist() {
        // act
        Optional<Role> found = roleRepository.findByName("INEXISTANT");

        // assert
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveNewRole() {
        // arrange
        Role newRole = Role.builder()
            .name("SUPPORT")
            .systemRole(false)
            .active(true)
            .build();

        // act
        Role saved = roleRepository.save(newRole);

        // assert
        assertThat(saved.getId()).isNotNull();
        assertThat(roleRepository.findAll()).hasSize(4);
        assertThat(roleRepository.findByName("SUPPORT")).isPresent();
    }

    @Test
    void shouldDeleteRole() {
        // arrange
        Role patient = roleRepository.findByName("PATIENT").orElseThrow();

        // act
        roleRepository.deleteById(patient.getId());

        // assert
        assertThat(roleRepository.findById(patient.getId())).isEmpty();
        assertThat(roleRepository.findByName("PATIENT")).isEmpty();
        assertThat(roleRepository.findAll()).hasSize(2);
    }
}