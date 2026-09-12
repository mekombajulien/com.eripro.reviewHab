package com.eriapro.gestionDesAvis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.Permission;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
	
	 boolean existsByRoleAndPermission(Role role, Permission permission);

	 Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);
	 

}
