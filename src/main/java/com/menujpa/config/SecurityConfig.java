package com.menujpa.config;

import com.menujpa.security.JwtAuthenticationFilter;
import com.menujpa.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static final String[] GESTION_STAFF = {
        "/api/v1/chefs/**", "/api/v1/meseros/**", "/api/v1/gerentes/**",
        "/api/v1/despensas/**", "/api/v1/ingredientes/**", "/api/v1/pagos/**",
        "/api/v1/mesas/**", "/api/v1/clientes/**"
    };

    // Estas colecciones traen datos sensibles (salario, cedula, telefono, correo, usuario) que
    // no tenian ninguna proteccion: caian en el permitAll generico de GET /api/v1/**.
    private static final String[] DATOS_SENSIBLES_GET = {
        "/api/v1/pagos/**", "/api/v1/chefs/**", "/api/v1/meseros/**", "/api/v1/gerentes/**"
    };

    private static final String[] ACCIONES_PEDIDO_MESERO_O_GERENTE = {
        "/api/v1/pedidos/*/modificar", "/api/v1/pedidos/*/entregar", "/api/v1/pedidos/*/cancelar"
    };

    // Menu/Receta/Alimento estaban abiertos a cualquier rol autenticado (Cliente incluido) para
    // crear/editar/borrar. Se restringe a GERENTE (dueño del menu) y CHEF (dueño de recetas e
    // ingredientes de cocina), que son los roles que en el dominio real administran esto.
    private static final String[] GESTION_CATALOGO = {
        "/api/v1/menus/**", "/api/v1/recetas/**", "/api/v1/alimentos/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    // Paginas (Thymeleaf): la proteccion real esta en las llamadas a /api/v1/**
                    // que cada pagina hace, no en el HTML en si -- si no, nadie podria ni cargar
                    // /login para autenticarse.
                    "/", "/login", "/registro", "/menus", "/recetas", "/alimentos",
                    "/chefs", "/meseros", "/gerentes", "/clientes", "/ingredientes",
                    "/despensas", "/mesas", "/pedidos", "/pagos", "/reservas",
                    "/css/**", "/js/**", "/webjars/**",
                    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                    "/api/v1/auth/login", "/api/v1/auth/registrarse"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, DATOS_SENSIBLES_GET).hasRole("GERENTE")
                // Clientes tambien trae PII (cedula/telefono/correo), pero un MESERO necesita
                // listarlos para elegir a quien pertenece un pedido al tomarlo/modificarlo.
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAnyRole("GERENTE", "MESERO")
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/reservas/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/chefs/fichar/**").hasRole("CHEF")
                .requestMatchers(HttpMethod.POST, "/api/v1/meseros/fichar/**").hasRole("MESERO")
                .requestMatchers(HttpMethod.POST, GESTION_STAFF).hasRole("GERENTE")
                .requestMatchers(HttpMethod.PUT, GESTION_STAFF).hasRole("GERENTE")
                .requestMatchers(HttpMethod.DELETE, GESTION_STAFF).hasRole("GERENTE")
                .requestMatchers(HttpMethod.POST, GESTION_CATALOGO).hasAnyRole("GERENTE", "CHEF")
                .requestMatchers(HttpMethod.PUT, GESTION_CATALOGO).hasAnyRole("GERENTE", "CHEF")
                .requestMatchers(HttpMethod.DELETE, GESTION_CATALOGO).hasAnyRole("GERENTE", "CHEF")
                // Solo un mesero "toma" un pedido nuevo; el gerente supervisa los ya tomados
                // (modificar/entregar/cancelar), pero no origina pedidos -- no tiene fila en Mesero.
                .requestMatchers(HttpMethod.POST, "/api/v1/pedidos/tomar").hasRole("MESERO")
                .requestMatchers(ACCIONES_PEDIDO_MESERO_O_GERENTE).hasAnyRole("MESERO", "GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/v1/reservas/reservar").hasRole("CLIENTE")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
