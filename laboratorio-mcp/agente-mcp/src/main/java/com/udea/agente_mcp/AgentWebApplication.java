package com.udea.agente_mcp;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;
import com.google.adk.web.AdkWebServer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public final class AgentWebApplication {
    private AgentWebApplication() {}

    public static void main(String[] args) throws Exception {
        boolean checkOnly = Arrays.asList(args).contains("--check-mcp");
        AgentSettings settings = AgentSettings.from(System.getenv());
        if (!checkOnly) AgentSettings.requireApiKey(System.getenv());

        McpToolset toolset = new McpToolset(StreamableHttpServerParameters.builder()
                .url(settings.mcpUrl()).timeout(Duration.ofSeconds(30)).build());
        boolean started = false;
        try {
            List<BaseTool> tools = toolset.getTools(null).toList().blockingGet();
            if (tools.isEmpty()) throw new IllegalStateException("El servidor MCP no publicó herramientas");
            System.out.println("Herramientas MCP descubiertas: " + tools.size());
            tools.forEach(tool -> System.out.println("  " + tool.name()));
            if (checkOnly) return;

            // start(BaseAgent...) es la API pública de ADK 1.0.0 y registra AgentStaticLoader.
            System.setProperty("server.address", "127.0.0.1");
            System.setProperty("server.port", Integer.toString(settings.port()));
            LlmAgent agent = TareaAgent.build(settings, tools);
            Runtime.getRuntime().addShutdownHook(new Thread(toolset::close, "mcp-session-close"));
            AdkWebServer.start(agent);
            started = true;
            System.out.println("Chatbot ADK: http://127.0.0.1:" + settings.port() + "/dev-ui");
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible iniciar el cliente. Verifica el servidor MCP, su URL y la configuración ADK.", e);
        } finally {
            if (!started) toolset.close();
        }
    }
}
