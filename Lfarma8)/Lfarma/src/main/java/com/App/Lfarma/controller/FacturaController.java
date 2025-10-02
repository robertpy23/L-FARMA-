package com.App.Lfarma.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.entity.DetalleFactura;
import com.App.Lfarma.entity.Factura;
import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.service.ClienteService;
import com.App.Lfarma.service.FacturaService;
import com.App.Lfarma.service.ProductoService;

@Controller
@RequestMapping("/facturas")
public class FacturaController {
    @Autowired
    private FacturaService facturaService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ProductoService productoService;
    
    @GetMapping("/crear")
    public String mostrarFormularioFactura(Model model) {
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("factura", new Factura());
        return "crearFactura";
    }
    
    @PostMapping("/guardar")
    public String guardarFactura(
            @RequestParam String codigoCliente, 
            @RequestParam List<String> idsProductos, 
            @RequestParam List<Integer> cantidades) {
        
        Cliente cliente = clienteService.obtenerClientePorCodigo(codigoCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        List<DetalleFactura> detalles = new ArrayList<>();
        for (int i = 0; i < idsProductos.size(); i++) {
            Producto producto = productoService.buscarPorId(idsProductos.get(i))
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            DetalleFactura detalle = new DetalleFactura();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidades.get(i));
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);
        }
        
        facturaService.crearFactura(cliente, detalles);
        
        return "redirect:/facturas";
    }
    
    @GetMapping("")
    public String listarFacturas(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("facturas", facturaService.buscarFacturas(search));
        } else {
            model.addAttribute("facturas", facturaService.listarFacturas());
        }
        return "listarFacturas";
    }
    
    @GetMapping("/{id}")
    public String verDetalleFactura(@PathVariable String id, Model model) {
        Factura factura = facturaService.obtenerFacturaPorId(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        model.addAttribute("factura", factura);
        return "detalleFactura";
    }
}