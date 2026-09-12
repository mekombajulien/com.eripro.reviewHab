package com.eriapro.gestionDesAvis.DTO.avisDTO;

import java.time.Instant;

import com.eriapro.gestionDesAvis.StatusAvis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class AvisResponseDTO {

	private Long id;
	private String message;
	private StatusAvis status;
	private Instant createdAt;

	/**
	 * j expose seulement le nom et l'email de l'auteur,
	 * jamais l'objet User complet (qui contient le mot de passe).
	 */
	private String auteurNom;
	private String auteurEmail;

}