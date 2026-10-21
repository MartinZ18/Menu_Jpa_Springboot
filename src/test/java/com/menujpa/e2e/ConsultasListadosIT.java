package com.menujpa.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menujpa.entities.*;
import com.menujpa.repositories.*;
import com.menujpa.security.PasswordHasher;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

// Mide cuantas sentencias SQL dispara cada endpoint de listado (N+1) con un dataset mediano y
// despues con el doble de filas: la cantidad de queries tiene que ser chica y NO crecer con las filas.
// El conteo sale de las estadisticas de Hibernate (prepareStatementCount) de la app real, levantada
// en un puerto, contra MySQL real; los datos se siembran por repositorio (la API de alta de reservas
// exige un login por cliente y el rate limit de /auth permite 5 por minuto).
//
// Artefactos (variable de entorno MENUJPA_PERF_DIR, por defecto target/perf-report):
// counts.json con las queries por endpoint y tamanio, y el JSON de cada respuesta para poder
// comparar que la salida de la API no cambia entre versiones del codigo.
class ConsultasListadosIT extends E2EBase {

    private static final int MAX_QUERIES_POR_LISTADO = 3;

    @Autowired EntityManagerFactory emf;
    @Autowired AlimentoRepository alimentoRepository;
    @Autowired IngredienteRepository ingredienteRepository;
    @Autowired RecetaRepository recetaRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired DespensaRepository despensaRepository;
    @Autowired MesaRepository mesaRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired MeseroRepository meseroRepository;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired ReservaRepository reservaRepository;

    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Map<String, Integer>> conteos = new LinkedHashMap<>();
    private final Path destino = Path.of(Optional.ofNullable(System.getenv("MENUJPA_PERF_DIR")).orElse("target/perf-report"));

    @Test
    void listados_cantidadDeQueries_esChicaYNoCreceConLasFilas() throws IOException {
        crearGerenteInicial("gerente.perf", "Gerente#2026");
        var login = http.post("/api/v1/auth/login", null, Map.of("usuario", "gerente.perf", "contrasenia", "Gerente#2026"));
        assertThat(login.status()).isEqualTo(200);
        String tokenGerente = login.cuerpo().path("token").asText();

        Cliente demo = new Cliente();
        demo.setNombre("Demo");
        demo.setApellido("Cliente");
        demo.setUsuario("cliente.demo");
        demo.setContrasenia(PasswordHasher.hashIfNeeded("Cliente#2026"));
        demo = clienteRepository.save(demo);
        var loginCliente = http.post("/api/v1/auth/login", null, Map.of("usuario", "cliente.demo", "contrasenia", "Cliente#2026"));
        assertThat(loginCliente.status()).isEqualTo(200);
        String tokenCliente = loginCliente.cuerpo().path("token").asText();

        try {
            sembrar(0, demo);
            medir("1x", tokenGerente, tokenCliente);
            sembrar(1, demo);
            medir("2x", tokenGerente, tokenCliente);
        } finally {
            Files.createDirectories(destino);
            json.writerWithDefaultPrettyPrinter().writeValue(destino.resolve("counts.json").toFile(), conteos);
        }

        for (String listado : List.of("menus", "despensas", "pedidos", "reservas", "recetas", "reservas-cliente")) {
            Map<String, Integer> c = conteos.get(listado);
            assertThat(c.get("2x")).as("%s: queries con el doble de filas", listado).isEqualTo(c.get("1x"));
            assertThat(c.get("2x")).as("%s: queries por request", listado).isLessThanOrEqualTo(MAX_QUERIES_POR_LISTADO);
        }
    }

    private void medir(String tamanio, String tokenGerente, String tokenCliente) throws IOException {
        // Listados (se les exige un tope de queries) y lecturas puntuales/paginadas (solo se registran:
        // sirven para comparar el JSON y ver el costo, pero no son el N+1 que se persigue).
        llamar("menus", tamanio, "/api/v1/menus", tokenGerente);
        llamar("despensas", tamanio, "/api/v1/despensas", tokenGerente);
        llamar("pedidos", tamanio, "/api/v1/pedidos", tokenGerente);
        llamar("reservas", tamanio, "/api/v1/reservas", tokenGerente);
        llamar("recetas", tamanio, "/api/v1/recetas", tokenGerente);
        llamar("reservas-cliente", tamanio, "/api/v1/reservas", tokenCliente);
        llamar("menus-uno", tamanio, "/api/v1/menus/1", tokenGerente);
        llamar("despensas-uno", tamanio, "/api/v1/despensas/1", tokenGerente);
        llamar("pedidos-uno", tamanio, "/api/v1/pedidos/1", tokenGerente);
        llamar("reservas-uno", tamanio, "/api/v1/reservas/1", tokenGerente);
        llamar("recetas-uno", tamanio, "/api/v1/recetas/1", tokenGerente);
        llamar("menus-paged", tamanio, "/api/v1/menus/paged?size=50&sort=id", tokenGerente);
        llamar("recetas-paged", tamanio, "/api/v1/recetas/paged?size=50&sort=id", tokenGerente);
    }

