package com.eriapro.gestionDesAvis.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserCreateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserUpdateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserResponseDTO;
import com.eriapro.gestionDesAvis.entite.User;

public interface AdminUserService {

	UserResponseDTO creer(AdminUserCreateDTO dto);



	UserResponseDTO trouverParId(Long id);

	UserResponseDTO modifier(Long id, AdminUserUpdateDTO dto);

	void supprimer(Long id);

	Page<User> listerTous(int page, int size, String sortBy, String direction);

}
