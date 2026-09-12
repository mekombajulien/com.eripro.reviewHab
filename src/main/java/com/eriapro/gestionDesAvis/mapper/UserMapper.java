package com.eriapro.gestionDesAvis.mapper;

import com.eriapro.gestionDesAvis.DTO.userDTO.UserRequestDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserResponseDTO;
import com.eriapro.gestionDesAvis.entite.User;

public class UserMapper {
 
	public static User toEntity(UserRequestDTO userRequestDTO) {
		return User.builder()
				       .email(userRequestDTO.getEmail())
				       .name(userRequestDTO.getName())
				       .password(userRequestDTO.getPassword())
				       .build();
	}
	
	public static UserResponseDTO toUserResponseDTO(User user) {
		
		return UserResponseDTO.builder()
		                  .id(user.getId())
		                  .email(user.getEmail())
		                  .name(user.getName())
		                  .build();
	}
}
