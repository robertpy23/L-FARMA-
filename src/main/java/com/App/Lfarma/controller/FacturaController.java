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

    @Autowired
    private UsuarioRepository usuarioRepository;

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
            
                int cantidad = cantidades.get(i);
                if (cantidad > producto.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Stock disponible: " + producto.getCantidad() + ", solicitado: " + cantidad);
                }
            
                // Verificar que el producto tenga precio de venta y costo de compra válidos
                if (producto.getPrecio() <= 0) {
                    throw new RuntimeException("El producto " + producto.getNombre() + " no tiene un precio de venta válido");
                }
                if (producto.getCostoCompra() <= 0) {
                    throw new RuntimeException("El producto " + producto.getNombre() + " no tiene un costo de compra válido");
                }
            
                DetalleFactura detalle = new DetalleFactura();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
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
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> guardarFacturaDesdeCarrito(@RequestBody Map<String, Object> datos) {
        try {
            System.out.println("📦 Datos recibidos para factura: " + datos);

            // 1️⃣ Obtener cliente del frontend
            String clienteCodigo = (String) datos.get("clienteId");
            System.out.println("🔍 Buscando cliente: " + clienteCodigo);

            if (clienteCodigo == null || clienteCodigo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "clienteId es requerido"
                ));
            }

            // 2️⃣ Obtener datos del formulario
            Map<String, Object> datosEnvio = (Map<String, Object>) datos.get("datosEnvio");
            System.out.println("📋 Datos del formulario recibidos: " + datosEnvio);

            // 3️⃣ Buscar si existe en Mongo o crear desde MySQL
            Cliente cliente = clienteService.obtenerClientePorCodigo(clienteCodigo)
                    .orElseGet(() -> {
                        System.out.println("🔄 Cliente no encontrado en Mongo, buscando en MySQL...");

                        // Si no existe, sincronizarlo desde MySQL
                        Usuario usuario = usuarioRepository.findByUsername(clienteCodigo)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en MySQL con username: " + clienteCodigo));

                        System.out.println("✅ Usuario encontrado en MySQL: " + usuario.getUsername());

                        Cliente nuevo = new Cliente();
                        nuevo.setCodigo(usuario.getUsername());
                        nuevo.setNombre(usuario.getUsername() != null ? usuario.getUsername() : "Cliente " + usuario.getUsername());
                        nuevo.setUsername(usuario.getUsername());

                        // ✅ CORREGIDO: Manejar campos que pueden ser null
                        if (usuario.getEmail() != null) {
                            nuevo.setEmail(usuario.getEmail());
                        } else {
                            nuevo.setEmail(usuario.getUsername() + "@ejemplo.com");
                        }

                        if (usuario.getTelefono() != null) {
                            nuevo.setTelefono(usuario.getTelefono());
                        } else {
                            nuevo.setTelefono("000-0000000");
                        }

                        return clienteService.agregarCliente(nuevo);
                    });

            System.out.println("✅ Cliente encontrado/creado: " + cliente.getCodigo());

            // ✅✅✅ CORRECCIÓN CRÍTICA: ACTUALIZAR CLIENTE CON DATOS DEL FORMULARIO
            if (datosEnvio != null) {
                System.out.println("🔄 Actualizando cliente con datos del formulario...");

                // Combinar nombre y apellido
                String nombreCompleto = datosEnvio.get("nombre") + " " + datosEnvio.get("apellido");
                cliente.setNombre(nombreCompleto);

                // Actualizar otros campos
                cliente.setEmail((String) datosEnvio.get("email"));
                cliente.setTelefono((String) datosEnvio.get("telefono"));
                cliente.setIdentificacion((String) datosEnvio.get("identificacion"));
                cliente.setDireccion((String) datosEnvio.get("direccion"));

                // Guardar los cambios del cliente
                clienteService.guardarCliente(cliente);
                System.out.println("✅ Cliente actualizado con datos del formulario: " + cliente.getNombre());
            } else {
                System.out.println("⚠️ No se recibieron datos del formulario para actualizar el cliente");
            }

            // 4️⃣ Convertir productos del carrito
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datos.get("productos");
            if (productos == null || productos.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "La lista de productos está vacía"
                ));
            }

            List<DetalleFactura> detalles = new ArrayList<>();

            // ✅ Validar stock antes de procesar
            for (Map<String, Object> p : productos) {
                String id = (String) p.get("id");

                // ✅ CORREGIDO: Manejar diferentes tipos de cantidad (Integer vs Long)
                int cantidad;
                Object cantidadObj = p.get("cantidad");
                if (cantidadObj instanceof Integer integer) {
                    cantidad = integer;
                } else if (cantidadObj instanceof Long long1) {
                    cantidad = long1.intValue();
                } else if (cantidadObj instanceof Double double1) {
                    cantidad = double1.intValue();
                } else {
                    throw new RuntimeException("Tipo de cantidad no válido: " + cantidadObj.getClass().getSimpleName());
                }

                System.out.println("🔍 Validando producto ID: " + id + ", cantidad: " + cantidad);

                Producto producto = productoService.buscarPorId(id)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

                // Verificar stock
                if (producto.getCantidad() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Stock disponible: " + producto.getCantidad() + ", solicitado: " + cantidad);
                }
                
                    // Validar precio de venta y costo de compra
                    if (producto.getPrecio() <= 0) {
                        throw new RuntimeException("El producto " + producto.getNombre() + " no tiene un precio de venta válido");
                    }
                    if (producto.getCostoCompra() <= 0) {
                        throw new RuntimeException("El producto " + producto.getNombre() + " no tiene un costo de compra válido");
                    }
            }

            // ✅ Crear detalles de factura después de validar todo el stock
            for (Map<String, Object> p : productos) {
                String id = (String) p.get("id");

                // ✅ CORREGIDO: Manejar diferentes tipos de cantidad
                int cantidad;
                Object cantidadObj = p.get("cantidad");
                if (cantidadObj instanceof Integer integer) {
                    cantidad = integer;
                } else if (cantidadObj instanceof Long long1) {
                    cantidad = long1.intValue();
                } else if (cantidadObj instanceof Double double1) {
                    cantidad = double1.intValue();
                } else {
                    cantidad = 1; // Valor por defecto
                }

                Producto producto = productoService.buscarPorId(id).get(); // Ya validado arriba

                DetalleFactura detalle = new DetalleFactura();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalles.add(detalle);

                System.out.println("✅ Producto agregado a factura: " + producto.getNombre() + " x " + cantidad);
            }

            // 5️⃣ Crear factura con cliente actualizado
            System.out.println("🔄 Creando factura...");
            Factura factura = facturaService.crearFactura(cliente, detalles);

            // ✅ Asegurar que el total se calcule correctamente
            factura.calcularTotal();

            System.out.println("✅ Factura creada exitosamente: " + factura.getId());
            System.out.println("💰 Total factura: $" + factura.getTotal());
            System.out.println("📋 Número de productos: " + factura.getDetalles().size());
            System.out.println("👤 Cliente en factura: " + factura.getCliente().getNombre() + " - " + factura.getCliente().getEmail());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "facturaId", factura.getId(),
                    "total", factura.getTotal(),
                    "cliente", cliente.getNombre(),
                    "productosCount", factura.getDetalles().size(),
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
        try {
            Factura factura = facturaService.obtenerFacturaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            model.addAttribute("factura", factura);
            model.addAttribute("productos", factura.getDetalles().stream()
                    .map(DetalleFactura::getProducto)
                    .toList());

            // ✅ Calcular subtotal para mostrar en la vista
            double subtotal = factura.getDetalles().stream()
                    .mapToDouble(detalle -> detalle.getPrecioUnitario() * detalle.getCantidad())
                    .sum();
            model.addAttribute("subtotal", subtotal);

            // No aplicar IVA: mostrar 0 en la vista
            double iva = 0;
            model.addAttribute("iva", iva);

            return "compraFinalizada";
        } catch (Exception e) {
            System.err.println("❌ Error al cargar factura finalizada: " + e.getMessage());
            model.addAttribute("error", "No se pudo cargar la factura solicitada");
            return "error";
        }
    }

    // ✅ NUEVO ENDPOINT: Para obtener factura en formato JSON (útil para APIs)
    @GetMapping("/api/{id}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerFacturaApi(@PathVariable String id) {
        try {
            Factura factura = facturaService.obtenerFacturaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "factura", factura
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }

    // ✅ NUEVO ENDPOINT: Para listar facturas en JSON
    @GetMapping("/api")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> listarFacturasApi() {
        try {
            List<Factura> facturas = facturaService.listarFacturas();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "facturas", facturas,
                    "count", facturas.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }

    // ✅ NUEVO ENDPOINT: Para el checkout
    @GetMapping("/checkout")
    public String mostrarCheckout() {
        return "checkout";
    }

    // ✅ NUEVO ENDPOINT: Para verificar estado de factura
    @GetMapping("/api/estado/{id}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> verificarEstadoFactura(@PathVariable String id) {
        try {
            Factura factura = facturaService.obtenerFacturaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "facturaId", factura.getId(),
                    "estado", "COMPLETADA",
                    "total", factura.getTotal(),
                    "fecha", factura.getFecha(),
                    "cliente", factura.getCliente().getNombre()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }
}