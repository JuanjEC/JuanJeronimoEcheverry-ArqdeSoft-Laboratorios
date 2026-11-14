package com.udea.mcp.dto;

import com.udea.mcp.model.Producto;
import java.math.BigDecimal;

public record ProductoDto(Long id, String nombre, BigDecimal precio, int stock) {
    public static ProductoDto de(Producto producto) {
        return new ProductoDto(producto.getId(), producto.getNombre(), producto.getPrecio(), producto.getStock());
    }
}
