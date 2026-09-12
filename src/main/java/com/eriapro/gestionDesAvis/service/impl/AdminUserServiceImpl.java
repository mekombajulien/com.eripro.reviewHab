package com.eriapro.gestionDesAvis.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserCreateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserUpdateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserResponseDTO;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.entite.UserRole;
import com.eriapro.gestionDesAvis.exception.RoleNotFoundException;
import com.eriapro.gestionDesAvis.exception.UserAlreadyExistsException;
import com.eriapro.gestionDesAvis.exception.UserNotFoundException;
import com.eriapro.gestionDesAvis.mapper.UserMapper;
import com.eriapro.gestionDesAvis.repository.RoleRepository;
import com.eriapro.gestionDesAvis.repository.UserRepository;
import com.eriapro.gestionDesAvis.repository.UserRoleRepository;
import com.eriapro.gestionDesAvis.service.AdminUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
    private static final Set<String> CHAMPS_TRIABLES = Set.of("id", "createdAt", "status");
    private static final String TRI_PAR_DEFAUT = "createdAt";
    private static final int TAILLE_MAX_PAGE = 50; // évite qu'un client demande size=100000

	@Override
	@Transactional
	public UserResponseDTO creer(AdminUserCreateDTO dto) {

		Optional<User> userExist =
				userRepository.findByEmailWithAuthorities(dto.getEmail());

		if (userExist.isPresent()) {
			throw new UserAlreadyExistsException(
					"Un utilisateur avec cet email existe déjà"
			);
		}

		String roleName =
				dto.getRole() != null ? dto.getRole() : "CLIENT";

		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() ->
						new RoleNotFoundException(
								"Le rôle " + roleName + " n'existe pas"
						)
				);

		User user = User.builder() 
				.name(dto.getName())
				.email(dto.getEmail())
				.password(passwordEncoder.encode(dto.getPassword()))
				.actif(true)
				.build();

		User userSave = userRepository.save(user);

		UserRole userRole = UserRole.builder()
				.user(userSave)
				.role(role)
				.createdAt(Instant.now())
				.build();

		userRoleRepository.save(userRole);

		return UserMapper.toUserResponseDTO(userSave);
	}

	@Override
	public Page<User> listerTous(int page, int size, String sortBy, String direction) {
		Pageable pageable = construirePageable(page, size, sortBy, direction);
        return userRepository.findAll(pageable);
	}
	
	 // méthode privée partagée — évite de dupliquer la logique de validation
    // dans mesAvis() et tousLesAvis()
    private Pageable construirePageable(int page, int size, String sortBy, String direction) {
        String champTri = CHAMPS_TRIABLES.contains(sortBy) ? sortBy : TRI_PAR_DEFAUT;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        int taillePage = Math.min(size, TAILLE_MAX_PAGE);
        int numeroPage = Math.max(page, 0);

        return PageRequest.of(numeroPage, taillePage, Sort.by(dir, champTri));
    }

	@Override
	public UserResponseDTO trouverParId(Long id) {

		User user = userRepository.findById(id)
				.orElseThrow(() ->
						new UserNotFoundException(
								"Utilisateur introuvable"
						)
				);

		return UserMapper.toUserResponseDTO(user);
	}

	@Override
	@Transactional
	public UserResponseDTO modifier(Long id, AdminUserUpdateDTO dto) {

		User user = userRepository.findById(id)
				.orElseThrow(() ->
						new UserNotFoundException(
								"Utilisateur introuvable"
						)
				);

		if (dto.getName() != null) {
			user.setName(dto.getName());
		}

		if (dto.getEmail() != null) {
			user.setEmail(dto.getEmail());
		}

		if (dto.getActif() != null) {
			user.setActif(dto.getActif());
		}

		if (dto.getRole() != null) {

			Role nouveauRole = roleRepository.findByName(dto.getRole())
					.orElseThrow(() ->
							new RoleNotFoundException(
									"Le rôle " + dto.getRole() + " n'existe pas"
							)
					);

			List<UserRole> ancienneAssociations =
					userRoleRepository.findByUser_Id(user.getId());

			userRoleRepository.deleteAll(ancienneAssociations);

			UserRole userRole = UserRole.builder()
					.user(user)
					.role(nouveauRole)
					.createdAt(Instant.now())
					.build();

			userRoleRepository.save(userRole);
		}

		User userSave = userRepository.save(user);

		return UserMapper.toUserResponseDTO(userSave);
	}

	@Override
	@Transactional
	public void supprimer(Long id) {

		if (!userRepository.existsById(id)) {
			throw new UserNotFoundException("Utilisateur introuvable");
		}

		userRepository.deleteById(id);
	}

}
