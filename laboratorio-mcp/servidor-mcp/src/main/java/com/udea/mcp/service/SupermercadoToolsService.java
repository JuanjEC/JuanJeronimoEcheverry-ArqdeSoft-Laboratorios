package com.udea.mcp.service;

import com.udea.mcp.dto.*;
import com.udea.mcp.model.*;
import com.udea.mcp.repository.*;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SupermercadoToolsService {
    private final ProductoRepository productos;
    private final PedidoRepository pedidos;

    public SupermercadoToolsService(ProductoRepository productos, PedidoRepository pedidos) {
        this.productos = productos; this.pedidos = pedidos;
    }

    @Tool(description = "Lista el catálogo con id, nombre, precio unitario en COP y stock actual, ordenado por id.")
    public List<ProductoDto> listarProductos() {
        return productos.findAll(Sort.by("id")).stream().map(ProductoDto::de).toList();
    }

    @Tool(description = "Busca productos por parte del nombre sin distinguir mayúsculas; devuelve id, precio en COP y stock.")
    public List<ProductoDto> buscarProductos(
            @ToolParam(description = "Texto no vacío de hasta 150 caracteres del nombre") String fragmento) {
        return productos.findByNombreContainingIgnoreCaseOrderByIdAsc(Validaciones.texto(fragmento, "Fragmento", 150))
                .stream().map(ProductoDto::de).toList();
    }

    @Tool(description = "Crea un pedido a domicilio y descuenta stock de forma atómica. Presenta productos, cantidades, total en COP y dirección al usuario y solicita su confirmación antes de llamar. Devuelve id, estado, detalle y total definitivo; no es idempotente: no repetir ante una respuesta incierta.")
    @Transactional
    public PedidoDto crearPedido(
            @ToolParam(description = "Nombre del cliente, no vacío, máximo 150 caracteres") String cliente,
            @ToolParam(description = "Dirección de entrega, no vacía, máximo 500 caracteres") String direccion,
            @ToolParam(description = "Entre 1 y 100 líneas con productoId positivo y cantidad entera positiva; sin productoId repetidos") List<ItemPedidoRequest> items,
            @ToolParam(description = "true solamente después de la confirmación explícita del usuario de este pedido") boolean confirmado) {
        Validaciones.confirmar(confirmado);
        String nombre = Validaciones.texto(cliente, "Cliente", 150);
        String destino = Validaciones.texto(direccion, "Dirección", 500);
        SortedMap<Long, Integer> lineas = validarItems(items);
        Pedido pedido = new Pedido(nombre, destino);
        // Todos los caminos que cambian stock toman locks de productos por id ascendente.
        // Se mantienen hasta commit; el segundo comprador observa el stock ya confirmado.
        for (var linea : lineas.entrySet()) {
            Producto producto = bloquearProducto(linea.getKey());
            producto.descontar(linea.getValue());
            pedido.agregar(producto, linea.getValue());
        }
        return PedidoDto.de(pedidos.saveAndFlush(pedido));
    }

    @Tool(description = "Consulta un pedido por id; devuelve estado, dirección, cantidades y precios históricos en COP sin modificar stock.")
    public PedidoDto consultarPedido(@ToolParam(description = "Id positivo del pedido") Long id) {
        return PedidoDto.de(pedidos.findById(Validaciones.id(id))
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido " + id)));
    }

    @Tool(description = "Cancela un pedido y repone su stock atómicamente. Solicita confirmación explícita al usuario antes de llamar. Es idempotente: un pedido ya cancelado no repone stock otra vez.")
    @Transactional
    public PedidoDto cancelarPedido(
            @ToolParam(description = "Id positivo del pedido a cancelar") Long id,
            @ToolParam(description = "true solamente después de la confirmación explícita del usuario para cancelar este id") boolean confirmado) {
        Validaciones.confirmar(confirmado);
        // Serializa cancelaciones del mismo pedido antes de comprobar su estado.
        Pedido pedido = pedidos.bloquear(Validaciones.id(id))
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido " + id));
        if (pedido.getEstado() == Pedido.Estado.CANCELADO) return PedidoDto.de(pedido);
        pedido.getItems().stream().sorted(java.util.Comparator.comparing(PedidoItem::getProductoId))
                .forEach(item -> bloquearProducto(item.getProductoId()).reponer(item.getCantidad()));
        pedido.cancelar();
        pedidos.flush();
        return PedidoDto.de(pedido);
    }

    private Producto bloquearProducto(Long id) {
        return productos.bloquear(id).orElseThrow(() -> new IllegalArgumentException("No existe el producto " + id));
    }

    private SortedMap<Long, Integer> validarItems(List<ItemPedidoRequest> items) {
        if (items == null || items.isEmpty() || items.size() > 100)
            throw new IllegalArgumentException("El pedido debe contener entre 1 y 100 líneas");
        SortedMap<Long, Integer> lineas = new TreeMap<>();
        for (ItemPedidoRequest item : items) {
            if (item == null) throw new IllegalArgumentException("Una línea del pedido es nula");
            Long id = Validaciones.id(item.productoId());
            if (item.cantidad() == null || item.cantidad() <= 0)
                throw new IllegalArgumentException("La cantidad debe ser un entero positivo");
            if (lineas.putIfAbsent(id, item.cantidad()) != null)
                throw new IllegalArgumentException("Producto repetido: " + id);
        }
        return lineas;
    }
}
