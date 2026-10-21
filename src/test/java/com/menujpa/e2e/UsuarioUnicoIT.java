package com.menujpa.e2e;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// "usuario" no tiene UNIQUE global (cada rol es una tabla), y el login busca en orden
// Cliente > Chef > Mesero > Gerente. Si el gerente pudiera dar de alta personal con un usuario
// que ya existe en otra tabla, una de las dos cuentas quedaria tapada en el login. Se prueba por
// HTTP que ni el alta ni la edicion del personal pueden reusar un usuario de otro rol, y que la
// cuenta original sigue entrando con su rol.
//
// Presupuesto de rate limit: 3 llamadas a /auth/* (limite: 5 por minuto por IP).
class UsuarioUnicoIT extends E2EBase {

    @Test
    void personal_noPuedeReusarUnUsuarioDeOtroRol_niAlCrearNiAlEditar() {
        crearGerenteInicial("gerente.e2e", "Gerente#2026");
        String tokenGerente = login("gerente.e2e", "Gerente#2026", "GERENTE");

        http.seccion("Una clienta se registra sola");
        var registro = http.post("/api/v1/auth/registrarse", null, Map.of(
            "nombre", "Carla", "apellido", "Suarez",
            "usuario", "cliente.carla", "contrasenia", "Clienta#2026"));
        assertThat(registro.status()).isEqualTo(201);

        http.seccion("El gerente no puede crear personal con un usuario que ya usa otro rol");
        var meseroRepetido = http.post("/api/v1/meseros", tokenGerente, Map.of(
            "nombre", "Dario", "apellido", "Mesero", "usuario", "cliente.carla", "contrasenia", "Mesero#2026"));
        assertThat(meseroRepetido.status()).isEqualTo(400);
        assertThat(meseroRepetido.cuerpo().toString()).contains("ya está en uso");
        var chefComoGerente = http.post("/api/v1/chefs", tokenGerente, Map.of(
            "nombre", "Elena", "apellido", "Chef", "usuario", "gerente.e2e", "contrasenia", "ChefElena#2026"));
        assertThat(chefComoGerente.status()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mesero", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM chef", Long.class)).isZero();

        http.seccion("Con un usuario libre el alta funciona");
        var chef = http.post("/api/v1/chefs", tokenGerente, Map.of(
            "nombre", "Elena", "apellido", "Chef", "usuario", "chef.elena", "contrasenia", "ChefElena#2026"));
        assertThat(chef.status()).isEqualTo(200);
        long idChef = chef.cuerpo().path("id").asLong();

        http.seccion("Editar el personal tampoco puede pasarlo a un usuario tomado");
        var editarAUsuarioTomado = http.put("/api/v1/chefs/" + idChef, tokenGerente, Map.of(
            "nombre", "Elena", "apellido", "Chef", "usuario", "cliente.carla"));
        assertThat(editarAUsuarioTomado.status()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT usuario FROM chef WHERE id = ?", String.class, idChef)).isEqualTo("chef.elena");

        // Su propio usuario no cuenta como "tomado": editar otros datos sin tocarlo sigue funcionando.
        var editarSinCambiarUsuario = http.put("/api/v1/chefs/" + idChef, tokenGerente, Map.of(
            "nombre", "Elena María", "apellido", "Chef", "usuario", "chef.elena"));
        assertThat(editarSinCambiarUsuario.status()).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT nombre FROM chef WHERE id = ?", String.class, idChef)).isEqualTo("Elena María");

        http.seccion("La clienta original sigue entrando como CLIENTE");
        login("cliente.carla", "Clienta#2026", "CLIENTE");
    }

    private String login(String usuario, String contrasenia, String rolEsperado) {
        var res = http.post("/api/v1/auth/login", null, Map.of("usuario", usuario, "contrasenia", contrasenia));
        assertThat(res.status()).as("login de %s", usuario).isEqualTo(200);
        assertThat(res.cuerpo().path("rol").asText()).isEqualTo(rolEsperado);
        return res.cuerpo().path("token").asText();
    }
}
