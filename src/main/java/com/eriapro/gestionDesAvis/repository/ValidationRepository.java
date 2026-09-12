package com.eriapro.gestionDesAvis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.entite.Validation;
import java.util.List;
import java.util.Map;


public interface ValidationRepository extends JpaRepository<Validation, Long>{
	
	Optional<Validation>  findByUser( User user);
	Optional<Validation>  findByCode(String code);
	

}
