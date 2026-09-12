package com.eriapro.gestionDesAvis.mapper;

import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleResponseDTO;
import com.eriapro.gestionDesAvis.entite.Role;

public class RoleMapper {

	public static RoleResponseDTO toResponseDTO(Role role) {

		return RoleResponseDTO.builder()
				.id(role.getId())
				.name(role.getName())
				.systemRole(role.isSystemRole())
				.active(role.isActive())
				.permissions(
						role.getPermissions()
								.stream()
								.map(rp -> rp.getPermission().getCode())
								.toList()
				)
				.build();
	}

}
