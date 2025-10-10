package com.App.Lfarma.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.App.Lfarma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.App.Lfarma.entity.Usuario;

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

    @Autowired private UsuarioRepository usuarioRepository;


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
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarFacturaDesdeCarrito(@RequestBody Map<String, Object> datos) {
        try {
            System.out.println("📦 Datos recibidos: " + datos);

            // 1️⃣ Obtener cliente del frontend
            String clienteCodigo = (String) datos.get("clienteId");
            System.out.println("🔍 Buscando cliente: " + clienteCodigo);

            if (clienteCodigo == null || clienteCodigo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("clienteId es requerido");
            }

            // 2️⃣ Buscar si existe en Mongo
            Cliente cliente = clienteService.obtenerClientePorCodigo(clienteCodigo)
                    .orElseGet(() -> {
                        System.out.println("🔄 Cliente no encontrado en Mongo, buscando en MySQL...");

                        // Si no existe, sincronizarlo desde MySQL
                        Usuario usuario = usuarioRepository.findByUsername(clienteCodigo)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en MySQL con username: " + clienteCodigo));

                        System.out.println("✅ Usuario encontrado en MySQL: " + usuario.getUsername());

                        Cliente nuevo = new Cliente();
                        nuevo.setCodigo(usuario.getUsername());
                        nuevo.setNombre(usuario.getUsername());
                        nuevo.setUsername(usuario.getUsername());
                        // Agregar más campos si están disponibles
                        return clienteService.agregarCliente(nuevo);
                    });

            System.out.println("✅ Cliente encontrado/creado: " + cliente.getCodigo());

            // 3️⃣ Convertir productos del carrito
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datos.get("productos");
            if (productos == null || productos.isEmpty()) {
                return ResponseEntity.badRequest().body("La lista de productos está vacía");
            }

            List<DetalleFactura> detalles = new ArrayList<>();

            for (Map<String, Object> p : productos) {
                String id = (String) p.get("id");
                int cantidad = (int) p.get("cantidad");

                System.out.println("🔍 Buscando producto ID: " + id);

                Producto producto = productoService.buscarPorId(id)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

                // Verificar stock
                if (producto.getCantidad() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Stock disponible: " + producto.getCantidad());
                }

                DetalleFactura detalle = new DetalleFactura();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalles.add(detalle);

                System.out.println("✅ Producto agregado: " + producto.getNombre());
            }

            // 4️⃣ Crear factura con cliente sincronizado
            System.out.println("🔄 Creando factura...");
            Factura factura = facturaService.crearFactura(cliente, detalles);
            factura.calcularTotal();

            System.out.println("✅ Factura creada exitosamente: " + factura.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "facturaId", factura.getId(),
                    "message", "Compra realizada exitosamente"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error al crear factura: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error al crear la factura: " + e.getMessage()
                    ));
        }
    }


    @GetMapping("/finalizada/{id}")
    public String mostrarFacturaFinalizada(@PathVariable String id, Model model) {
        Factura factura = facturaService.obtenerFacturaPorId(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        model.addAttribute("factura", factura);
        model.addAttribute("productos", factura.getDetalles().stream()
                .map(DetalleFactura::getProducto)
                .toList());
        return "compraFinalizada"; // tu vista .html
    }

}