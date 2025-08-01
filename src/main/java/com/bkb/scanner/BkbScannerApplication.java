package com.bkb.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")

public class BkbScannerApplication {
	public static void main(String[] args) {
		SpringApplication.run(BkbScannerApplication.class, args);
	}

	public static class PasswordEncoderTest {

		public static void main(String[] args) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String rawPassword = "password";
			String encodedPassword = encoder.encode(rawPassword);

			System.out.println("Generated BCrypt Hash: " + encodedPassword);
		}
	}
}