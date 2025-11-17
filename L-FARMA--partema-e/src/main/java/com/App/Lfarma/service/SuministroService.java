package com.App.Lfarma.service;

import com.App.Lfarma.entity.*;
import com.App.Lfarma.repository.SuministroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SuministroService {

    @Autowired
    private SuministroRepository suministroRepository;

    @Autowired
    private ProductoService productoService;

    @Transactional
    public Suministro registrarSuministro(Suministro suministro) {
        // Validaciones
        if (suministro.getProveedor() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }

        if (suministro.getDetalles() == null || suministro.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El suministro debe tener al menos un producto");
        }

        // Configurar fechas y estado
        suministro.setFechaSuministro(new Date());
        suministro.setEstado("RECIBIDO");

        // Procesar cada detalle y ACTUALIZAR STOCK
        for (DetalleSuministro detalle : suministro.getDetalles()) {
            Producto producto = detalle.getProducto();

            // ✅ ACTUALIZAR STOCK AUTOMÁTICAMENTE
            producto.setCantidad(producto.getCantidad() + detalle.getCantidad());

            // ✅ ACTUALIZAR COSTO DE COMPRA si es mayor a 0
            if (detalle.getPrecioCompra() > 0) {
                producto.setCostoCompra(detalle.getPrecioCompra());
            }

            // ✅ ACTUALIZAR PRECIO si se sugiere uno nuevo y es mayor a 0
            if (detalle.getPrecioVentaSugerido() > 0) {
                producto.setPrecio(detalle.getPrecioVentaSugerido());
            }

            // ✅ ACTUALIZAR PROVEEDOR del producto
            producto.setProveedor(suministro.getProveedor());

            // ✅ ACTUALIZAR proveedorId también para compatibilidad
            producto.setProveedorId(suministro.getProveedor().getId());

            // Guardar producto actualizado
            productoService.guardarProducto(producto);
        }

        return suministroRepository.save(suministro);
    }

    public List<Suministro> listarSuministros() {
        return suministroRepository.findAll();
    }

    public Page<Suministro> listarSuministrosPaginados(Pageable pageable) {
        return suministroRepository.findAll(pageable);
    }

    public Page<Suministro> buscarSuministrosPorProveedor(String codigoProveedor, Pageable pageable) {
        return suministroRepository.findByProveedorCodigo(codigoProveedor, pageable);
    }

    public Optional<Suministro> obtenerSuministroPorId(String id) {
        return suministroRepository.findById(id);
    }

    public List<Suministro> obtenerSuministrosPorRangoFechas(Date desde, Date hasta) {
        return suministroRepository.findByFechaSuministroBetween(desde, hasta);
    }

    public double calcularTotalSuministrosPorProveedor(String codigoProveedor) {
        List<Suministro> suministros = suministroRepository.findByProveedorCodigo(codigoProveedor, Pageable.unpaged())
                .getContent();
        return suministros.stream()
                .mapToDouble(Suministro::getTotalSuministro)
                .sum();
    }
}