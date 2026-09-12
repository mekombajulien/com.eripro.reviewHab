package com.eriapro.gestionDesAvis.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.entite.Validation;
import com.eriapro.gestionDesAvis.repository.ValidationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl {
    private final ValidationRepository validationRepository;
    private final NotificationServiceImpl notificationServiceImpl;
	public void saveValidation(User userSave) {
	
		// verifier si utilisateur a deja  d abord une validation sinon lui en creer
		Validation validation = validationRepository.findByUser(userSave).orElseGet(()-> {
			
			Validation newValidation = new Validation();
			
			newValidation.setUser(userSave);
			
			return newValidation;

		});
		
		// mise a jour validation
		final Instant creation = Instant.now();	
		final Instant Expiration = creation.plus(Duration.ofMinutes(10));
		
		validation.setCreation(creation);
		validation.setExpiration(Expiration);
		 
		Random random = new Random();
		
		int randomInt = random.nextInt(999999);
		
		String code = String.format("%06d", randomInt);
		
		validation.setCode(code);
		// sauvegarde de validation
		
		Validation validationSave = validationRepository.save(validation);
		// envoi du mail 
		
		notificationServiceImpl.SendEmail(validationSave);
		
		log.info("verifier votre email vous avez recu un code d activation");

		
	}
	public Validation lireLeCode(String code) {
		
		return validationRepository.findByCode(code).orElseThrow(()-> new RuntimeException("votre code est incorrect"));
		
	}

}
