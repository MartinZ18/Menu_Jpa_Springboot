package com.menujpa.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.regex.Pattern;

public final class PasswordHasher {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordHasher() {}

    public static String hashIfNeeded(String contrasenia) {
        if (contrasenia == null || contrasenia.isBlank()) return contrasenia;
        if (BCRYPT_PATTERN.matcher(contrasenia).matches()) return contrasenia;
        return ENCODER.encode(contrasenia);
    }
}
