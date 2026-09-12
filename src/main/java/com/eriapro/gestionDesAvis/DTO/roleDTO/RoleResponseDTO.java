package com.eriapro.gestionDesAvis.DTO.roleDTO;

import java.util.List;

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
public class RoleResponseDTO {

	private Long id;
	private String name;
	private boolean systemRole;
	private boolean active;

	/**
	 * Liste des codes de permissions attachées à ce rôle
	 * (ex: "AVIS_CREATE", "USER_READ"...).
	 */
	private List<String> permissions;

}
