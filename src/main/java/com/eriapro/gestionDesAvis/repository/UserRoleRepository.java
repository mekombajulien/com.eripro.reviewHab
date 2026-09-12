package com.eriapro.gestionDesAvis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

	List<UserRole> findByUser_Id(Long userId);

}
