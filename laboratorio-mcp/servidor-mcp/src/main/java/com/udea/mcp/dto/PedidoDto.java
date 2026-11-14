package com.udea.mcp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.udea.mcp.model.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Snapshot sin entidades JPA, proxies ni referencias inversas. Importes en COP. */
public record PedidoDto(Long id, String cliente, String direccion,
                        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant creadoEn,
                        Pedido.Estado estado, BigDecimal total, List<ItemDto> items) {
    public PedidoDto { items = List.copyOf(items); }

    public record ItemDto(Long productoId, String nombre, int cantidad,
                          BigDecimal precioUnitario, BigDecimal subtotal) {}

    public static PedidoDto de(Pedido pedido) {
        return new PedidoDto(pedido.getId(), pedido.getCliente(), pedido.getDireccion(), pedido.getCreadoEn(),
                pedido.getEstado(), pedido.getTotal(), pedido.getItems().stream()
                .map(item -> new ItemDto(item.getProductoId(), item.getNombre(), item.getCantidad(),
                        item.getPrecioUnitario(), item.getSubtotal())).toList());
    }
}
