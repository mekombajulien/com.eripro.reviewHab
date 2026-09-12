package com.eriapro.gestionDesAvis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
	
	Optional<Permission> findByCode(String code);

}
