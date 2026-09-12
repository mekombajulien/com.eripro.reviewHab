package com.eriapro.gestionDesAvis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.never;

import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleCreateDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleResponseDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleUpdateDTO;
import com.eriapro.gestionDesAvis.entite.Permission;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.RolePermission;
import com.eriapro.gestionDesAvis.exception.PermissionNotFoundException;
import com.eriapro.gestionDesAvis.exception.RoleNotFoundException;
import com.eriapro.gestionDesAvis.exception.SystemRoleModificationException;
import com.eriapro.gestionDesAvis.repository.PermissionRepository;
import com.eriapro.gestionDesAvis.repository.RolePermissionRepository;
import com.eriapro.gestionDesAvis.repository.RoleRepository;
import static org.junit.jupiter.api.Assertions.assertThrows;
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RoleServiceImpl roleServiceImpl;


    @Test
    void shouldReturnAllRole() {

        // ARRANGE

        Role r1 = Role.builder()
                .name("SUPPORT")
                .systemRole(false)
                .active(true)
                .build();

        Role r2 = Role.builder()
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

        when(roleRepository.findAll())
                .thenReturn(List.of(r1, r2));


        // ACT

        List<RoleResponseDTO> listeRole =
                roleServiceImpl.listerTous();


        // ASSERT

        assertThat(listeRole)
                .hasSize(2);
        assertThat(listeRole.get(0).getName())
             .isEqualTo("SUPPORT");

        assertThat(listeRole.get(1).getName())
            
        .isEqualTo("ASSISTANT");
        verify(roleRepository,times(1))
            .findAll();
    }
    @Test
    void shouldReturnEmptyListWhenRolesExist(){
    	
    	when(roleRepository.findAll())
    	      .thenReturn(List.of());
    	List<RoleResponseDTO> resultat = roleServiceImpl.listerTous();
    	
    	assertThat(resultat).isEmpty();
    	
    	verify(roleRepository,times(1))
    	  .findAll();
    }
    
    @Test
    void shouldCreateRole() {

        // ARRANGE
        RoleCreateDTO dto = RoleCreateDTO.builder()
                .name("INTENDANT")
                .build();

        Role role = Role.builder()
                .name("INTENDANT")
                .systemRole(false)
                .active(true)
                .build();

        when(roleRepository.findByName("INTENDANT"))
                .thenReturn(Optional.empty());

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        // ACT
        RoleResponseDTO resultat = roleServiceImpl.creer(dto);

        // ASSERT
        assertThat(resultat).isNotNull();
        assertThat(resultat.getName()).isEqualTo("INTENDANT");

        verify(roleRepository, times(1)).findByName("INTENDANT");
        verify(roleRepository, times(1)).save(any (Role.class));
    }
    @Test
    void shouldReturnRoleById () {
    	
    	Role r2 = Role.builder()
    			.id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();
    	when(roleRepository.findById(1L))
    	     .thenReturn(Optional.of(r2));
    	//act 
    	RoleResponseDTO resultat = roleServiceImpl.trouverParId(1L); 
    	//assert
    	assertThat(resultat).isNotNull();
    	assertThat(resultat.getName()).isEqualTo("ASSISTANT");
    	verify(roleRepository, times(1)).findById(1L);
        
    	
    }
    
    @Test
    void shouldThrowWhenRoleIdNotFound() {

        // ARRANGE
        when(roleRepository.findById(99L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows (RoleNotFoundException.class,
                () -> roleServiceImpl.trouverParId(99L));

        verify(roleRepository, times(1)).findById(99L);
    }
    @Test
    void shouldUpdateRole() {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

        RoleUpdateDTO dto = RoleUpdateDTO.builder()
                .name("SUPERVISEUR")
                .active(false)
                .build();

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        // ACT
        RoleResponseDTO resultat = roleServiceImpl.modifier(1L, dto);

        // ASSERT
        assertThat(resultat).isNotNull();
        assertThat(resultat.getName()).isEqualTo("SUPERVISEUR");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void shouldThrowWhenModifyingSystemRole() {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .systemRole(true)
                .active(true)
                .build();

        RoleUpdateDTO dto = RoleUpdateDTO.builder()
                .name("SUPERADMIN")
                .build();

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        // ACT + ASSERT
        assertThrows(SystemRoleModificationException.class,
                () -> roleServiceImpl.modifier(1L, dto));

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldThrowWhenModifyingRoleIdNotFound() {

        // ARRANGE
        RoleUpdateDTO dto = RoleUpdateDTO.builder()
                .name("SUPERVISEUR")
                .build();

        when(roleRepository.findById(99L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(RoleNotFoundException.class,
                () -> roleServiceImpl.modifier(99L, dto));

        verify(roleRepository, never()).save(any(Role.class));
        
    }
    
    @Test
    void shouldDeleteRole() {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        // ACT
        roleServiceImpl.supprimer(1L);

        // ASSERT
        verify(roleRepository, times(1)).delete(role);
    }

    @Test
    void shouldThrowWhenDeletingSystemRole() {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .systemRole(true)
                .active(true)
                .build();

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        // ACT + ASSERT
        assertThrows(SystemRoleModificationException.class,
                () -> roleServiceImpl.supprimer(1L));

        verify(roleRepository, never()).delete(any(Role.class));
    }
    @Test
    void shouldThrowWhenDeletingRoleIdNotFound() {

        // ARRANGE
        when(roleRepository.findById(99L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(RoleNotFoundException.class,
                () -> roleServiceImpl.supprimer(99L));

        verify(roleRepository, never()).delete(any(Role.class));
    }
    
    
    @Test
    void shouldNotDuplicatePermissionWhenAlreadyAssigned() {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

        Permission permission = Permission.builder()
                .code("AVIS_CREATE")
                .build();

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findByCode("AVIS_CREATE"))
                .thenReturn(Optional.of(permission));

        when(rolePermissionRepository.existsByRoleAndPermission(role, permission))
                .thenReturn(true);

        // ACT
        RoleResponseDTO resultat = roleServiceImpl.assignerPermission(1L, "AVIS_CREATE");

        // ASSERT
        assertThat(resultat).isNotNull();
        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    }
    
    @Test
    void shouldThrowWhenAssigningPermissionNotFound() {
    	
    	Role role = Role.builder()
                .id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

       

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findByCode("AVIS_CREATE"))
                .thenReturn(Optional.empty());
        assertThrows(PermissionNotFoundException.class,
                () -> roleServiceImpl.assignerPermission(1L, "AVIS_CREATE"));
        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    	
    }
    @Test
    void shouldDoNothingWhenPermissionNotAssigned () {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

        Permission permission = Permission.builder()
                .code("AVIS_CREATE")
                .build();

        

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findByCode("AVIS_CREATE"))
                .thenReturn(Optional.of(permission));

        

        // ACT
        RoleResponseDTO resultat = roleServiceImpl.retirerPermission(1L, "AVIS_CREATE");

        // ASSERT
        assertThat(resultat).isNotNull();
        verify(rolePermissionRepository, never()).delete(any(RolePermission.class));
    }
    
    
    @Test
    void shouldRemovePermissionFromRole() {

        // ARRANGE
        Role role = Role.builder()
                .id(1L)
                .name("ASSISTANT")
                .systemRole(false)
                .active(true)
                .build();

        Permission permission = Permission.builder()
                .code("AVIS_CREATE")
                .build();

        RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findByCode("AVIS_CREATE"))
                .thenReturn(Optional.of(permission));

        when(rolePermissionRepository.findByRoleAndPermission(role, permission))
                .thenReturn(Optional.of(rolePermission));

        // ACT
        RoleResponseDTO resultat = roleServiceImpl.retirerPermission(1L, "AVIS_CREATE");

        // ASSERT
        assertThat(resultat).isNotNull();
        verify(rolePermissionRepository, times(1)).delete(any(RolePermission.class));
    }
    
    @Test
    void shouldThrowWhenRemovingFromRoleIdNotFound() {
    	
    	when(roleRepository.findById(99L))
    	     .thenReturn(Optional.empty());
    	//act + assert
    	
    	 assertThrows(RoleNotFoundException.class,
                 () -> roleServiceImpl.retirerPermission(99L,"AVIS_CREATE"));

    	 verify(rolePermissionRepository, never()).delete(any(RolePermission.class));
    	
    }
    
    @Test
    void shouldThrowWhenRemovingPermissionNotFound() {
    	
    	 Role role = Role.builder()
                 .id(1L)
                 .name("ASSISTANT")
                 .systemRole(false)
                 .active(true)
                 .build();
    	
    	   when(roleRepository.findById(1L))
           .thenReturn(Optional.of(role));

   when(permissionRepository.findByCode("AVIS_CREATE"))
           .thenReturn(Optional.empty());

    	//act + assert
    	
    	 assertThrows(PermissionNotFoundException.class,
                 () -> roleServiceImpl.retirerPermission(1L,"AVIS_CREATE"));

    	 verify(rolePermissionRepository, never()).delete(any(RolePermission.class));
    	
    }
    
}