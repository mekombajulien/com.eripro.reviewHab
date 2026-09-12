package com.eriapro.gestionDesAvis.contoller;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import com.eriapro.gestionDesAvis.DTO.authDTO.UserAuth;
import com.eriapro.gestionDesAvis.DTO.refreshDTO.RequestRefreshTokenDTO;
import com.eriapro.gestionDesAvis.DTO.userDTO.UserRequestDTO;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.exception.UserNotFoundException;
import com.eriapro.gestionDesAvis.securite.JwtService;

import com.eriapro.gestionDesAvis.service.impl.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
	private final UserServiceImpl userServiceImpl;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	
	@PostMapping("/inscription")
	public ResponseEntity<Void> inscription(@Valid @RequestBody UserRequestDTO userRequestDTO) {
	
		 userServiceImpl.inscription(userRequestDTO);
		 return ResponseEntity.status(HttpStatus.CREATED).build();
		
	}
	
	@PostMapping("/activation")
	public void activation ( @RequestBody Map<String, String> codeActivation) {
		
		userServiceImpl.activation(codeActivation);

		
	}
	
	 @PostMapping("/refresh")
	    public Map<String,String>refresh( @RequestBody RequestRefreshTokenDTO request){


	        Map<String,String> tokens =
	                jwtService.refreshToken(
	                        request.getRefreshToken()
	                );
			return tokens;


	        

	    }
	
	
	@PostMapping("/updatePassword")
	public void updatePassword ( @RequestBody Map<String, String> email) {
		
		userServiceImpl.updatePassword(email);
	
	}
	
	
	@PostMapping("/newPassWord")
	public void newPassWord ( @RequestBody Map<String, String> email) {
		
		userServiceImpl.newPassWord(email);
	
	}
	
	@PostMapping("/connection")
	public Map<String, String> connection ( @RequestBody UserAuth user) {
		
	final	Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(user.username(),user.password()));
	
	if (authentication.isAuthenticated()) {
		
		Map<String, String> token = jwtService.generate(user.username());
		return token;
	}
		  

	throw new UserNotFoundException("Identifiant ou mot de passe incorrect");
	}
	
	
	@PostMapping("/deconnection")
	public void deconnection ( ) {
	
		jwtService.deconnection();
		
	}
	
	
	
	

}
