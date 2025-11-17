package com.App.Lfarma.controller;

import com.App.Lfarma.DTO.ReporteDTO;
import com.App.Lfarma.DTO.ResumenReporteDTO;
import com.App.Lfarma.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // 🔹 Endpoint JSON (usado por el JavaScript del dashboard)
    @GetMapping("/api/reportes")
    @ResponseBody
    public List<ReporteDTO> obtenerReporte(
            @RequestParam("desde") @DateTimeFormat(pattern = "yyyy-MM-dd") Date desde,
            @RequestParam("hasta") @DateTimeFormat(pattern = "yyyy-MM-dd") Date hasta) {

        return reporteService.generarReporte(desde, hasta);
    }

    // Endpoint resumen (KPIs)
    @GetMapping("/reportes/summary")
    public ResumenReporteDTO obtenerResumen(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date hasta) {
        ResumenReporteDTO resumen = reporteService.generarResumen(desde, hasta);
        System.out.println("API Response - Total Ventas: " + resumen.getTotalVentas() +
                         ", Total Ganancia: " + resumen.getTotalGanancia() +
                         ", Productos Vendidos: " + resumen.getProductosVendidos() +
                         ", Facturas Emitidas: " + resumen.getFacturasEmitidas());
        return resumen;
    }
}
