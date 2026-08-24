package com.sistema.bitacora;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class GeneradorHashTest {

	@Test
	void generarHash() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String passwordPlano = "password123";
		String hashGenerado = encoder.encode(passwordPlano);

		System.out.println("=========================================");
		System.out.println("TU HASH GENERADO: " + hashGenerado);
		System.out.println("=========================================");
	}
}