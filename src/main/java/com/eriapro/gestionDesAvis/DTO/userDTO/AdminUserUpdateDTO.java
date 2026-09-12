package com.eriapro.gestionDesAvis.DTO.userDTO;

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
public class AdminUserUpdateDTO {

	private String name;
	private String email;

	/**
	 * Activer ou désactiver le compte (bloque la connexion si false).
	 */
	private Boolean actif;

	/**
	 * Optionnel : renommer/changer le rôle de l'utilisateur.
	 * Si null, le rôle actuel est conservé.
	 */
	private String role;

}
