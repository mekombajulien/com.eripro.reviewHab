package com.eriapro.gestionDesAvis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import com.eriapro.gestionDesAvis.entite.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("""
			SELECT DISTINCT u
			FROM User u
			LEFT JOIN FETCH u.userRoles ur
			LEFT JOIN FETCH ur.role r
			LEFT JOIN FETCH r.permissions rp
			LEFT JOIN FETCH rp.permission
			WHERE u.email = :email
			""")
			Optional<User> findByEmailWithAuthorities(@Param("email")String email);
}
