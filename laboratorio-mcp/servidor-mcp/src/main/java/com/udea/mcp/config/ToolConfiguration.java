package com.udea.mcp.config;

import com.udea.mcp.service.TareaToolsService;
import com.udea.mcp.service.SupermercadoToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ToolConfiguration {
    @Bean
    ToolCallbackProvider tools(TareaToolsService tareas, SupermercadoToolsService supermercado,
                               com.fasterxml.jackson.databind.ObjectMapper mapper) {
        ToolCallbackProvider methods = MethodToolCallbackProvider.builder().toolObjects(tareas, supermercado).build();
        org.springframework.ai.tool.ToolCallback[] callbacks = java.util.Arrays.stream(methods.getToolCallbacks())
                .map(callback -> new StrictToolCallback(callback, mapper))
                .toArray(org.springframework.ai.tool.ToolCallback[]::new);
        return () -> callbacks.clone();
    }
}
