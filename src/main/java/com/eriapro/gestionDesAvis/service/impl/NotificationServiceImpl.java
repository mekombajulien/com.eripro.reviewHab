package com.eriapro.gestionDesAvis.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.eriapro.gestionDesAvis.entite.Validation;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {
	
	final JavaMailSender mailSender;

	public void SendEmail(Validation validationSave) {
		
		SimpleMailMessage message = new SimpleMailMessage();
		
		message.setFrom("eriapro@proton.me");
		message.setTo(validationSave.getUser().getEmail());
		message.setSubject(" code de validation ");
		
		String messageform = String.format("bonjour %s , votre code de confirmation est %s",
				validationSave.getUser().getName() , 
				validationSave.getCode());
		message.setText(messageform);
		
		mailSender.send(message);
		   
		
		
	}
	
	
	

}
