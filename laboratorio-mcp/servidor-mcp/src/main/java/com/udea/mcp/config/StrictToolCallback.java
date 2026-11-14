package com.udea.mcp.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/** Evita las coerciones permisivas de Jackson (1.9 a 1, "true" a true) en argumentos MCP. */
final class StrictToolCallback implements ToolCallback {
    private final ToolCallback delegate;
    private final ObjectMapper mapper;
    private final JsonNode schema;

    StrictToolCallback(ToolCallback delegate, ObjectMapper mapper) {
        this.delegate = delegate; this.mapper = mapper;
        try { schema = mapper.readTree(delegate.getToolDefinition().inputSchema()); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Esquema de herramienta inválido", ex); }
    }

    @Override public ToolDefinition getToolDefinition() { return delegate.getToolDefinition(); }
    @Override public ToolMetadata getToolMetadata() { return delegate.getToolMetadata(); }

    @Override public String call(String input) {
        try { validar(mapper.readTree(input), schema, "argumentos"); }
        catch (JsonProcessingException ex) { throw new IllegalArgumentException("Argumentos JSON inválidos", ex); }
        return delegate.call(input);
    }

    private void validar(JsonNode value, JsonNode definition, String ruta) {
        if (value == null || value.isNull()) return; // Obligatorios se comprueban en el objeto padre.
        String tipo = definition.path("type").asText();
        boolean valido = switch (tipo) {
            case "object" -> value.isObject();
            case "array" -> value.isArray();
            case "string" -> value.isTextual();
            case "boolean" -> value.isBoolean();
            case "integer" -> value.isIntegralNumber() && value.canConvertToLong();
            case "number" -> value.isNumber();
            default -> true;
        };
        if (!valido) throw new IllegalArgumentException(ruta + " debe ser de tipo " + tipo);
        if (value.isObject()) {
            for (JsonNode required : definition.path("required")) {
                JsonNode campo = value.get(required.asText());
                if (campo == null || campo.isNull()) throw new IllegalArgumentException(ruta + "." + required.asText() + " es obligatorio");
            }
            value.fields().forEachRemaining(field -> {
                JsonNode propiedad = definition.path("properties").get(field.getKey());
                if (propiedad == null) throw new IllegalArgumentException("Argumento desconocido: " + ruta + "." + field.getKey());
                validar(field.getValue(), propiedad, ruta + "." + field.getKey());
            });
        } else if (value.isArray()) {
            for (JsonNode item : value) {
                if (item.isNull()) throw new IllegalArgumentException(ruta + " no admite elementos nulos");
                validar(item, definition.path("items"), ruta + "[]");
            }
        }
    }
}
