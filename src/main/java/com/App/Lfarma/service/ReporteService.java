package com.App.Lfarma.service;

import com.App.Lfarma.DTO.ReporteDTO;
import com.App.Lfarma.entity.Factura;
import com.App.Lfarma.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private FacturaRepository facturaRepository;

    public List<ReporteDTO> generarReporte(Date desde, Date hasta) {
        List<Factura> facturas = facturaRepository.findByFechaBetween(desde, hasta);
        List<ReporteDTO> reportes = new ArrayList<>();

        for (Factura f : facturas) {
            if (f.getDetalles() != null) {
                f.getDetalles().forEach(detalle -> {
            // Usar el precio unitario guardado en el detalle (precio al momento de la venta)
            double totalVenta = detalle.getCantidad() * detalle.getPrecioUnitario();
            double ganancia = detalle.getCantidad() *
                (detalle.getPrecioUnitario() - detalle.getProducto().getCostoCompra());

                    reportes.add(new ReporteDTO(
                            f.getFecha(),
                            detalle.getProducto().getNombre(),
                            detalle.getCantidad(),
                            totalVenta,
                            ganancia
                    ));
                });
            }
        }

        return reportes;
    }

    public com.App.Lfarma.DTO.ResumenReporteDTO generarResumen(Date desde, Date hasta) {
        // Si no se proporcionan fechas, calcular para el mes actual
        if (desde == null || hasta == null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            desde = cal.getTime();
            
            cal.add(java.util.Calendar.MONTH, 1);
            cal.add(java.util.Calendar.MILLISECOND, -1);
            hasta = cal.getTime();
        }

        List<Factura> facturas = facturaRepository.findByFechaBetween(desde, hasta);
        System.out.println("[ReporteService] Generando resumen desde " + desde + " hasta " + hasta);
        System.out.println("[ReporteService] Facturas encontradas: " + facturas.size());

        double totalVentas = 0;
        double totalGanancia = 0;
        long productosVendidos = 0;
        long facturasEmitidas = facturas.size();

        for (Factura f : facturas) {
            System.out.println("[ReporteService] Procesando factura ID: " + f.getId() + " - Fecha: " + f.getFecha());
            if (f.getDetalles() != null) {
                for (var detalle : f.getDetalles()) {
                    double venta = detalle.getCantidad() * detalle.getPrecioUnitario();
                    double ganancia = detalle.getCantidad() * (detalle.getPrecioUnitario() - detalle.getProducto().getCostoCompra());
                    
                    System.out.println("[ReporteService] Detalle - Producto: " + detalle.getProducto().getNombre() + 
                                     " | Cantidad: " + detalle.getCantidad() + 
                                     " | Precio Unitario: " + detalle.getPrecioUnitario() + 
                                     " | Costo Compra: " + detalle.getProducto().getCostoCompra() +
                                     " | Venta: " + venta +
                                     " | Ganancia: " + ganancia);
                    
                    totalVentas += venta;
                    totalGanancia += ganancia;
                    productosVendidos += detalle.getCantidad();
                }
            }
        }

        System.out.println("[ReporteService] Resumen Final - Total Ventas: " + totalVentas + 
                          " | Total Ganancia: " + totalGanancia +
                          " | Productos Vendidos: " + productosVendidos +
                          " | Facturas Emitidas: " + facturasEmitidas);

        return new com.App.Lfarma.DTO.ResumenReporteDTO(totalVentas, totalGanancia, productosVendidos, facturasEmitidas);
    }
}
