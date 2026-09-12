package com.eriapro.gestionDesAvis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class GestionDesAvisApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionDesAvisApplication.class, args);
	}

}
