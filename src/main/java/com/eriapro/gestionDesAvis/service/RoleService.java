package com.eriapro.gestionDesAvis.service;

import java.util.List;

import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleCreateDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleResponseDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleUpdateDTO;

public interface RoleService {

	RoleResponseDTO creer(RoleCreateDTO dto);

	List<RoleResponseDTO> listerTous();

	RoleResponseDTO trouverParId(Long id);

	RoleResponseDTO modifier(Long id, RoleUpdateDTO dto);

	void supprimer(Long id);

	RoleResponseDTO assignerPermission(Long roleId, String permissionCode);

	RoleResponseDTO retirerPermission(Long roleId, String permissionCode);

}
