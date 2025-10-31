package com.App.Lfarma.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.entity.DetalleFactura;
import com.App.Lfarma.entity.Factura;
import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.repository.FacturaRepository;

@Service
public class FacturaService {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private FacturaRepository facturaRepository;

    public Factura crearFactura(Cliente cliente, List<DetalleFactura> detalles) {
        double total = 0;
    // No se aplica IVA: mantener iva a 0 y calcular total sin impuestos
    double totalGanancia = 0; // 🧮 acumulador de ganancia neta

        Factura factura = new Factura();
        factura.setFecha(new Date());
        factura.setCliente(cliente);
        factura.setDetalles(detalles);

        for (DetalleFactura detalle : detalles) {
            Producto producto = detalle.getProducto();

            if (producto.getCantidad() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Descontar stock
            productoService.descontarStock(producto.getCodigo(), detalle.getCantidad());

            // Precio de venta
            detalle.setPrecioUnitario(producto.getPrecio());

            // Subtotal de la línea
            double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
            total += subtotal;

        // 🆕 Calcular ganancia neta por producto
        // Usar el precio unitario registrado en el detalle (precio al momento de la venta)
        double precioUnitarioDetalle = detalle.getPrecioUnitario();
        double gananciaProducto = detalle.getCantidad() *
            (precioUnitarioDetalle - producto.getCostoCompra());
        // Debug: imprimir cálculo de ganancia por producto
        System.out.println("[FacturaService] Producto: " + producto.getNombre() +
            " | Cantidad: " + detalle.getCantidad() +
            " | PrecioUnitario(detalle): " + precioUnitarioDetalle +
            " | CostoCompra: " + producto.getCostoCompra() +
            " | GananciaProducto: " + gananciaProducto);
            totalGanancia += gananciaProducto;
        }

    // No aplicar IVA: guardar iva en 0 y total como la suma de subtotales
    factura.setIva(0);
    factura.setTotal(total);

        // 🆕 Guardar la ganancia neta total en la factura
        factura.setGananciaNeta(totalGanancia);

        return facturaRepository.save(factura);
    }

    public List<Factura> listarFacturas() {
        return facturaRepository.findAll();
    }

    public List<Factura> buscarFacturas(String searchTerm) {
        return facturaRepository.findAll().stream()
                .filter(factura ->
                        factura.getId().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                factura.getCliente().getNombre().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                factura.getCliente().getIdentificacion().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Optional<Factura> obtenerFacturaPorId(String id) {
        return facturaRepository.findById(id);
    }
}
