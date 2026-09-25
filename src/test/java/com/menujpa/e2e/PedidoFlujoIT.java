package com.menujpa.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

// Recorre el ciclo de vida de un pedido de punta a punta por HTTP, con usuarios reales de cada rol:
// autenticacion (401) -> autorizacion por rol (403) -> precio calculado desde la base ->
// guardia de propiedad entre meseros (IDOR) -> entrega unica. Cada efecto se verifica tambien
// leyendo MySQL directo, para no confiar solo en lo que responde la propia API.
//
// Presupuesto de rate limit: este flujo hace 4 llamadas a /auth/* (limite: 5 por minuto por IP).
class PedidoFlujoIT extends E2EBase {

    private static final String ENDPOINT_TOMAR = "/api/v1/pedidos/tomar";

    @Test
    void pedido_meseroTomaYEntrega_respetaAutenticacionRolesYPropiedad() {
        crearGerenteInicial("gerente.e2e", "Gerente#2026");

        http.seccion("El gerente arma el escenario por la API");
        String tokenGerente = login("gerente.e2e", "Gerente#2026", "GERENTE");

        var contraseniaCorta = http.post("/api/v1/meseros", tokenGerente, Map.of(
            "nombre", "Dario", "apellido", "Mesero", "usuario", "mesero.dario", "contrasenia", "corta"));
        assertThat(contraseniaCorta.status()).isEqualTo(400);
        // Sin contraseña la cuenta nunca podria iniciar sesion: el alta se rechaza aunque venga de
        // fuera del formulario (Swagger, curl), no solo desde la validacion del lado del cliente.
        var sinContrasenia = http.post("/api/v1/meseros", tokenGerente, Map.of(
            "nombre", "Dario", "apellido", "Mesero", "usuario", "mesero.dario"));
        assertThat(sinContrasenia.status()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mesero", Long.class)).isZero();

        crearMesero(tokenGerente, "Ana", "mesero.ana", "MeseroAna#2026");
        crearMesero(tokenGerente, "Bruno", "mesero.bruno", "MeseroBruno#2026");
        var meseros = http.get("/api/v1/meseros", tokenGerente);
        assertThat(meseros.status()).isEqualTo(200);
        assertThat(meseros.cuerpo().findValues("contrasenia")).as("el listado expone contraseñas").isEmpty();
        long milanesa = crearAlimento(tokenGerente, "Milanesa napolitana", 8500.0);
        long limonada = crearAlimento(tokenGerente, "Limonada", 2300.5);

        http.seccion("Una clienta se registra sola");
        var registro = http.post("/api/v1/auth/registrarse", null, Map.of(
            "nombre", "Carla", "apellido", "Suarez",
            "usuario", "cliente.carla", "contrasenia", "Clienta#2026"));
        assertThat(registro.status()).isEqualTo(201);
        assertThat(registro.cuerpo().path("rol").asText()).isEqualTo("CLIENTE");
        String tokenClienta = registro.cuerpo().path("token").asText();

        http.seccion("Los meseros creados por el gerente pueden iniciar sesion");
        String tokenAna = login("mesero.ana", "MeseroAna#2026", "MESERO");
        String tokenBruno = login("mesero.bruno", "MeseroBruno#2026", "MESERO");

        http.seccion("Nadie sin rol MESERO puede tomar un pedido");
        var pedidoValido = Map.of("clienteIds", List.of(1), "alimentoIds", List.of(milanesa));

        var sinToken = http.post(ENDPOINT_TOMAR, null, pedidoValido);
        assertThat(sinToken.status()).isEqualTo(401);
        assertThat(sinToken.cuerpo().path("error").asText()).isEqualTo("No autenticado. Iniciá sesión de nuevo.");

        var tokenAdulterado = http.post(ENDPOINT_TOMAR, adulterarFirma(tokenAna), pedidoValido);
        assertThat(tokenAdulterado.status()).isEqualTo(401);

        var comoClienta = http.post(ENDPOINT_TOMAR, tokenClienta, pedidoValido);
        assertThat(comoClienta.status()).isEqualTo(403);
        assertThat(comoClienta.cuerpo().path("error").asText()).isEqualTo("No tenés permiso para esta acción.");

        var comoGerente = http.post(ENDPOINT_TOMAR, tokenGerente, pedidoValido);
        assertThat(comoGerente.status()).isEqualTo(403);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM pedido", Long.class)).isZero();

        http.seccion("Ana toma un pedido para Carla, con la limonada dos veces");
        var clientes = http.get("/api/v1/clientes", tokenAna);
        assertThat(clientes.status()).isEqualTo(200);
        long carla = idDe(clientes.cuerpo(), "usuario", "cliente.carla");

        var tomado = http.post(ENDPOINT_TOMAR, tokenAna, Map.of(
            "clienteIds", List.of(carla),
            "alimentoIds", List.of(milanesa, limonada, limonada)));
        assertThat(tomado.status()).isEqualTo(201);
        long pedido = tomado.cuerpo().path("id").asLong();
        assertThat(tomado.cuerpo().path("precioTotalPedido").asDouble()).isEqualTo(13101.0);
        assertThat(tomado.cuerpo().path("estado").asBoolean()).isFalse();

        assertThat(jdbc.queryForObject(
            "SELECT precio_total_pedido FROM pedido WHERE id = ?", Double.class, pedido)).isEqualTo(13101.0);
        assertThat(jdbc.queryForObject(
            "SELECT COUNT(*) FROM pedido_alimento WHERE id_pedido = ?", Long.class, pedido)).isEqualTo(3L);

        http.seccion("Bruno intenta entregar el pedido de Ana");
        var entregaAjena = http.post("/api/v1/pedidos/" + pedido + "/entregar", tokenBruno, null);
        assertThat(entregaAjena.status()).isEqualTo(400);
        assertThat(entregaAjena.cuerpo().path("error").asText())
            .isEqualTo("No podés modificar un pedido que no tomaste vos.");
        assertThat(estadoEnBase(pedido)).isFalse();

        http.seccion("Ana entrega su pedido una sola vez");
        var entrega = http.post("/api/v1/pedidos/" + pedido + "/entregar", tokenAna, null);
        assertThat(entrega.status()).isEqualTo(200);
        assertThat(entrega.cuerpo().path("estado").asBoolean()).isTrue();
        assertThat(estadoEnBase(pedido)).isTrue();

        var segundaEntrega = http.post("/api/v1/pedidos/" + pedido + "/entregar", tokenAna, null);
        assertThat(segundaEntrega.status()).isEqualTo(400);
        assertThat(segundaEntrega.cuerpo().path("error").asText()).isEqualTo("El pedido ya fue entregado.");
    }

    private String login(String usuario, String contrasenia, String rolEsperado) {
        var res = http.post("/api/v1/auth/login", null, Map.of("usuario", usuario, "contrasenia", contrasenia));
        assertThat(res.status()).as("login de %s", usuario).isEqualTo(200);
        assertThat(res.cuerpo().path("rol").asText()).isEqualTo(rolEsperado);
        return res.cuerpo().path("token").asText();
    }

    private void crearMesero(String tokenGerente, String nombre, String usuario, String contrasenia) {
        var res = http.post("/api/v1/meseros", tokenGerente, Map.of(
            "nombre", nombre, "apellido", "Mesero", "usuario", usuario, "contrasenia", contrasenia));
        assertThat(res.status()).as("alta de mesero %s", usuario).isEqualTo(200);
        // La contraseña se acepta al crear pero jamas debe volver en una respuesta, ni siquiera hasheada.
        assertThat(res.cuerpo().has("contrasenia")).as("respuesta del alta expone la contraseña").isFalse();
        assertThat(jdbc.queryForObject("SELECT contrasenia FROM mesero WHERE usuario = ?", String.class, usuario))
            .as("la contraseña se guarda hasheada").startsWith("$2");
    }

    private long crearAlimento(String tokenGerente, String nombre, double precio) {
        var res = http.post("/api/v1/alimentos", tokenGerente, Map.of("nombre", nombre, "precio", precio));
        assertThat(res.status()).as("alta de alimento %s", nombre).isEqualTo(200);
        return res.cuerpo().path("id").asLong();
    }

    private boolean estadoEnBase(long pedido) {
        return jdbc.queryForObject("SELECT estado FROM pedido WHERE id = ?", Boolean.class, pedido);
    }

    private static long idDe(JsonNode lista, String campo, String valor) {
        return StreamSupport.stream(lista.spliterator(), false)
            .filter(n -> valor.equals(n.path(campo).asText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No aparece " + campo + "=" + valor + " en " + lista))
            .path("id").asLong();
    }

    // Cambia un caracter de la firma: el token sigue teniendo formato valido pero ya no verifica.
    private static String adulterarFirma(String token) {
        char ultimo = token.charAt(token.length() - 2);
        return token.substring(0, token.length() - 2) + (ultimo == 'A' ? 'B' : 'A') + token.charAt(token.length() - 1);
    }
}