    private void llamar(String nombre, String tamanio, String ruta, String token) throws IOException {
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        sf.getStatistics().clear();
        var res = http.get(ruta, token);
        long queries = sf.getStatistics().getPrepareStatementCount();
        conteos.computeIfAbsent(nombre, k -> new LinkedHashMap<>()).put(tamanio, (int) queries);
        Files.createDirectories(destino);
        Files.writeString(destino.resolve(nombre + "." + tamanio + ".json"),
            json.writerWithDefaultPrettyPrinter().writeValueAsString(res.cuerpo()));
        assertThat(res.status()).as("GET %s", ruta).isEqualTo(200);
    }

    // Una "tanda" = 10 menus x 5 recetas x 4 alimentos, 10 despensas x 5 ingredientes, 20 pedidos
    // con 3 alimentos y 2 clientes, 20 reservas sobre mesas distintas (las 3 primeras son del cliente demo).
    private void sembrar(int tanda, Cliente demo) {
        Gerente gerente = gerenteRepository.findByUsuario("gerente.perf").orElseThrow();
        int base = tanda * 1000;

        List<Alimento> alimentos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Alimento a = new Alimento();
            a.setNombre("Alimento " + (base + i));
            a.setPrecio(1000.0 + i);
            alimentos.add(alimentoRepository.save(a));
        }
        List<Receta> recetas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Receta r = new Receta();
            r.setNombreReceta("Receta " + (base + i));
            r.setDescripcion("Descripcion " + i);
            r.setTiempo("30 min");
            r.setDificultad("Media");
            for (int k = 0; k < 4; k++) r.getAlimentosSeleccionados().add(alimentos.get((i + k) % alimentos.size()));
            recetas.add(recetaRepository.save(r));
        }
        for (int i = 0; i < 10; i++) {
            Menu m = new Menu();
            m.setNombre("Menu " + (base + i));
            m.setGerente(gerente);
            for (int k = 0; k < 5; k++) m.getRecetas().add(recetas.get(i * 5 + k));
            menuRepository.save(m);
        }

        List<Ingrediente> ingredientes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Ingrediente ing = new Ingrediente();
            ing.setDescripcion("Ingrediente " + (base + i));
            ing.setCantidadStock(10 + i);
            ingredientes.add(ingredienteRepository.save(ing));
        }
        for (int i = 0; i < 10; i++) {
            Despensa d = new Despensa();
            for (int k = 0; k < 5; k++) d.getIngredientes().add(ingredientes.get(i * 5 + k));
            despensaRepository.save(d);
        }

        List<Mesero> meseros = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Mesero m = new Mesero();
            m.setNombre("Mesero " + (base + i));
            m.setApellido("Perf");
            m.setUsuario("mesero.perf." + (base + i));
            meseros.add(meseroRepository.save(m));
        }
        List<Cliente> clientes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Cliente c = new Cliente();
            c.setNombre("Cliente " + (base + i));
            c.setApellido("Perf");
            c.setUsuario("cliente.perf." + (base + i));
            clientes.add(clienteRepository.save(c));
        }
        Date fecha = new GregorianCalendar(2026, Calendar.JUNE, 15).getTime();
        for (int i = 0; i < 20; i++) {
            Pedido p = new Pedido();
            p.getClientes().add(clientes.get(i));
            p.getClientes().add(clientes.get((i + 1) % 20));
            p.getMeseros().add(meseros.get(i % 3));
            for (int k = 0; k < 3; k++) p.getAlimentosAdquiridos().add(alimentos.get((i + k) % alimentos.size()));
            p.setFechaPedido(fecha);
            p.setHoraPedido("12:30");
            p.setPrecioTotalPedido(3000.0 + i);
            pedidoRepository.save(p);
        }
        for (int i = 0; i < 20; i++) {
            Mesa mesa = new Mesa();
            mesa.setNumero(base + i + 1);
            mesa.setCapacidad(4);
            mesaRepository.save(mesa);
            Reserva r = new Reserva();
            r.setCliente(i < 3 ? demo : clientes.get(i));
            r.setMesa(mesa);
            r.setFecha(fecha);
            r.setHoraInicio("20:00");
            r.setHoraFin("22:00");
            r.setCantidadPersonas(2);
            reservaRepository.save(r);
        }
    }
}
