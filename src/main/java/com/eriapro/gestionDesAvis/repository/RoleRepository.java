package com.eriapro.gestionDesAvis.repository;

import java.util.Optional; 

import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.Role;



public interface RoleRepository extends JpaRepository<Role, Long> {
	

  Optional<Role> findByName(String name);

}
