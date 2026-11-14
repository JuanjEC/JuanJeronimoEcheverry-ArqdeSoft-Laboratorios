package com.udea.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.mcp.model.Producto;
import com.udea.mcp.repository.*;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ToolContractTest {
    @Autowired ToolCallbackProvider provider;
    @Autowired ObjectMapper mapper;
    @Autowired ProductoRepository productos;
    @Autowired PedidoRepository pedidos;
    @Autowired org.springframework.context.ApplicationContext context;

    @Test void seedDesactivadoPorDefecto() {
        assertThat(context.getBeansOfType(com.udea.mcp.config.DemoSeeder.class)).isEmpty();
    }

    @Test void registraExactamenteLasCatorceHerramientas() {
        assertThat(Arrays.stream(provider.getToolCallbacks()).map(t -> t.getToolDefinition().name())).containsExactlyInAnyOrder(
                "listarTareas", "listarPendientes", "listarCompletadas", "contarTareas", "buscarTareas", "crearTarea",
                "marcarCompletada", "actualizarTarea", "eliminarTarea", "listarProductos", "buscarProductos",
                "crearPedido", "consultarPedido", "cancelarPedido");
    }

    @Test void esquemaExponeConfirmacionYLineasTipadas() throws Exception {
        JsonNode schema = mapper.readTree(tool("crearPedido").getToolDefinition().inputSchema());
        assertThat(schema.get("required")).contains(mapper.valueToTree("confirmado"), mapper.valueToTree("items"));
        assertThat(schema.at("/properties/confirmado/type").asText()).isEqualTo("boolean");
        assertThat(schema.at("/properties/items/type").asText()).isEqualTo("array");
        assertThat(schema.at("/properties/items/items/properties/productoId/type").asText()).isEqualTo("integer");
        assertThat(schema.at("/properties/items/items/properties/cantidad/type").asText()).isEqualTo("integer");
        JsonNode actualizacion = mapper.readTree(tool("actualizarTarea").getToolDefinition().inputSchema());
        assertThat(actualizacion.get("required").toString()).isEqualTo("[\"id\"]");
    }

    @Test void invocacionJsonUsaDtoYTransaccionYRechazaConfirmacionAusente() throws Exception {
        pedidos.deleteAll(); productos.deleteAll();
        Producto producto = productos.saveAndFlush(new Producto("Prueba callback", new BigDecimal("0.10"), 4));
        String argumentos = "{\"cliente\":\"Ana\",\"direccion\":\"Calle 1\",\"items\":[{\"productoId\":" + producto.getId() + ",\"cantidad\":3}]";
        assertThatThrownBy(() -> tool("crearPedido").call(argumentos + "}")).isInstanceOf(RuntimeException.class);
        assertThat(pedidos.count()).isZero();
        JsonNode resultado = mapper.readTree(tool("crearPedido").call(argumentos + ",\"confirmado\":true}"));
        assertThat(resultado.get("total").decimalValue()).isEqualByComparingTo("0.30");
        assertThat(resultado.get("items").size()).isEqualTo(1);
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(1);
        pedidos.deleteAll(); productos.deleteAll();
    }

    @Test void fechaDelCallbackEsIsoNoArreglo() throws Exception {
        JsonNode resultado = mapper.readTree(tool("crearTarea").call("{\"titulo\":\"Fecha ISO callback\",\"fecha\":\"2026-11-20\"}"));
        assertThat(resultado.get("fecha").isTextual()).isTrue();
        assertThat(resultado.get("fecha").asText()).isEqualTo("2026-11-20");
        tool("eliminarTarea").call("{\"id\":" + resultado.get("id").asLong() + "}");
    }

    @Test void rechazaCoercionesJsonDeConfirmacionCantidadesEIds() {
        pedidos.deleteAll(); productos.deleteAll();
        Producto producto = productos.saveAndFlush(new Producto("Coerciones", new BigDecimal("1.00"), 10));
        String base = "{\"cliente\":\"Ana\",\"direccion\":\"Calle 1\",\"items\":[{\"productoId\":" + producto.getId();
        for (String cantidad : new String[]{"1.9", "\"2\"", "true", "2147483648", "null"}) {
            String input = base + ",\"cantidad\":" + cantidad + "}],\"confirmado\":true}";
            assertThatThrownBy(() -> tool("crearPedido").call(input)).isInstanceOf(RuntimeException.class);
        }
        for (String confirmacion : new String[]{"\"true\"", "1", "null", "false"}) {
            String input = base + ",\"cantidad\":1}],\"confirmado\":" + confirmacion + "}";
            assertThatThrownBy(() -> tool("crearPedido").call(input)).isInstanceOf(RuntimeException.class);
        }
        assertThatThrownBy(() -> tool("consultarPedido").call("{\"id\":1.5}")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> tool("consultarPedido").call("{\"id\":\"1\"}")).isInstanceOf(RuntimeException.class);
        assertThat(pedidos.count()).isZero();
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(10);
        productos.deleteAll();
    }

    private ToolCallback tool(String nombre) {
        return Arrays.stream(provider.getToolCallbacks()).filter(t -> t.getToolDefinition().name().equals(nombre)).findFirst().orElseThrow();
    }
}
