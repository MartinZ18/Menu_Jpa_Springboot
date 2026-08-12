package com.menujpa.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {

    @Test
    void hashIfNeeded_conTextoPlano_loHashea() {
        String hash = PasswordHasher.hashIfNeeded("miClave123");

        assertThat(hash).isNotEqualTo("miClave123");
        assertThat(new BCryptPasswordEncoder().matches("miClave123", hash)).isTrue();
    }

    @Test
    void hashIfNeeded_conHashExistente_loDejaIgual() {
        String hashOriginal = PasswordHasher.hashIfNeeded("miClave123");

        String resultado = PasswordHasher.hashIfNeeded(hashOriginal);

        assertThat(resultado).isEqualTo(hashOriginal);
    }

    @Test
    void hashIfNeeded_conNull_devuelveNull() {
        assertThat(PasswordHasher.hashIfNeeded(null)).isNull();
    }

    @Test
    void hashIfNeeded_conVacio_loDejaIgual() {
        assertThat(PasswordHasher.hashIfNeeded("")).isEqualTo("");
    }

    @Test
    void matches_conLaContraseniaCorrecta_devuelveTrue() {
        String hash = PasswordHasher.hashIfNeeded("miClave123");

        assertThat(PasswordHasher.matches("miClave123", hash)).isTrue();
    }

    @Test
    void matches_conLaContraseniaIncorrecta_devuelveFalse() {
        String hash = PasswordHasher.hashIfNeeded("miClave123");

        assertThat(PasswordHasher.matches("otraClave", hash)).isFalse();
    }

    @Test
    void matches_conAlgunArgumentoNull_devuelveFalse() {
        assertThat(PasswordHasher.matches(null, "hash")).isFalse();
        assertThat(PasswordHasher.matches("clave", null)).isFalse();
    }
}
