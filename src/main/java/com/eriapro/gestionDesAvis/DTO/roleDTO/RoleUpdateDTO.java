package com.eriapro.gestionDesAvis.DTO.roleDTO;

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
public class RoleUpdateDTO {

	private String name;
	private Boolean active;

}
