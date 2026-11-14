package com.udea.mcp.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ProductoTest {
    @Test void validaNombrePrecioStockYEscala() {
        for (String nombre : new String[]{null, " ", "x".repeat(151)})
            assertThatIllegalArgumentException().isThrownBy(() -> new Producto(nombre, BigDecimal.ONE, 1));
        for (BigDecimal precio : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1"), new BigDecimal("1.001"), new BigDecimal("100000000000000000.00")})
            assertThatIllegalArgumentException().isThrownBy(() -> new Producto("Arroz", precio, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> new Producto("Arroz", BigDecimal.ONE, -1));
        Producto producto = new Producto("Arroz", new BigDecimal("0.10"), 3);
        assertThatIllegalArgumentException().isThrownBy(() -> producto.descontar(4));
        assertThatIllegalArgumentException().isThrownBy(() -> producto.descontar(0));
        assertThatIllegalArgumentException().isThrownBy(() -> producto.reponer(0));
        assertThat(producto.getStock()).isEqualTo(3);
        assertThat(producto.getPrecio()).isEqualTo(new BigDecimal("0.10"));
    }
}
