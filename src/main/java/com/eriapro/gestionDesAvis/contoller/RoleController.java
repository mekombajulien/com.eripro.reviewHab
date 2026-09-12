package com.eriapro.gestionDesAvis.contoller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eriapro.gestionDesAvis.DTO.roleDTO.AssignPermissionDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleCreateDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleResponseDTO;
import com.eriapro.gestionDesAvis.DTO.roleDTO.RoleUpdateDTO;
import com.eriapro.gestionDesAvis.service.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleService roleService;

	@PostMapping
	public ResponseEntity<RoleResponseDTO> creer(
			@Valid @RequestBody RoleCreateDTO dto
	) { 
		RoleResponseDTO role = roleService.creer(dto);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(role);
	}

	@GetMapping
	public ResponseEntity<List<RoleResponseDTO>> listerTous() {

		return ResponseEntity.ok(
				roleService.listerTous()
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<RoleResponseDTO> trouverParId(
			@PathVariable Long id
	) {
		return ResponseEntity.ok(
				roleService.trouverParId(id)
		);
	}

	@PutMapping("/{id}")
	public ResponseEntity<RoleResponseDTO> modifier(
			@PathVariable Long id,
			@RequestBody RoleUpdateDTO dto
	) {
		return ResponseEntity.ok(
				roleService.modifier(id, dto)
		);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> supprimer(
			@PathVariable Long id
	) {
		roleService.supprimer(id);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/permissions")
	public ResponseEntity<RoleResponseDTO> assignerPermission(
			@PathVariable Long id,
			@Valid @RequestBody AssignPermissionDTO dto
	) {
		return ResponseEntity.ok(
				roleService.assignerPermission(id, dto.getPermissionCode())
		);
	}

	@DeleteMapping("/{id}/permissions/{code}")
	public ResponseEntity<RoleResponseDTO> retirerPermission(
			@PathVariable Long id,
			@PathVariable String code
	) {
		return ResponseEntity.ok(
				roleService.retirerPermission(id, code)
		);
	}

}
