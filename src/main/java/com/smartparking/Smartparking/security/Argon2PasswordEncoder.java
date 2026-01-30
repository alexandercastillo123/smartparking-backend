package com.smartparking.Smartparking.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordEncoder implements PasswordEncoder {

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Override
    public String encode(CharSequence rawPassword) {
        // Reduced parameters for low-resource environments (Koyeb free tier)
        // iterations: 2 (was 10), memory: 15MB (was 64MB), parallelism: 1
        return argon2.hash(2, 15360, 1, rawPassword.toString().toCharArray());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return argon2.verify(encodedPassword, rawPassword.toString().toCharArray());
    }
}