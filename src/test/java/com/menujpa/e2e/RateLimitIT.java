package com.menujpa.e2e;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Fuerza bruta contra /auth/login: el limite (5 intentos por minuto por IP) tiene que cortar ANTES
// de que se verifiquen las credenciales, asi que el sexto intento se rechaza aunque la contraseña
// sea la correcta. El contador es por IP y compartido con /auth/registrarse.
// Corre en su propio contexto de Spring (ver E2EBase), asi arranca con el contador en cero.
class RateLimitIT extends E2EBase {

    private static final String DEMASIADOS_INTENTOS = "Demasiados intentos. Esperá un minuto e intentá de nuevo.";

    @Test
    void login_sextoIntentoEnLaVentana_devuelve429AunqueLaContraseniaSeaCorrecta() {
        crearGerenteInicial("gerente.rl", "Gerente#2026");

        http.seccion("Cinco intentos con contraseña equivocada: pasan el filtro y los rechaza la autenticacion");
        for (int intento = 1; intento <= 5; intento++) {
            var res = login("gerente.rl", "adivinando-" + intento);
            assertThat(res.status()).as("intento %d", intento).isEqualTo(401);
            assertThat(res.cuerpo().path("error").asText()).isEqualTo("Usuario o contraseña incorrectos");
        }

        http.seccion("Sexto intento, con la contraseña correcta: lo corta el rate limit");
        var sexto = login("gerente.rl", "Gerente#2026");
        assertThat(sexto.status()).isEqualTo(429);
        assertThat(sexto.cuerpo().path("error").asText()).isEqualTo(DEMASIADOS_INTENTOS);
        assertThat(sexto.cuerpo().has("token")).isFalse();

        http.seccion("El mismo contador bloquea el registro desde esa IP");
        var registro = http.post("/api/v1/auth/registrarse", null, Map.of(
            "nombre", "Bot", "apellido", "Masivo", "usuario", "bot.1", "contrasenia", "Registro#2026"));
        assertThat(registro.status()).isEqualTo(429);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Long.class)).isZero();

        http.seccion("Las rutas que no son de autenticacion no quedan bloqueadas");
        assertThat(http.get("/api/v1/alimentos", null).status()).isEqualTo(200);
    }

    private ClienteHttp.Respuesta login(String usuario, String contrasenia) {
        return http.post("/api/v1/auth/login", null, Map.of("usuario", usuario, "contrasenia", contrasenia));
    }
}
