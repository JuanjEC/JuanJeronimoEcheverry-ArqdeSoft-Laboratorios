package com.udea.mcp.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

final class Validaciones {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private Validaciones() {}

    static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " es obligatorio");
        String limpio = valor.strip();
        if (limpio.length() > maximo) throw new IllegalArgumentException(campo + " excede " + maximo + " caracteres");
        return limpio;
    }

    static Long id(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("El id debe ser positivo");
        return id;
    }

    static LocalDate fecha(String valor) {
        if (valor == null || !valor.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
            throw new IllegalArgumentException("La fecha debe tener formato ISO yyyy-MM-dd");
        try {
            LocalDate fecha = LocalDate.parse(valor, FECHA);
            if (fecha.getYear() < 1) throw new IllegalArgumentException("El año debe estar entre 0001 y 9999");
            return fecha;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha ISO yyyy-MM-dd no existe", ex);
        }
    }

    static void confirmar(boolean confirmado) {
        if (!confirmado) throw new IllegalArgumentException("Se requiere confirmación explícita del usuario (confirmado=true)");
    }
}
