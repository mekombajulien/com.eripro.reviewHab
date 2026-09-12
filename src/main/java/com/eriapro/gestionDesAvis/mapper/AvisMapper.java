package com.eriapro.gestionDesAvis.mapper;

import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisRequestDTO;
import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisResponseDTO;
import com.eriapro.gestionDesAvis.entite.Avis;

public class AvisMapper {

	public static Avis toEntity (AvisRequestDTO dto) {
		return Avis.builder()
		     .message(dto.getMessage())
		     .build();
		}
	
	public static AvisResponseDTO toResponseDTO(Avis avis) {

		return AvisResponseDTO.builder()
				.id(avis.getId())
				.message(avis.getMessage())
				.status(avis.getStatus())
				.createdAt(avis.getCreatedAt())
				.auteurNom(avis.getUser().getName())
				.auteurEmail(avis.getUser().getEmail())
				.build();
	}
}
