package com.menujpa.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Cliente HTTP real (java.net.http, no MockMvc) que ademas deja registrado cada request/response
// en un reporte Markdown: el artefacto que queda en target/e2e-report/ despues de cada corrida.
// Se usa HttpClient en vez de TestRestTemplate porque este ultimo maneja mal los 401 sin cuerpo
// de desafio y esconde el status real detras de excepciones.
final class ClienteHttp {

    record Respuesta(int status, JsonNode cuerpo) {}

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private final String baseUrl;
    private final StringBuilder reporte = new StringBuilder();
    private int paso = 0;

    ClienteHttp(int puerto) {
        this.baseUrl = "http://localhost:" + puerto;
    }

    Respuesta get(String ruta, String token) {
        return enviar("GET", ruta, token, null);
    }

    Respuesta post(String ruta, String token, Object cuerpo) {
        return enviar("POST", ruta, token, cuerpo);
    }

    Respuesta put(String ruta, String token, Object cuerpo) {
        return enviar("PUT", ruta, token, cuerpo);
    }

    void seccion(String titulo) {
        reporte.append("\n## ").append(titulo).append("\n");
    }

    void escribirReporte(String nombre) throws IOException {
        Path destino = Path.of("target", "e2e-report", nombre + ".md");
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, "# E2E: " + nombre + "\n" + reporte, StandardCharsets.UTF_8);
    }

    private Respuesta enviar(String metodo, String ruta, String token, Object cuerpo) {
        try {
            String cuerpoJson = cuerpo == null ? null : json.writeValueAsString(cuerpo);
            HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .header("Content-Type", "application/json")
                .method(metodo, cuerpoJson == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(cuerpoJson));
            if (token != null) req.header("Authorization", "Bearer " + token);

            HttpResponse<String> res = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
            registrar(metodo, ruta, token, cuerpoJson, res);
            return new Respuesta(res.statusCode(), parsear(res.body()));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Fallo la llamada " + metodo + " " + ruta, e);
        }
    }

    private JsonNode parsear(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) return NullNode.getInstance();
        try {
            return json.readTree(cuerpo);
        } catch (IOException e) {
            return TextNode.valueOf(cuerpo);
        }
    }

    private void registrar(String metodo, String ruta, String token, String cuerpoJson, HttpResponse<String> res) {
        paso++;
        reporte.append("\n### ").append(paso).append(". ").append(metodo).append(' ').append(ruta).append('\n');
        reporte.append("- Authorization: ").append(token == null ? "(sin token)" : "Bearer " + ocultar(token)).append('\n');
        if (cuerpoJson != null) reporte.append("- Request: `").append(ocultarTokens(cuerpoJson)).append("`\n");
        reporte.append("- **Status: ").append(res.statusCode()).append("**\n");
        if (!res.body().isBlank()) reporte.append("- Response: `").append(ocultarTokens(res.body())).append("`\n");
    }

    // Los JWT cambian en cada corrida (iat/exp): se recortan para que el reporte sea comparable entre corridas.
    private static String ocultar(String token) {
        return token.length() <= 12 ? token : token.substring(0, 12) + "…";
    }

    private static String ocultarTokens(String texto) {
        return texto.replaceAll("\"token\":\"([^\"]{12})[^\"]*\"", "\"token\":\"$1…\"");
    }
}
