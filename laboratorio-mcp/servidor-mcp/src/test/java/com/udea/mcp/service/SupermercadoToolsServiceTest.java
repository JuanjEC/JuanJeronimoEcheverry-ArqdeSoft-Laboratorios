package com.udea.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.mcp.dto.*;
import com.udea.mcp.model.*;
import com.udea.mcp.repository.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SupermercadoToolsServiceTest {
    @Autowired SupermercadoToolsService tools;
    @Autowired ProductoRepository productos;
    @Autowired PedidoRepository pedidos;
    @Autowired ObjectMapper json;
    Producto arroz;
    Producto leche;

    @BeforeEach void preparar() {
        pedidos.deleteAll();
        productos.deleteAll();
        arroz = productos.saveAndFlush(new Producto("Arroz 1 kg", new BigDecimal("4500.25"), 10));
        leche = productos.saveAndFlush(new Producto("Leche 1 L", new BigDecimal("3800.10"), 5));
    }

    @Test void creaConsultaYCancelaSinCiclosYConDineroExacto() throws Exception {
        assertThat(tools.listarProductos()).extracting(ProductoDto::id).containsExactly(arroz.getId(), leche.getId());
        assertThat(tools.buscarProductos("aRRoZ")).hasSize(1);
        PedidoDto pedido = crear(List.of(item(leche, 1), item(arroz, 2)));
        assertThat(pedido.total()).isEqualByComparingTo("12800.60");
        assertThat(pedido.items()).extracting(PedidoDto.ItemDto::productoId).containsExactly(arroz.getId(), leche.getId());
        assertThat(stock(arroz)).isEqualTo(8);
        assertThat(stock(leche)).isEqualTo(4);
        assertThat(tools.consultarPedido(pedido.id())).isEqualTo(pedido);
        assertThat(json.readTree(json.writeValueAsString(pedido)).get("items").size()).isEqualTo(2);
        assertThat(json.writeValueAsString(pedido)).doesNotContain("hibernateLazyInitializer", "\"pedido\":");
        PedidoDto cancelado = tools.cancelarPedido(pedido.id(), true);
        assertThat(cancelado.estado()).isEqualTo(Pedido.Estado.CANCELADO);
        assertThat(stock(arroz)).isEqualTo(10);
        assertThat(stock(leche)).isEqualTo(5);
        assertThat(tools.cancelarPedido(pedido.id(), true)).isEqualTo(cancelado);
        assertThat(stock(arroz)).isEqualTo(10);
        assertThat(stock(leche)).isEqualTo(5);
    }

    @Test void exigeConfirmacionEnServidorSinCambios() {
        assertThatIllegalArgumentException().isThrownBy(() -> tools.crearPedido("Ana", "Calle 1", List.of(item(arroz, 1)), false));
        assertThat(pedidos.count()).isZero();
        assertThat(stock(arroz)).isEqualTo(10);
        PedidoDto pedido = crear(List.of(item(arroz, 1)));
        assertThatIllegalArgumentException().isThrownBy(() -> tools.cancelarPedido(pedido.id(), false));
        assertThat(tools.consultarPedido(pedido.id()).estado()).isEqualTo(Pedido.Estado.CREADO);
        assertThat(stock(arroz)).isEqualTo(9);
    }

    @Test void validaClienteDireccionLineasCantidadesEIds() {
        for (String cliente : new String[]{null, " ", "x".repeat(151)})
            assertThatIllegalArgumentException().isThrownBy(() -> tools.crearPedido(cliente, "Calle 1", List.of(item(arroz, 1)), true));
        for (String direccion : new String[]{null, " ", "x".repeat(501)})
            assertThatIllegalArgumentException().isThrownBy(() -> tools.crearPedido("Ana", direccion, List.of(item(arroz, 1)), true));
        List<List<ItemPedidoRequest>> invalidas = new ArrayList<>();
        invalidas.add(null); invalidas.add(List.of()); invalidas.add(Arrays.asList((ItemPedidoRequest) null));
        invalidas.add(List.of(new ItemPedidoRequest(null, 1)));
        invalidas.add(List.of(new ItemPedidoRequest(0L, 1)));
        invalidas.add(List.of(new ItemPedidoRequest(-1L, 1)));
        invalidas.add(List.of(new ItemPedidoRequest(arroz.getId(), null)));
        invalidas.add(List.of(item(arroz, 0))); invalidas.add(List.of(item(arroz, -1)));
        invalidas.add(List.of(item(arroz, 1), item(arroz, 2)));
        invalidas.add(Collections.nCopies(101, item(arroz, 1)));
        for (List<ItemPedidoRequest> items : invalidas)
            assertThatIllegalArgumentException().isThrownBy(() -> crear(items));
        for (Long id : new Long[]{null, 0L, -1L, Long.MAX_VALUE}) {
            assertThatIllegalArgumentException().isThrownBy(() -> tools.consultarPedido(id));
            assertThatIllegalArgumentException().isThrownBy(() -> tools.cancelarPedido(id, true));
        }
        for (String fragmento : new String[]{null, " ", "x".repeat(151)})
            assertThatIllegalArgumentException().isThrownBy(() -> tools.buscarProductos(fragmento));
        assertThat(pedidos.count()).isZero();
        assertThat(stock(arroz)).isEqualTo(10);
    }

    @Test void rollbackSiSegundoProductoSinStock() {
        assertThatIllegalArgumentException().isThrownBy(() -> crear(List.of(item(arroz, 3), item(leche, 6))));
        assertThat(stock(arroz)).isEqualTo(10);
        assertThat(stock(leche)).isEqualTo(5);
        assertThat(pedidos.count()).isZero();
    }

    @Test void rollbackSiSegundoProductoNoExiste() {
        assertThatIllegalArgumentException().isThrownBy(() -> crear(List.of(item(arroz, 3), new ItemPedidoRequest(Long.MAX_VALUE, 1))));
        assertThat(stock(arroz)).isEqualTo(10);
        assertThat(pedidos.count()).isZero();
    }

    @Test void rollbackPorTotalFueraDeRango() {
        Producto caro = productos.saveAndFlush(new Producto("Precio máximo", new BigDecimal("99999999999999999.99"), 2));
        assertThatIllegalArgumentException().isThrownBy(() -> crear(List.of(item(arroz, 1), item(caro, 2))));
        assertThat(stock(arroz)).isEqualTo(10);
        assertThat(stock(caro)).isEqualTo(2);
        assertThat(pedidos.count()).isZero();
    }

    @Test void rollbackCancelacionSiReposicionDesborda() {
        PedidoDto pedido = crear(List.of(item(arroz, 1), item(leche, 1)));
        Producto cambiado = productos.findById(leche.getId()).orElseThrow();
        cambiado.reponer(Integer.MAX_VALUE - cambiado.getStock());
        productos.saveAndFlush(cambiado);
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> tools.cancelarPedido(pedido.id(), true));
        assertThat(stock(arroz)).isEqualTo(9);
        assertThat(stock(leche)).isEqualTo(Integer.MAX_VALUE);
        assertThat(tools.consultarPedido(pedido.id()).estado()).isEqualTo(Pedido.Estado.CREADO);
    }

    @Test void dosCompradoresNoSobrevenden() throws Exception {
        List<Boolean> resultados = competir(
                () -> intentarCrear(List.of(item(arroz, 7))),
                () -> intentarCrear(List.of(item(arroz, 7))));
        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(stock(arroz)).isEqualTo(3);
        assertThat(pedidos.count()).isEqualTo(1);
    }

    @Test void ordenInversoDeLineasNoGeneraDeadlock() throws Exception {
        List<Boolean> resultados = competir(
                () -> intentarCrear(List.of(item(arroz, 1), item(leche, 1))),
                () -> intentarCrear(List.of(item(leche, 1), item(arroz, 1))));
        assertThat(resultados).containsOnly(true).hasSize(2);
        assertThat(stock(arroz)).isEqualTo(8);
        assertThat(stock(leche)).isEqualTo(3);
    }

    @Test void cancelacionesConcurrentesReponenSoloUnaVez() throws Exception {
        PedidoDto pedido = crear(List.of(item(arroz, 2), item(leche, 1)));
        competir(() -> tools.cancelarPedido(pedido.id(), true), () -> tools.cancelarPedido(pedido.id(), true));
        assertThat(stock(arroz)).isEqualTo(10);
        assertThat(stock(leche)).isEqualTo(5);
        assertThat(tools.consultarPedido(pedido.id()).estado()).isEqualTo(Pedido.Estado.CANCELADO);
    }

    private PedidoDto crear(List<ItemPedidoRequest> items) { return tools.crearPedido(" Ana ", " Calle 1 ", items, true); }
    private ItemPedidoRequest item(Producto producto, int cantidad) { return new ItemPedidoRequest(producto.getId(), cantidad); }
    private int stock(Producto producto) { return productos.findById(producto.getId()).orElseThrow().getStock(); }
    private boolean intentarCrear(List<ItemPedidoRequest> items) {
        try { crear(items); return true; } catch (IllegalArgumentException ex) { return false; }
    }

    /** Barrera de inicio, transacciones reales en hilos diferentes, sin sleeps ni mocks. */
    private <T> List<T> competir(Supplier<T> primera, Supplier<T> segunda) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<T>> futuros = new ArrayList<>();
            for (Supplier<T> accion : List.of(primera, segunda)) futuros.add(executor.submit(() -> {
                listos.countDown();
                if (!inicio.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Barrera agotada");
                return accion.get();
            }));
            assertThat(listos.await(10, TimeUnit.SECONDS)).isTrue();
            inicio.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros) resultados.add(futuro.get(20, TimeUnit.SECONDS));
            return resultados;
        } finally { inicio.countDown(); executor.shutdownNow(); }
    }
}
