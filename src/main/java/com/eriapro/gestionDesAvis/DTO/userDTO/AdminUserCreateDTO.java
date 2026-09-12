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
public class AdminUserCreateDTO {

	private String name;
	private String email;
	private String password;

	/**
	 * Nom du rôle à attribuer (ex: "CLIENT", "MANAGER", "ADMIN").
	 * Si non fourni, le rôle CLIENT sera attribué par défaut.
	 */
	private String role;

}
