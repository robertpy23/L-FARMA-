package com.App.Lfarma.service;

import com.App.Lfarma.entity.Factura;
import com.App.Lfarma.repository.ClienteRepository;
import com.App.Lfarma.repository.FacturaRepository;
import com.App.Lfarma.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    public long countClientes() {
        try {
            return clienteRepository.count();
        } catch (Exception e) {
            log.error("Error contando clientes: {}", e.getMessage(), e);
            return 0;
        }
    }

    public long countProductos() {
        try {
            return productoRepository.count();
        } catch (Exception e) {
            log.error("Error contando productos: {}", e.getMessage(), e);
            return 0;
        }
    }

    public int countVentasHoy() {
        try {
            Date[] rango = hoyRango();
            List<Factura> facturas = facturaRepository.findByFechaBetween(rango[0], rango[1]);
            return facturas != null ? facturas.size() : 0;
        } catch (Exception e) {
            log.error("Error contando ventas hoy: {}", e.getMessage(), e);
            return 0;
        }
    }

    public double ingresosHoy() {
        try {
            Date[] rango = hoyRango();
            List<Factura> facturas = facturaRepository.findByFechaBetween(rango[0], rango[1]);
            double suma = 0.0;
            if (facturas != null) {
                for (Factura f : facturas) {
                    suma += f.getTotal();
                }
            }
            return Math.round(suma * 100.0) / 100.0;
        } catch (Exception e) {
            log.error("Error calculando ingresos hoy: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    public double gananciaNetaHoy() {
        try {
            Date[] rango = hoyRango();
            List<Factura> facturas = facturaRepository.findByFechaBetween(rango[0], rango[1]);
            double suma = 0.0;
            if (facturas != null) {
                for (Factura f : facturas) {
                    suma += f.getGananciaNeta();
                }
            }
            return Math.round(suma * 100.0) / 100.0;
        } catch (Exception e) {
            log.error("Error calculando ganancia neta hoy: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    // Nuevo: costo total de compra para las ventas del día (suma de cantidad * costoCompra)
    public double costoCompraHoy() {
        try {
            Date[] rango = hoyRango();
            List<Factura> facturas = facturaRepository.findByFechaBetween(rango[0], rango[1]);
            double sumaCosto = 0.0;
            if (facturas != null) {
                for (Factura f : facturas) {
                    if (f.getDetalles() != null) {
                        for (var detalle : f.getDetalles()) {
                            if (detalle.getProducto() != null) {
                                sumaCosto += detalle.getCantidad() * detalle.getProducto().getCostoCompra();
                            }
                        }
                    }
                }
            }
            return Math.round(sumaCosto * 100.0) / 100.0;
        } catch (Exception e) {
            log.error("Error calculando costo de compra hoy: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    public long alertasStock(int umbral) {
        try {
            // No hay método de conteo por cantidad en repo, así que contamos en memoria
            return productoRepository.findAll().stream().filter(p -> p.getCantidad() <= umbral).count();
        } catch (Exception e) {
            log.error("Error calculando alertas de stock: {}", e.getMessage(), e);
            return 0;
        }
    }

    private Date[] hoyRango() {
        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.DAY_OF_MONTH, 1);
        fin.add(Calendar.MILLISECOND, -1);

        return new Date[]{inicio.getTime(), fin.getTime()};
    }
}
