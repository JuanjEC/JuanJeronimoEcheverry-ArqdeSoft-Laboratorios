package com.udea.mcp.repository;

import com.udea.mcp.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTest {
    @Autowired TareaRepository tareas;
    @Autowired ProductoRepository productos;
    @Autowired PedidoRepository pedidos;
    @Autowired TestEntityManager em;

    @Test void filtraEstadoYTituloIgnorandoMayusculasConOrden() {
        Tarea pendiente = tareas.save(new Tarea("Preparar SLIDES", LocalDate.of(2026, 11, 15)));
        Tarea completada = new Tarea("Revisar slides", LocalDate.of(2026, 11, 16)); completada.completar();
        tareas.saveAndFlush(completada); em.clear();
        assertThat(tareas.findByCompletadoOrderByIdAsc(false)).extracting(Tarea::getId).containsExactly(pendiente.getId());
        assertThat(tareas.findByCompletadoOrderByIdAsc(true)).extracting(Tarea::getId).containsExactly(completada.getId());
        assertThat(tareas.findByTituloContainingIgnoreCaseOrderByIdAsc("sLiDeS")).hasSize(2);
    }

    @Test void persisteDetalleYCargaBajoLock() {
        Producto producto = productos.saveAndFlush(new Producto("Arroz", new BigDecimal("12.30"), 5));
        Pedido pedido = new Pedido("Ana", "Calle 1"); pedido.agregar(producto, 2);
        pedidos.saveAndFlush(pedido); em.clear();
        assertThat(productos.bloquear(producto.getId())).isPresent();
        Pedido cargado = pedidos.bloquear(pedido.getId()).orElseThrow();
        assertThat(cargado.getTotal()).isEqualByComparingTo("24.60");
        assertThat(cargado.getItems()).hasSize(1);
        assertThat(cargado.getItems().getFirst().getProductoId()).isEqualTo(producto.getId());
        assertThat(productos.findByNombreContainingIgnoreCaseOrderByIdAsc("RRo")).hasSize(1);
    }

    @Test void rechazaProductoConNombreDuplicado() {
        productos.saveAndFlush(new Producto("Único", new BigDecimal("1.00"), 1));
        assertThatThrownBy(() -> productos.saveAndFlush(new Producto("Único", new BigDecimal("2.00"), 1)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
