package com.menujpa.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // Cualquier string de 32+ caracteres sirve como clave HMAC de prueba; en la app real viene de JWT_SECRET.
    private static final String SECRET_DE_TEST = "clave-de-test-de-al-menos-32-caracteres-de-largo";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_DE_TEST, 3600_000L);
    }

    @Test
    void generarToken_yParsearlo_devuelveElUsuarioYRolOriginales() {
        String token = jwtService.generarToken("jperez", "CLIENTE");

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("jperez");
        assertThat(claims.get("rol", String.class)).isEqualTo("CLIENTE");
    }

    @Test
    void esValido_conTokenBienFormado_devuelveTrue() {
        String token = jwtService.generarToken("jperez", "CLIENTE");

        assertThat(jwtService.esValido(token)).isTrue();
    }

    @Test
    void esValido_conTokenBasura_devuelveFalse() {
        assertThat(jwtService.esValido("esto-no-es-un-jwt")).isFalse();
    }

    @Test
    void esValido_conTokenFirmadoConOtraClave_devuelveFalse() {
        JwtService otroServicio = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres", 3600_000L);
        String token = otroServicio.generarToken("jperez", "CLIENTE");

        assertThat(jwtService.esValido(token)).isFalse();
    }

    @Test
    void esValido_conTokenExpirado_devuelveFalse() {
        JwtService servicioDeVidaCorta = new JwtService(SECRET_DE_TEST, -1000L);
        String token = servicioDeVidaCorta.generarToken("jperez", "CLIENTE");

        assertThat(jwtService.esValido(token)).isFalse();
    }
}
