package com.servify.publicaciones.domain.service;

import com.servify.publicaciones.domain.model.PublicacionServicio;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PoliticaRelevanciaServicio {

    private static final Set<String> STOP_WORDS = Set.of(
            "necesito", "necesita", "busco", "quiero", "realizar", "hacer", "para",
            "con", "sin", "por", "una", "uno", "unos", "unas", "del", "las", "los",
            "servicio", "servicios", "trabajo", "problema", "solucionar", "arreglo",
            "arreglar", "reparacion", "reparar", "mantenimiento", "cambio", "cambiar",
            "urgente", "domicilio"
    );

    private static final Map<String, Set<String>> ALIAS_SERVICIO = Map.of(
            "gas", Set.of("gas", "gasista", "calefon", "calefones", "termotanque", "estufa", "hornalla"),
            "plomeria", Set.of("plomeria", "plomero", "plomera", "canilla", "bacha", "cano", "caño", "inodoro", "agua"),
            "electricidad", Set.of("electricidad", "electricista", "luz", "enchufe", "cable", "tablero"),
            "guitarra", Set.of("guitarra", "guitarrista"),
            "pintura", Set.of("pintura", "pintor", "pintora", "pintar"),
            "limpieza", Set.of("limpieza", "limpiar", "limpiador", "limpiadora")
    );

    public int calcularPuntaje(String necesidad, PublicacionServicio publicacion) {
        if (publicacion == null) {
            return 0;
        }
        Set<String> tokensNecesidad = tokensRelevantes(necesidad);
        if (tokensNecesidad.isEmpty()) {
            return 0;
        }

        Set<String> tokensTitulo = tokensRelevantes(publicacion.getTitulo());
        Set<String> tokensDescripcion = tokensRelevantes(publicacion.getDescripcion());

        int puntajeTitulo = contarInterseccion(tokensNecesidad, tokensTitulo) * 10;
        int puntajeDescripcion = contarInterseccion(tokensNecesidad, tokensDescripcion) * 4;
        return puntajeTitulo + puntajeDescripcion;
    }

    public Comparator<PublicacionServicio> compararPorRelevancia(String necesidad) {
        return Comparator
                .comparingInt((PublicacionServicio p) -> calcularPuntaje(necesidad, p))
                .reversed()
                .thenComparing(PublicacionServicio::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(PublicacionServicio::getId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private Set<String> tokensRelevantes(String texto) {
        if (texto == null || texto.isBlank()) {
            return Set.of();
        }
        return Stream.of(normalizarTexto(texto).split("\\s+"))
                .map(this::normalizarToken)
                .filter(token -> token != null && !token.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizarTexto(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase().replaceAll("[^a-z0-9ñ ]", " ");
    }

    private String normalizarToken(String token) {
        if (token == null) {
            return null;
        }
        String normalizado = singularizar(token.trim());
        if (normalizado.length() < 3 || STOP_WORDS.contains(normalizado)) {
            return null;
        }
        for (Map.Entry<String, Set<String>> grupo : ALIAS_SERVICIO.entrySet()) {
            if (grupo.getValue().contains(normalizado)) {
                return grupo.getKey();
            }
        }
        return normalizado;
    }

    private String singularizar(String token) {
        if (token.endsWith("ciones") && token.length() > 7) {
            return token.substring(0, token.length() - 6) + "cion";
        }
        if (token.endsWith("es") && token.length() > 5) {
            return token.substring(0, token.length() - 2);
        }
        if (token.endsWith("s") && token.length() > 4) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }

    private int contarInterseccion(Set<String> izquierda, Set<String> derecha) {
        int total = 0;
        for (String token : izquierda) {
            if (derecha.contains(token)) {
                total++;
            }
        }
        return total;
    }
}
