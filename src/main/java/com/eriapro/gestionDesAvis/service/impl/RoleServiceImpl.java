package com.eriapro.gestionDesAvis.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleCreateDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleResponseDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleUpdateDTO;
import com.eriapro.gestionDesAvis.entite.Permission;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.RolePermission;
import com.eriapro.gestionDesAvis.exception.PermissionNotFoundException;
import com.eriapro.gestionDesAvis.exception.RoleAlreadyExistsException;
import com.eriapro.gestionDesAvis.exception.RoleNotFoundException;
import com.eriapro.gestionDesAvis.exception.SystemRoleModificationException;
import com.eriapro.gestionDesAvis.mapper.RoleMapper;
import com.eriapro.gestionDesAvis.repository.PermissionRepository;
import com.eriapro.gestionDesAvis.repository.RolePermissionRepository;
import com.eriapro.gestionDesAvis.repository.RoleRepository;
import com.eriapro.gestionDesAvis.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final RolePermissionRepository rolePermissionRepository;

	@Override
	@Transactional
	public RoleResponseDTO creer(RoleCreateDTO dto) {

		if (roleRepository.findByName(dto.getName()).isPresent()) {
			throw new RoleAlreadyExistsException(
					"Un rôle nommé " + dto.getName() + " existe déjà"
			);
		}

		Role role = Role.builder()
				.name(dto.getName())
				.systemRole(false)
				.active(true)
				.build();

		Role roleSave = roleRepository.save(role);

		return RoleMapper.toResponseDTO(roleSave);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoleResponseDTO> listerTous() {

		return roleRepository.findAll()
				.stream()
				.map(RoleMapper::toResponseDTO)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RoleResponseDTO trouverParId(Long id) {

		Role role = trouverRoleOuLeverException(id);

		return RoleMapper.toResponseDTO(role);
	}

	@Override
	@Transactional
	public RoleResponseDTO modifier(Long id, RoleUpdateDTO dto) {

		Role role = trouverRoleOuLeverException(id);

		if (role.isSystemRole()) {
			throw new SystemRoleModificationException(
					"Le rôle " + role.getName() + " est un rôle système et ne peut pas être modifié"
			);
		}

		if (dto.getName() != null) {
			role.setName(dto.getName());
		}

		if (dto.getActive() != null) {
			role.setActive(dto.getActive());
		}

		Role roleSave = roleRepository.save(role);

		return RoleMapper.toResponseDTO(roleSave);
	}

	@Override
	@Transactional
	public void supprimer(Long id) {

		Role role = trouverRoleOuLeverException(id);

		if (role.isSystemRole()) {
			throw new SystemRoleModificationException(
					"Le rôle " + role.getName() + " est un rôle système et ne peut pas être supprimé"
			);
		}

		roleRepository.delete(role);
	}

	@Override
	@Transactional
	public RoleResponseDTO assignerPermission(Long roleId, String permissionCode) {

		Role role = trouverRoleOuLeverException(roleId);

		Permission permission = permissionRepository.findByCode(permissionCode)
				.orElseThrow(() ->
						new PermissionNotFoundException(
								"La permission " + permissionCode + " n'existe pas"
						)
				);

		if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {

			RolePermission rolePermission = RolePermission.builder()
					.role(role)
					.permission(permission)
					.createdAt(Instant.now())
					.build();

			rolePermissionRepository.save(rolePermission);
		}

		Role roleRafraichi = trouverRoleOuLeverException(roleId);

		return RoleMapper.toResponseDTO(roleRafraichi);
	}

	@Override
	@Transactional
	public RoleResponseDTO retirerPermission(Long roleId, String permissionCode) {

		Role role = trouverRoleOuLeverException(roleId);

		Permission permission = permissionRepository.findByCode(permissionCode)
				.orElseThrow(() ->
						new PermissionNotFoundException(
								"La permission " + permissionCode + " n'existe pas"
						)
				);

		rolePermissionRepository.findByRoleAndPermission(role, permission)
				.ifPresent(rolePermissionRepository::delete);

		Role roleRafraichi = trouverRoleOuLeverException(roleId);

		return RoleMapper.toResponseDTO(roleRafraichi);
	}


	private Role trouverRoleOuLeverException(Long id) {

		return roleRepository.findById(id)
				.orElseThrow(() ->
						new RoleNotFoundException("Rôle introuvable")
				);
	}

}
