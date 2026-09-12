package com.eriapro.gestionDesAvis.config;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.eriapro.gestionDesAvis.entite.Permission;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.RolePermission;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.entite.UserRole;
import com.eriapro.gestionDesAvis.exception.PermissionNotFoundException;
import com.eriapro.gestionDesAvis.exception.RoleNotFoundException;
import com.eriapro.gestionDesAvis.repository.PermissionRepository;
import com.eriapro.gestionDesAvis.repository.RolePermissionRepository;
import com.eriapro.gestionDesAvis.repository.RoleRepository;
import com.eriapro.gestionDesAvis.repository.UserRepository;
import com.eriapro.gestionDesAvis.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Value("${admin.default-password:ChangeMoi123!}")
    private String adminDefaultPassword;

	    @Override
	    public void run(String... args) throws Exception {

	        initRoles();
	        initPermissions();
	        assignPermissions(); 
	        initAdmin();   
	        // Nous la compléterons juste après

	    }
	    
	    
	    /**
	     * Création du compte administrateur par défaut, s'il n'existe pas déjà.
	     */
	    private void initAdmin() {

	        String adminEmail = "admin@avis.com";

	        if (userRepository.findByEmailWithAuthorities(adminEmail).isPresent()) {
	            return;
	        }

	        Role roleAdmin = roleRepository.findByName("ADMIN")
	                .orElseThrow(() -> new RoleNotFoundException("Le rôle ADMIN n'existe pas"));

	        User admin = User.builder()
	                .name("Administrateur")
	                .email(adminEmail)
	                .password(passwordEncoder.encode(adminDefaultPassword))
	                .actif(true)
	                .build();

	        User adminSave = userRepository.save(admin);

	        UserRole userRole = UserRole.builder()
	                .user(adminSave)
	                .role(roleAdmin)
	                .createdAt(Instant.now())
	                .build();

	        userRoleRepository.save(userRole);
	    }
	    

	    /**
	     * Création des rôles système.
	     */
	    private void initRoles() {

	        createRole("ADMIN", true);
	        createRole("MANAGER", true);
	        createRole("CLIENT", true);

	    }

	    /**
	     * Création d'un rôle s'il n'existe pas.
	     */
	    private void createRole(String name, boolean systemRole) {

	        if (roleRepository.findByName(name).isEmpty()) {

	            Role role = Role.builder()
	                    .name(name)
	                    .systemRole(systemRole)
	                    .active(true)
	                    .build();

	            roleRepository.save(role);
	        }

	    }

	    /**
	     * Création des permissions.
	     */
	    private void initPermissions() {

	        // Avis
	        createPermission("AVIS_CREATE", "Créer un avis");
	        createPermission("AVIS_READ_OWN", "Voir ses propres avis");
	        createPermission("AVIS_READ_ALL", "Voir tous les avis");
	        createPermission("AVIS_UPDATE", "Modifier un avis");
	        createPermission("AVIS_DELETE", "Supprimer un avis");

	        // Rôles
	        createPermission("ROLE_CREATE", "Créer un rôle");
	        createPermission("ROLE_READ", "Consulter les rôles");
	        createPermission("ROLE_DELETE", "Supprimer un rôle");
	        createPermission("ROLE_ASSIGN", "Attribuer un rôle");

	        // Permissions
	        createPermission("PERMISSION_READ", "Consulter les permissions");
	        createPermission("PERMISSION_ASSIGN", "Attribuer des permissions");

	        // Utilisateurs
	        createPermission("USER_CREATE", "Créer un utilisateur");
	        createPermission("USER_READ", "Consulter les utilisateurs");
	        createPermission("USER_UPDATE", "Modifier un utilisateur");
	        createPermission("USER_DELETE", "Supprimer un utilisateur");

	    }

	    /**
	     * Création d'une permission si elle n'existe pas.
	     */
	    private void createPermission(String code, String description) {

	        if (permissionRepository.findByCode(code).isEmpty()) {

	            Permission permission = Permission.builder()
	                    .code(code)
	                    .description(description)
	                    .build();

	            permissionRepository.save(permission);

	        }

	    }

	    
	    private void assignPermission(String roleName, String permissionCode) {

	        Role role = roleRepository.findByName(roleName)
	                .orElseThrow(() -> new RoleNotFoundException("Rôle introuvable : " + roleName));

	        Permission permission = permissionRepository.findByCode(permissionCode)
	                .orElseThrow(() -> new PermissionNotFoundException("Permission introuvable : " + permissionCode));

	        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {

	            RolePermission rolePermission = RolePermission.builder()
	                    .role(role)
	                    .permission(permission)
	                    .createdAt(Instant.now())
	                    .build();

	            rolePermissionRepository.save(rolePermission);
	        }
	    }
	    /**
	     * Association des rôles et des permissions.
	     * Nous allons compléter cette méthode à l'étape suivante.
	     */
	    private void assignPermissions() {

	        // ===========================
	        // CLIENT
	        // ===========================

	        assignPermission("CLIENT", "AVIS_CREATE");
	        assignPermission("CLIENT", "AVIS_READ_OWN");

	        // ===========================
	        // MANAGER
	        // ===========================

	        assignPermission("MANAGER", "AVIS_CREATE");
	        assignPermission("MANAGER", "AVIS_READ_ALL");
	        assignPermission("MANAGER", "AVIS_DELETE");

	        // ===========================
	        // ADMIN
	        // ===========================

	        assignPermission("ADMIN", "AVIS_CREATE");
	        assignPermission("ADMIN", "AVIS_READ_OWN");
	        assignPermission("ADMIN", "AVIS_READ_ALL");
	        assignPermission("ADMIN", "AVIS_UPDATE");
	        assignPermission("ADMIN", "AVIS_DELETE");

	        assignPermission("ADMIN", "ROLE_CREATE");
	        assignPermission("ADMIN", "ROLE_READ");
	        assignPermission("ADMIN", "ROLE_UPDATE");
	        assignPermission("ADMIN", "ROLE_DELETE");
	        assignPermission("ADMIN", "ROLE_ASSIGN");

	        assignPermission("ADMIN", "PERMISSION_READ");
	        assignPermission("ADMIN", "PERMISSION_ASSIGN");

	        assignPermission("ADMIN", "USER_CREATE");
	        assignPermission("ADMIN", "USER_READ");
	        assignPermission("ADMIN", "USER_UPDATE");
	        assignPermission("ADMIN", "USER_DELETE");
	    }

	}