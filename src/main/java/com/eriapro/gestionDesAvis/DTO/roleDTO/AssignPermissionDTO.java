package com.eriapro.gestionDesAvis.DTO.roleDTO;

import jakarta.validation.constraints.NotBlank;
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
public class AssignPermissionDTO {

	@NotBlank(message = "Le code de la permission est obligatoire")
	private String permissionCode;

}
