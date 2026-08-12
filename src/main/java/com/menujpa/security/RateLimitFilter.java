package com.menujpa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Limita intentos de login/registro por IP para frenar fuerza bruta y registros masivos.
// Ventana fija en memoria (no distribuida): suficiente para una instancia unica; en un
// despliegue multi-instancia habria que mover el contador a algo compartido (Redis, etc).
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> RUTAS_LIMITADAS = Set.of(
        "/api/v1/auth/login", "/api/v1/auth/registrarse");
    private static final int LIMITE_INTENTOS = 5;
    private static final long VENTANA_MS = 60_000;

    private final ConcurrentHashMap<String, Ventana> intentosPorIp = new ConcurrentHashMap<>();

    private static class Ventana {
        final AtomicInteger contador = new AtomicInteger(0);
        volatile long inicio = System.currentTimeMillis();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (RUTAS_LIMITADAS.contains(request.getRequestURI())) {
            Ventana ventana = intentosPorIp.computeIfAbsent(request.getRemoteAddr(), k -> new Ventana());
            long ahora = System.currentTimeMillis();
            synchronized (ventana) {
                if (ahora - ventana.inicio > VENTANA_MS) {
                    ventana.inicio = ahora;
                    ventana.contador.set(0);
                }
                if (ventana.contador.incrementAndGet() > LIMITE_INTENTOS) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Demasiados intentos. Esperá un minuto e intentá de nuevo.\"}");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
