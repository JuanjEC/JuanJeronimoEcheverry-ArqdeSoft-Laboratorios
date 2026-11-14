package com.udea.agente_mcp;

import java.net.URI;
import java.util.Map;

/** Configuración sin secretos persistidos ni carga implícita de archivos .env. */
public record AgentSettings(String mcpUrl, String model, int port) {
    public static AgentSettings from(Map<String, String> env) {
        String url = value(env, "MCP_SERVER_URL", "http://127.0.0.1:8088/mcp");
        URI uri = URI.create(url);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("MCP_SERVER_URL debe ser una URL HTTP(S) sin credenciales ni fragmento");
        }
        int port;
        try { port = Integer.parseInt(value(env, "ADK_PORT", "8081")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("ADK_PORT debe ser un puerto numérico", e); }
        if (port < 1 || port > 65535) throw new IllegalArgumentException("ADK_PORT fuera de rango");
        return new AgentSettings(url, value(env, "GEMINI_MODEL", "gemini-2.5-flash"), port);
    }

    private static String value(Map<String, String> env, String key, String fallback) {
        String value = env.get(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static void requireApiKey(Map<String, String> env) {
        String key = env.get("GOOGLE_API_KEY");
        if (key == null || key.isBlank())
            throw new IllegalStateException("Define GOOGLE_API_KEY en la terminal que inicia el agente. Para probar sólo MCP usa --check-mcp.");
    }
}
