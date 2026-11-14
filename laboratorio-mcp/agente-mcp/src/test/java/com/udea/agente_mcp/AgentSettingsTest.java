package com.udea.agente_mcp;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentSettingsTest {
    @Test void defaultsDoNotRequireSecretsOrNetwork() {
        var settings = AgentSettings.from(Map.of());
        assertEquals("http://127.0.0.1:8088/mcp", settings.mcpUrl());
        assertEquals(8081, settings.port());
        assertEquals("gemini-2.5-flash", settings.model());
        var agent = TareaAgent.build(settings, List.of());
        assertEquals("TareaAgent", agent.name());
    }
    @Test void supportsExplicitConfiguration() {
        var settings = AgentSettings.from(Map.of("MCP_SERVER_URL", "https://example.com/mcp",
                "ADK_PORT", "9090", "GEMINI_MODEL", "modelo-configurado"));
        assertEquals(9090, settings.port());
        assertEquals("modelo-configurado", settings.model());
    }
    @Test void rejectsBadUrlsAndPorts() {
        for (String url : List.of("file:///tmp/mcp", "http://user:pass@localhost/mcp", "relative", "http://localhost/mcp#x"))
            assertThrows(IllegalArgumentException.class, () -> AgentSettings.from(Map.of("MCP_SERVER_URL", url)));
        for (String port : List.of("0", "65536", "abc"))
            assertThrows(IllegalArgumentException.class, () -> AgentSettings.from(Map.of("ADK_PORT", port)));
    }
    @Test void missingKeyFailsWithoutPrintingSecrets() {
        assertThrows(IllegalStateException.class, () -> AgentSettings.requireApiKey(Map.of()));
        assertThrows(IllegalStateException.class, () -> AgentSettings.requireApiKey(Map.of("GOOGLE_API_KEY", "  ")));
        assertDoesNotThrow(() -> AgentSettings.requireApiKey(Map.of("GOOGLE_API_KEY", "test-not-a-real-key")));
    }
}
