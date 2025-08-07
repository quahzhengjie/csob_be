package com.bkb.scanner.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- HASHING (during signup or password change) ---
        String rawPassword = "password123";
        String storedHashInDB = encoder.encode(rawPassword);
        System.out.println("Stored Hash: " + storedHashInDB);


        // --- VERIFICATION (during login) ---
        // A user tries to log in with "password"
        String loginAttemptPassword = "password123";

        // The .matches() method handles everything
        boolean isMatch = encoder.matches(loginAttemptPassword, storedHashInDB);

        System.out.println("Passwords match: " + isMatch); // This will print "true"

        // --- An incorrect attempt ---
        String wrongPassword = "password123";
        boolean isMatchWrong = encoder.matches(wrongPassword, storedHashInDB);
        System.out.println("Passwords match: " + isMatchWrong); // This will print "false"
    }
}