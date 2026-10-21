package com.menujpa.e2e;

import com.menujpa.entities.Gerente;
import com.menujpa.repositories.GerenteRepository;
import com.menujpa.security.PasswordHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;

// Base de los E2E: la app completa levantada en un puerto real, contra un MySQL 8 real en Docker.
// Un solo contenedor para toda la corrida (patron singleton); cada clase de test tiene su propio
// contexto de Spring (@DirtiesContext) para que el estado en memoria del RateLimitFilter no se
// filtre de una clase a otra, y create-drop deja el schema limpio en cada contexto.
// Sin Docker el test FALLA a proposito: un E2E que se saltea en silencio no prueba nada.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "jwt.secret=e2e-clave-solo-para-tests-de-al-menos-32-caracteres",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.generate_statistics=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class E2EBase {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void conectarMysql(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registro.add("spring.datasource.username", MYSQL::getUsername);
        registro.add("spring.datasource.password", MYSQL::getPassword);
    }

    @LocalServerPort
    int puerto;

    @Autowired
    GerenteRepository gerenteRepository;

    @Autowired
    JdbcTemplate jdbc;

    ClienteHttp http;

    @BeforeEach
    void crearClienteHttp() {
        http = new ClienteHttp(puerto);
    }

    @AfterEach
    void guardarReporte(TestInfo info) throws IOException {
        // Se escribe tambien cuando el test falla: ahi es cuando mas sirve ver la conversacion HTTP.
        http.escribirReporte(getClass().getSimpleName() + "-" + info.getTestMethod().orElseThrow().getName());
    }

    // No hay seed ni forma de crear el primer GERENTE por la API (crear staff exige ser GERENTE),
    // asi que es el unico dato que se inserta directo en la base. Todo lo demas pasa por HTTP.
    void crearGerenteInicial(String usuario, String contrasenia) {
        Gerente gerente = new Gerente();
        gerente.setNombre("Gerencia");
        gerente.setApellido("Inicial");
        gerente.setUsuario(usuario);
        gerente.setContrasenia(PasswordHasher.hashIfNeeded(contrasenia));
        gerenteRepository.save(gerente);
    }
}
