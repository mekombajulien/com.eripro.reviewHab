package com.eriapro.gestionDesAvis.service;

import java.util.Map;

import com.eriapro.gestionDesAvis.DTO.userDTO.UserRequestDTO;

public interface UserService {

	void inscription(UserRequestDTO userRequestDTO);

	void activation(Map<String, String> codeActivation);

}
