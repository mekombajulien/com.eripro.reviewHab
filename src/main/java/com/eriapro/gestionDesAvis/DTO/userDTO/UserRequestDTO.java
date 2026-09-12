package com.eriapro.gestionDesAvis.DTO.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UserRequestDTO {

	@NotBlank(message = "Le nom est obligatoire")
	private String name;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "Adresse e-mail invalide")
	private String email;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!?.*_])[A-Za-z\\d@#$%^&+=!?.*_]{8,}$",
			message = "Le mot de passe doit contenir une majuscule, une minuscule, un chiffre et un caractère spécial"
	)
	private String password;

}
