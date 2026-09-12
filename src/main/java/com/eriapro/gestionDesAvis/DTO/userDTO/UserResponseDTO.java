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
public class UserResponseDTO {
	private Long id ;
	private String name;
	private String email;

}
