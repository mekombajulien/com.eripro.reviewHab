package com.eriapro.gestionDesAvis.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eriapro.gestionDesAvis.entite.Avis;

public interface AvisRepository extends JpaRepository<Avis, Long> {
	
	 List<Avis> findByUserEmail(String email);   
	 Page<Avis> findByUserEmail(String email, Pageable pageable);
	 
	 
	 

}
