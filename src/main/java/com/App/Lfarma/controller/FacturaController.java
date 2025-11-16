package com.App.Lfarma.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.App.Lfarma.entity.Usuario;
import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.entity.DetalleFactura;
import com.App.Lfarma.entity.Factura;
import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.repository.UsuarioRepository;
import com.App.Lfarma.service.ClienteService;
import com.App.Lfarma.service.FacturaService;
import com.App.Lfarma.service.ProductoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@Controller
@RequestMapping("/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ✅ MÉTODO PARA VERIFICAR SI EL USUARIO ACTUAL ES ADMIN
    private boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    // ✅ PASAR EL ATRIBUTO A TODAS LAS VISTAS
    @ModelAttribute("esAdmin")
    public boolean esAdminAttribute() {
        return esAdmin();
    }

    // ✅ OBTENER USUARIO ACTUAL
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    @GetMapping("/crear")
    public String mostrarFormularioFactura(Model model) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} accediendo al formulario de creación de factura", user);

        try {
            List<Cliente> clientes = clienteService.listarClientes();
            List<Producto> productos = productoService.listarProductos();

            model.addAttribute("clientes", clientes);
            model.addAttribute("productos", productos);
            model.addAttribute("factura", new Factura());
            model.addAttribute("esAdmin", esAdmin());

            log.info("✅ Usuario {} cargó formulario de factura exitosamente - {} clientes, {} productos",
                    user, clientes.size(), productos.size());
            return "crearFactura";
        } catch (Exception e) {
            log.error("❌ Error al cargar formulario de factura para usuario {}: {}", user, e.getMessage(), e);
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "error";
        }
    }

    // ✅ CORREGIDO: Usar RedirectAttributes para mejor manejo de redirecciones
    @PostMapping("/guardar")
    public String guardarFactura(
            @RequestParam String codigoCliente,
            @RequestParam List<String> idsProductos,
            @RequestParam List<Integer> cantidades,
            RedirectAttributes redirectAttributes) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} guardando nueva factura para cliente: {}", user, codigoCliente);

        try {
            // ✅ VALIDACIÓN: Parámetros de entrada
            if (codigoCliente == null || codigoCliente.trim().isEmpty()) {
                log.warn("⚠️ Usuario {} intentó crear factura sin código de cliente", user);
                redirectAttributes.addFlashAttribute("error", "El código del cliente es requerido");
                return "redirect:/facturas/crear";
            }

            if (idsProductos == null || idsProductos.isEmpty() || cantidades == null || cantidades.isEmpty()) {
                log.warn("⚠️ Usuario {} intentó crear factura sin productos", user);
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un producto");
                return "redirect:/facturas/crear";
            }

            if (idsProductos.size() != cantidades.size()) {
                log.warn("⚠️ Usuario {} proporcionó listas de productos y cantidades inconsistentes", user);
                redirectAttributes.addFlashAttribute("error", "La cantidad de productos y cantidades no coincide");
                return "redirect:/facturas/crear";
            }

            Cliente cliente = clienteService.obtenerClientePorCodigo(codigoCliente.trim())
                    .orElseThrow(() -> {
                        log.error("❌ Cliente no encontrado: {}", codigoCliente);
                        return new RuntimeException("Cliente no encontrado: " + codigoCliente);
                    });

            List<DetalleFactura> detalles = new ArrayList<>();

            // ✅ VALIDACIÓN: Productos antes de procesar
            for (int i = 0; i < idsProductos.size(); i++) {
                String productoId = idsProductos.get(i);
                Integer cantidad = cantidades.get(i);

                if (productoId == null || productoId.trim().isEmpty()) {
                    throw new RuntimeException("ID de producto inválido en la posición " + i);
                }

                if (cantidad == null || cantidad <= 0) {
                    throw new RuntimeException("Cantidad inválida para el producto en la posición " + i);
                }

                Producto producto = productoService.buscarPorId(productoId.trim())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

                if (cantidad > producto.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Stock disponible: " + producto.getCantidad() + ", solicitado: " + cantidad);
                }

                if (producto.getPrecio() <= 0) {
                    throw new RuntimeException("El producto " + producto.getNombre() + " no tiene un precio de venta válido");
                }

                // ✅ CORRECCIÓN: Manejar productos sin costo de compra
                if (producto.getCostoCompra() <= 0) {
                    double costoCalculado = producto.getPrecio() * 0.6;
                    log.warn("⚠️ Producto sin costo de compra: {}, usando costo calculado: {}",
                            producto.getNombre(), costoCalculado);
                    producto.setCostoCompra(costoCalculado);
                    productoService.guardarProducto(producto);
                }

                DetalleFactura detalle = new DetalleFactura();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalles.add(detalle);

                log.debug("📦 Producto agregado a factura: {} x {}", producto.getNombre(), cantidad);
            }

            Factura factura = facturaService.crearFactura(cliente, detalles);

            // ✅ CORREGIDO: Pasar ID de factura reciente para destacar en la lista
            redirectAttributes.addFlashAttribute("success", "Factura creada exitosamente");
            redirectAttributes.addFlashAttribute("facturaReciente", factura.getId());

            log.info("✅ Usuario {} creó factura {} exitosamente con {} productos",
                    user, factura.getId(), detalles.size());

            return "redirect:/facturas";

        } catch (Exception e) {
            log.error("❌ Error al guardar factura para usuario {}: {}", user, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar factura: " + e.getMessage());
            return "redirect:/facturas/crear";
        }
    }

    // ✅ CORREGIDO: Implementar paginación completa con mejor manejo de errores
    @GetMapping("")
    public String listarFacturas(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} listando facturas - Página: {}, Tamaño: {}, Búsqueda: '{}'", user, page, size, search);

        try {
            // ✅ CORREGIDO: Validar parámetros de paginación
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100;

            Pageable pageable = PageRequest.of(page, size);
            Page<Factura> facturasPage;

            if (search != null && !search.trim().isEmpty()) {
                String terminoBusqueda = search.trim();
                facturasPage = facturaService.buscarFacturasPaginadas(terminoBusqueda, pageable);
                model.addAttribute("search", terminoBusqueda);
                log.debug("🔍 Búsqueda de facturas: '{}' - Encontradas: {}", terminoBusqueda, facturasPage.getTotalElements());
            } else {
                facturasPage = facturaService.listarFacturasPaginadas(pageable);
                log.debug("📋 Listado normal de facturas - Total: {}", facturasPage.getTotalElements());
            }

            model.addAttribute("facturas", facturasPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", facturasPage.getTotalPages());
            model.addAttribute("totalItems", facturasPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("esAdmin", esAdmin());

            log.info("✅ Usuario {} cargó {} facturas exitosamente (página {} de {})",
                    user, facturasPage.getNumberOfElements(), page + 1, facturasPage.getTotalPages());

        } catch (Exception e) {
            log.error("❌ Error en listarFacturas para usuario {}: {}", user, e.getMessage(), e);
            model.addAttribute("error", "Error al cargar facturas: " + e.getMessage());
            model.addAttribute("facturas", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("esAdmin", esAdmin());
        }

        return "listarFacturas";
    }

    @GetMapping("/{id}")
    public String verDetalleFactura(@PathVariable String id, Model model) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} viendo detalle de factura: {}", user, id);

        try {
            if (id == null || id.trim().isEmpty()) {
                log.warn("⚠️ Usuario {} proporcionó ID de factura inválido", user);
                model.addAttribute("error", "ID de factura inválido");
                model.addAttribute("esAdmin", esAdmin());
                return "error";
            }

            String idLimpio = id.trim();
            Factura factura = facturaService.obtenerFacturaPorId(idLimpio)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + idLimpio));

            // ✅ VALIDACIONES
            if (factura.getCliente() == null) {
                log.warn("⚠️ Factura {} no tiene información del cliente asociada", idLimpio);
                model.addAttribute("warning", "La factura no tiene información del cliente asociada");
            }

            if (factura.getDetalles() == null || factura.getDetalles().isEmpty()) {
                log.warn("⚠️ Factura {} no tiene detalles de productos", idLimpio);
                model.addAttribute("warning", "La factura no tiene detalles de productos");
            } else {
                for (DetalleFactura detalle : factura.getDetalles()) {
                    if (detalle.getProducto() == null) {
                        log.warn("⚠️ Detalle sin producto en factura: {}", idLimpio);
                        model.addAttribute("warning", "La factura contiene detalles sin información del producto");
                        break;
                    }
                }
            }

            model.addAttribute("factura", factura);
            model.addAttribute("esAdmin", esAdmin());

            log.info("✅ Usuario {} cargó detalle de factura {} exitosamente", user, idLimpio);
            return "detalleFactura";
        } catch (Exception e) {
            log.error("❌ Error al cargar detalle de factura {} para usuario {}: {}", id, user, e.getMessage(), e);
            model.addAttribute("error", "No se pudo cargar la factura solicitada: " + e.getMessage());
            model.addAttribute("esAdmin", esAdmin());
            return "error";
        }
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> guardarFacturaDesdeCarrito(@RequestBody Map<String, Object> datos) {
        String user = getCurrentUser();
        long startTime = System.currentTimeMillis();
        log.info("👤 Usuario {} iniciando creación de factura desde carrito", user);

        try {
            log.debug("📦 Datos recibidos para factura: {}", datos);

            // ✅ VALIDACIÓN: Estructura básica de datos
            if (datos == null || datos.isEmpty()) {
                log.warn("⚠️ Usuario {} envió datos de factura vacíos", user);
                return buildErrorResponse("Datos de factura requeridos", HttpStatus.BAD_REQUEST);
            }

            // 1️⃣ Obtener cliente del frontend
            String clienteCodigo = (String) datos.get("clienteId");
            if (clienteCodigo == null || clienteCodigo.trim().isEmpty()) {
                log.warn("⚠️ Usuario {} no proporcionó clienteId", user);
                return buildErrorResponse("clienteId es requerido", HttpStatus.BAD_REQUEST);
            }

            String clienteCodigoLimpio = clienteCodigo.trim();

            // 2️⃣ Obtener datos del formulario
            Map<String, Object> datosEnvio = (Map<String, Object>) datos.get("datosEnvio");
            log.debug("📋 Datos del formulario recibidos: {}", datosEnvio);

            // 3️⃣ Buscar o crear cliente
            Cliente cliente = clienteService.obtenerClientePorCodigo(clienteCodigoLimpio)
                    .orElseGet(() -> {
                        log.info("🔍 Cliente no encontrado en Mongo, buscando en MySQL...");
                        Usuario usuario = usuarioRepository.findByUsername(clienteCodigoLimpio)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en MySQL con username: " + clienteCodigoLimpio));

                        log.info("✅ Usuario encontrado en MySQL: {}", usuario.getUsername());

                        Cliente nuevo = new Cliente();
                        nuevo.setCodigo(usuario.getUsername());
                        nuevo.setNombre(usuario.getUsername() != null ? usuario.getUsername() : "Cliente " + usuario.getUsername());
                        nuevo.setUsername(usuario.getUsername());

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

            log.info("✅ Cliente encontrado/creado: {}", cliente.getCodigo());

            // ✅ ACTUALIZAR CLIENTE CON DATOS DEL FORMULARIO
            if (datosEnvio != null) {
                log.debug("🔄 Actualizando cliente con datos del formulario...");

                String nombre = (String) datosEnvio.get("nombre");
                String apellido = (String) datosEnvio.get("apellido");

                if (nombre != null && apellido != null) {
                    String nombreCompleto = nombre + " " + apellido;
                    cliente.setNombre(nombreCompleto);
                }

                if (datosEnvio.get("email") != null) {
                    cliente.setEmail((String) datosEnvio.get("email"));
                }
                if (datosEnvio.get("telefono") != null) {
                    cliente.setTelefono((String) datosEnvio.get("telefono"));
                }
                if (datosEnvio.get("identificacion") != null) {
                    cliente.setIdentificacion((String) datosEnvio.get("identificacion"));
                }
                if (datosEnvio.get("direccion") != null) {
                    cliente.setDireccion((String) datosEnvio.get("direccion"));
                }

                clienteService.guardarCliente(cliente);
                log.info("✅ Cliente actualizado con datos del formulario: {}", cliente.getNombre());
            }

            // 4️⃣ Convertir productos del carrito
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datos.get("productos");
            if (productos == null || productos.isEmpty()) {
                return buildErrorResponse("La lista de productos está vacía", HttpStatus.BAD_REQUEST);
            }

            List<DetalleFactura> detalles = new ArrayList<>();

            // ✅ VALIDACIÓN: Stock antes de procesar
            for (Map<String, Object> p : productos) {
                String productoId = (String) p.get("id");
                if (productoId == null || productoId.trim().isEmpty()) {
                    return buildErrorResponse("ID de producto inválido", HttpStatus.BAD_REQUEST);
                }

                int cantidad = parseCantidad(p.get("cantidad"));
                if (cantidad <= 0) {
                    return buildErrorResponse("Cantidad debe ser mayor a 0: " + cantidad, HttpStatus.BAD_REQUEST);
                }

                log.debug("🔍 Validando producto ID: {}, cantidad: {}", productoId, cantidad);
                Producto producto = productoService.buscarPorId(productoId.trim())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

                if (producto.getCantidad() < cantidad) {
                    return buildErrorResponse("Stock insuficiente para: " + producto.getNombre() +
                                    ". Stock disponible: " + producto.getCantidad() + ", solicitado: " + cantidad,
                            HttpStatus.BAD_REQUEST);
                }

                if (producto.getPrecio() <= 0) {
                    return buildErrorResponse("El producto " + producto.getNombre() + " no tiene un precio de venta válido",
                            HttpStatus.BAD_REQUEST);
                }

                if (producto.getCostoCompra() <= 0) {
                    double costoCalculado = producto.getPrecio() * 0.6;
                    log.warn("⚠️ Producto sin costo de compra: {}, usando costo calculado: {}",
                            producto.getNombre(), costoCalculado);
                    producto.setCostoCompra(costoCalculado);
                    productoService.guardarProducto(producto);
                }
            }

            // ✅ CREAR DETALLES DE FACTURA
            for (Map<String, Object> p : productos) {
                String productoId = (String) p.get("id");
                int cantidad = parseCantidad(p.get("cantidad"));

                Producto producto = productoService.buscarPorId(productoId.trim())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado después de validación: " + productoId));

                DetalleFactura detalle = new DetalleFactura();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalles.add(detalle);

                log.debug("📦 Producto agregado a factura: {} x {}", producto.getNombre(), cantidad);
            }

            // 5️⃣ CREAR FACTURA
            log.debug("🧾 Creando factura...");
            Factura factura = facturaService.crearFactura(cliente, detalles);
            factura.calcularTotal();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            Map<String, Object> response = buildSuccessResponse("Compra realizada exitosamente");
            response.put("data", Map.of(
                    "facturaId", factura.getId(),
                    "total", factura.getTotal(),
                    "cliente", cliente.getNombre(),
                    "productosCount", factura.getDetalles().size(),
                    "processingTimeMs", duration
            ));

            log.info("✅ Usuario {} creó factura {} exitosamente en {} ms ({} productos)",
                    user, factura.getId(), duration, factura.getDetalles().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("❌ Error al crear factura para usuario {} después de {} ms: {}", user, duration, e.getMessage(), e);
            return buildErrorResponse("Error al crear la factura: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, duration);
        }
    }

    @GetMapping("/finalizada/{id}")
    public String mostrarFacturaFinalizada(@PathVariable String id, Model model) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} viendo factura finalizada: {}", user, id);

        try {
            if (id == null || id.trim().isEmpty()) {
                model.addAttribute("error", "ID de factura inválido");
                model.addAttribute("esAdmin", esAdmin());
                return "error";
            }

            String idLimpio = id.trim();
            Factura factura = facturaService.obtenerFacturaPorId(idLimpio)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            model.addAttribute("factura", factura);

            if (factura.getDetalles() != null) {
                model.addAttribute("productos", factura.getDetalles().stream()
                        .map(DetalleFactura::getProducto)
                        .toList());
            } else {
                model.addAttribute("productos", new ArrayList<>());
            }

            double subtotal = 0;
            if (factura.getDetalles() != null) {
                subtotal = factura.getDetalles().stream()
                        .mapToDouble(detalle -> detalle.getPrecioUnitario() * detalle.getCantidad())
                        .sum();
            }
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("iva", 0);
            model.addAttribute("esAdmin", esAdmin());

            log.info("✅ Usuario {} cargó factura finalizada {} exitosamente", user, idLimpio);
            return "compraFinalizada";
        } catch (Exception e) {
            log.error("❌ Error al cargar factura finalizada {} para usuario {}: {}", id, user, e.getMessage(), e);
            model.addAttribute("error", "No se pudo cargar la factura solicitada: " + e.getMessage());
            model.addAttribute("esAdmin", esAdmin());
            return "error";
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerFacturaApi(@PathVariable String id) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando factura API: {}", user, id);

        try {
            if (id == null || id.trim().isEmpty()) {
                return buildErrorResponse("ID de factura inválido", HttpStatus.BAD_REQUEST);
            }

            String idLimpio = id.trim();
            Factura factura = facturaService.obtenerFacturaPorId(idLimpio)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + idLimpio));

            Map<String, Object> response = buildSuccessResponse("Factura obtenida exitosamente");
            response.put("data", Map.of("factura", factura));

            log.info("✅ Usuario {} obtuvo factura {} exitosamente", user, idLimpio);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener factura {} para usuario {}: {}", id, user, e.getMessage(), e);
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarFacturasApi(
            @RequestParam(defaultValue = "0") @Positive int page,
            @RequestParam(defaultValue = "10") @Positive int size) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando listado de facturas API - Página: {}, Tamaño: {}", user, page, size);

        try {
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100;

            Pageable pageable = PageRequest.of(page, size);
            Page<Factura> facturasPage = facturaService.listarFacturasPaginadas(pageable);

            Map<String, Object> response = buildSuccessResponse("Facturas obtenidas exitosamente");
            response.put("data", Map.of(
                    "facturas", facturasPage.getContent(),
                    "currentPage", page,
                    "totalPages", facturasPage.getTotalPages(),
                    "totalItems", facturasPage.getTotalElements(),
                    "pageSize", size
            ));

            log.info("✅ Usuario {} obtuvo {} facturas exitosamente", user, facturasPage.getNumberOfElements());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al obtener facturas para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al cargar facturas: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/checkout")
    public String mostrarCheckout(Model model) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} accediendo al checkout", user);

        try {
            model.addAttribute("esAdmin", esAdmin());
            return "checkout";
        } catch (Exception e) {
            log.error("❌ Error al cargar checkout para usuario {}: {}", user, e.getMessage(), e);
            model.addAttribute("error", "Error al cargar checkout: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/api/estado/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarEstadoFactura(@PathVariable String id) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} verificando estado de factura: {}", user, id);

        try {
            if (id == null || id.trim().isEmpty()) {
                return buildErrorResponse("ID de factura inválido", HttpStatus.BAD_REQUEST);
            }

            String idLimpio = id.trim();
            Factura factura = facturaService.obtenerFacturaPorId(idLimpio)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

            String nombreCliente = (factura.getCliente() != null) ?
                    factura.getCliente().getNombre() : "Cliente no disponible";

            Map<String, Object> response = buildSuccessResponse("Estado de factura obtenido exitosamente");
            response.put("data", Map.of(
                    "facturaId", factura.getId(),
                    "estado", "COMPLETADA",
                    "total", factura.getTotal(),
                    "fecha", factura.getFecha(),
                    "cliente", nombreCliente,
                    "productosCount", factura.getDetalles() != null ? factura.getDetalles().size() : 0
            ));

            log.info("✅ Usuario {} verificó estado de factura {} exitosamente", user, idLimpio);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al verificar estado de factura {} para usuario {}: {}", id, user, e.getMessage(), e);
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/actualizar-costos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCostosProductos() {
        String user = getCurrentUser();
        log.info("👤 Usuario {} actualizando costos de productos", user);

        try {
            List<Producto> productos = productoService.obtenerTodos();
            int actualizados = 0;
            List<String> productosActualizados = new ArrayList<>();

            for (Producto producto : productos) {
                if (producto.getCostoCompra() <= 0 && producto.getPrecio() > 0) {
                    double nuevoCosto = Math.round(producto.getPrecio() * 0.6 * 100.0) / 100.0; // Redondear a 2 decimales
                    producto.setCostoCompra(nuevoCosto);
                    productoService.guardarProducto(producto);
                    actualizados++;
                    productosActualizados.add(producto.getNombre());
                    log.debug("💰 Actualizado: {} - Precio: {} - Costo: {}",
                            producto.getNombre(), producto.getPrecio(), nuevoCosto);
                }
            }

            Map<String, Object> response = buildSuccessResponse(
                    "Costos de compra actualizados: " + actualizados + " productos");
            response.put("data", Map.of(
                    "actualizados", actualizados,
                    "productos", productosActualizados
            ));

            log.info("✅ Usuario {} actualizó {} productos exitosamente", user, actualizados);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error actualizando costos para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error actualizando costos: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ MÉTODOS AUXILIARES PRIVADOS
    private int parseCantidad(Object cantidadObj) {
        if (cantidadObj instanceof Integer) {
            return (Integer) cantidadObj;
        } else if (cantidadObj instanceof Long) {
            return ((Long) cantidadObj).intValue();
        } else if (cantidadObj instanceof Double) {
            return ((Double) cantidadObj).intValue();
        } else if (cantidadObj instanceof String) {
            try {
                return Integer.parseInt((String) cantidadObj);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cantidad no válida: " + cantidadObj);
            }
        } else {
            throw new RuntimeException("Tipo de cantidad no válido: " +
                    (cantidadObj != null ? cantidadObj.getClass().getSimpleName() : "null"));
        }
    }

    private Map<String, Object> buildSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String error, HttpStatus status) {
        return buildErrorResponse(error, status, 0);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String error, HttpStatus status, long processingTimeMs) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", error);
        response.put("timestamp", LocalDateTime.now());
        if (processingTimeMs > 0) {
            response.put("processingTimeMs", processingTimeMs);
        }
        return ResponseEntity.status(status).body(response);
    }
}