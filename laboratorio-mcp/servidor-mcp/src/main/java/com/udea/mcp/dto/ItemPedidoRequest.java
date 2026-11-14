package com.udea.mcp.dto;

import org.springframework.ai.tool.annotation.ToolParam;

/** Una línea de pedido. No se aceptan productos repetidos. */
public record ItemPedidoRequest(
        @ToolParam(description = "Id positivo del producto obtenido del catálogo") Long productoId,
        @ToolParam(description = "Número entero de unidades, mayor que cero") Integer cantidad) {
}
