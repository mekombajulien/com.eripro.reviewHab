package com.eriapro.gestionDesAvis.contoller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserCreateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.AdminUserUpdateDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserResponseDTO;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.mapper.UserMapper;
import com.eriapro.gestionDesAvis.service.AdminUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService adminUserService;

	@PostMapping
	public ResponseEntity<UserResponseDTO> creer(
			@RequestBody AdminUserCreateDTO dto
	) {
		UserResponseDTO user = adminUserService.creer(dto);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(user);
	}

	@GetMapping
	public ResponseEntity<Page<UserResponseDTO>> listerTous(
			 @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size,
	            @RequestParam(defaultValue = "createdAt") String sortBy,
	            @RequestParam(defaultValue = "desc") String direction
			
			) {

		Page<UserResponseDTO> resultat = adminUserService
				                        .listerTous(page, size, sortBy, direction)
				                        .map(UserMapper::toUserResponseDTO);
		                                
		   return ResponseEntity.ok(resultat);
				
		
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDTO> trouverParId(
			@PathVariable Long id
	) {
		return ResponseEntity.ok(
				adminUserService.trouverParId(id)
		);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDTO> modifier(
			@PathVariable Long id,
			@RequestBody AdminUserUpdateDTO dto
	) {
		return ResponseEntity.ok(
				adminUserService.modifier(id, dto)
		);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> supprimer(
			@PathVariable Long id
	) {
		adminUserService.supprimer(id);

		return ResponseEntity.noContent().build();
	}

}
