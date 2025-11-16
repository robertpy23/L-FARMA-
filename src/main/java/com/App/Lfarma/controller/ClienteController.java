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
import com.App.Lfarma.DTO.DireccionDTO;
import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.service.ClienteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@Controller
@RequestMapping("/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ClienteService clienteService;

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

    // ✅ CORREGIDO: Implementar paginación robusta en listarClientes
    @GetMapping
    public String listarClientes(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "15") @Positive int size,
            @RequestParam(required = false) String search,
            Model model) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} listando clientes - Página: {}, Tamaño: {}, Búsqueda: '{}'", user, page, size, search);

        try {
            // ✅ CORREGIDO: Validar parámetros de paginación
            if (size <= 0) size = 15;
            if (size > 100) size = 100;

            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage;

            if (search != null && !search.trim().isEmpty()) {
                String terminoBusqueda = search.trim();
                clientesPage = clienteService.buscarClientes(terminoBusqueda, pageable);
                model.addAttribute("search", terminoBusqueda);
                log.debug("🔍 Búsqueda de clientes: '{}' - Encontrados: {}", terminoBusqueda, clientesPage.getTotalElements());
            } else {
                clientesPage = clienteService.listarClientesPaginados(pageable);
                log.debug("📋 Listado normal de clientes - Total: {}", clientesPage.getTotalElements());
            }

            // ✅ CORREGIDO: Verificar si hay datos
            if (clientesPage.getContent().isEmpty()) {
                log.warn("⚠️ No se encontraron clientes con los criterios especificados");
                model.addAttribute("warning", "No se encontraron clientes con los criterios de búsqueda");
            }

            model.addAttribute("clientes", clientesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", clientesPage.getTotalPages());
            model.addAttribute("totalItems", clientesPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("esAdmin", esAdmin());

            log.info("✅ Usuario {} cargó {} clientes exitosamente (página {} de {})",
                    user, clientesPage.getNumberOfElements(), page + 1, clientesPage.getTotalPages());

        } catch (Exception e) {
            log.error("❌ Error en listarClientes para usuario {}: {}", user, e.getMessage(), e);
            model.addAttribute("error", "Error al cargar clientes: " + e.getMessage());
            model.addAttribute("clientes", List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("pageSize", size);
        }

        return "clientes";
    }

    // ✅ CORREGIDO: Mejor manejo de agregar cliente
    @PostMapping("/agregar")
    public String agregarCliente(@Valid Cliente cliente, RedirectAttributes redirectAttributes) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} agregando nuevo cliente: {}", user, cliente.getCodigo());

        try {
            Cliente clienteGuardado = clienteService.validarYGuardarCliente(cliente);
            redirectAttributes.addFlashAttribute("success",
                    "Cliente '" + clienteGuardado.getNombre() + "' agregado exitosamente");
            log.info("✅ Usuario {} agregó cliente {} exitosamente", user, clienteGuardado.getCodigo());
        } catch (Exception e) {
            log.error("❌ Error al agregar cliente para usuario {}: {}", user, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al agregar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    // ✅ CORREGIDO: Mejor manejo de formulario de edición
    @GetMapping("/editar/{codigo}")
    public String mostrarFormularioEditar(@PathVariable String codigo, Model model, RedirectAttributes redirectAttributes) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} editando cliente: {}", user, codigo);

        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Código de cliente inválido");
                return "redirect:/clientes";
            }

            String codigoLimpio = codigo.trim();
            Optional<Cliente> cliente = clienteService.obtenerClientePorCodigo(codigoLimpio);

            if (cliente.isPresent()) {
                model.addAttribute("cliente", cliente.get());
                log.info("✅ Usuario {} cargó formulario de edición para cliente {}", user, codigoLimpio);
                return "editarCliente";
            } else {
                log.warn("⚠️ Usuario {} intentó editar cliente no encontrado: {}", user, codigoLimpio);
                redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
                return "redirect:/clientes";
            }
        } catch (Exception e) {
            log.error("❌ Error al cargar cliente {} para usuario {}: {}", codigo, user, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar cliente: " + e.getMessage());
            return "redirect:/clientes";
        }
    }

    // ✅ CORREGIDO: Mejor manejo de actualización
    @PostMapping("/actualizar")
    public String actualizarCliente(@Valid Cliente cliente, RedirectAttributes redirectAttributes) {
        String user = getCurrentUser();

        // ✅ PROTEGER: Solo admin puede actualizar clientes existentes
        if (!esAdmin()) {
            log.warn("🚫 Usuario {} sin permisos intentó actualizar cliente: {}", user, cliente.getCodigo());
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para actualizar clientes");
            return "redirect:/clientes";
        }

        log.info("👤 Usuario {} actualizando cliente: {}", user, cliente.getCodigo());

        try {
            Cliente clienteActualizado = clienteService.validarYGuardarCliente(cliente);
            redirectAttributes.addFlashAttribute("success",
                    "Cliente '" + clienteActualizado.getNombre() + "' actualizado exitosamente");
            log.info("✅ Usuario {} actualizó cliente {} exitosamente", user, clienteActualizado.getCodigo());
        } catch (Exception e) {
            log.error("❌ Error al actualizar cliente {} para usuario {}: {}", cliente.getCodigo(), user, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    // ✅ CORREGIDO: Mejor manejo de eliminación
    @PostMapping("/eliminar")
    public String eliminarCliente(@RequestParam String codigo, RedirectAttributes redirectAttributes) {
        String user = getCurrentUser();

        // ✅ PROTEGER: Solo admin puede eliminar clientes
        if (!esAdmin()) {
            log.warn("🚫 Usuario {} sin permisos intentó eliminar cliente: {}", user, codigo);
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar clientes");
            return "redirect:/clientes";
        }

        log.info("👤 Usuario {} eliminando cliente: {}", user, codigo);

        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Código de cliente inválido");
                return "redirect:/clientes";
            }

            String codigoLimpio = codigo.trim();
            Optional<Cliente> clienteExistente = clienteService.obtenerClientePorCodigo(codigoLimpio);

            if (clienteExistente.isPresent()) {
                String nombreCliente = clienteExistente.get().getNombre();
                clienteService.eliminarCliente(codigoLimpio);
                redirectAttributes.addFlashAttribute("success",
                        "Cliente '" + nombreCliente + "' eliminado exitosamente");
                log.info("✅ Usuario {} eliminó cliente {} exitosamente", user, codigoLimpio);
            } else {
                log.warn("⚠️ Usuario {} intentó eliminar cliente no encontrado: {}", user, codigoLimpio);
                redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar cliente {} para usuario {}: {}", codigo, user, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    // ✅ CORREGIDO: Mejor manejo de actualización de dirección
    @PostMapping("/api/direccion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarDireccion(@Valid @RequestBody DireccionDTO dto) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} actualizando dirección", user);

        try {
            String username = obtenerUsernameAutenticado(dto);
            if (username == null || username.isBlank()) {
                log.warn("🚫 Usuario no autenticado intentó actualizar dirección");
                return buildErrorResponse("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
            }

            Cliente cliente = clienteService.obtenerClientePorUsername(username)
                    .orElseThrow(() -> {
                        log.error("❌ Cliente no encontrado: {}", username);
                        return new RuntimeException("Cliente no encontrado: " + username);
                    });

            // ✅ Validar coordenadas
            if (dto.getLatitud() == null || dto.getLongitud() == null) {
                return buildErrorResponse("Coordenadas inválidas", HttpStatus.BAD_REQUEST);
            }

            if (dto.getLatitud() < -90 || dto.getLatitud() > 90 ||
                    dto.getLongitud() < -180 || dto.getLongitud() > 180) {
                return buildErrorResponse("Coordenadas fuera de rango válido", HttpStatus.BAD_REQUEST);
            }

            // ✅ Actualizar datos de ubicación
            Cliente clienteActualizado = clienteService.actualizarUbicacionCliente(
                    cliente.getCodigo(),
                    dto.getLatitud(),
                    dto.getLongitud(),
                    dto.getDireccionTexto()
            );

            log.info("✅ Dirección actualizada para usuario: {} - Lat: {} Lng: {} - Dir: {}",
                    username, dto.getLatitud(), dto.getLongitud(), dto.getDireccionTexto());

            Map<String, Object> response = buildSuccessResponse("Dirección actualizada correctamente");
            response.put("data", Map.of(
                    "cliente", clienteActualizado.getNombre(),
                    "direccion", clienteActualizado.getDireccion(),
                    "latitud", clienteActualizado.getLatitud(),
                    "longitud", clienteActualizado.getLongitud()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al actualizar dirección para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al actualizar dirección: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ MÉTODOS AUXILIARES PRIVADOS
    private String obtenerUsernameAutenticado(DireccionDTO dto) {
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            return dto.getUsername();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }

        return null;
    }

    @GetMapping("/api/direccion/actual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDireccionActual(Authentication authentication) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} obteniendo dirección actual", user);

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return buildErrorResponse("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
            }

            String username = authentication.getName();
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsername(username);

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();

                Map<String, Object> response = buildSuccessResponse("Dirección actual obtenida exitosamente");
                response.put("data", Map.of(
                        "direccion", cliente.getDireccion() != null ? cliente.getDireccion() : "",
                        "latitud", cliente.getLatitud() != null ? cliente.getLatitud() : 0,
                        "longitud", cliente.getLongitud() != null ? cliente.getLongitud() : 0
                ));

                log.info("✅ Usuario {} obtuvo dirección actual exitosamente", user);
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ Cliente no encontrado para usuario: {}", username);
                return buildErrorResponse("Cliente no encontrado", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("❌ Error al obtener dirección actual para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al obtener dirección: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: API para listar clientes con paginación
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarClientesApi(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size,
            @RequestParam(required = false) String search) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando listado de clientes API - Página: {}, Tamaño: {}, Búsqueda: '{}'",
                user, page, size, search);

        try {
            // Validar parámetros
            if (size <= 0) size = 20;
            if (size > 100) size = 100;

            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage;

            if (search != null && !search.trim().isEmpty()) {
                clientesPage = clienteService.buscarClientes(search.trim(), pageable);
            } else {
                clientesPage = clienteService.listarClientesPaginados(pageable);
            }

            Map<String, Object> response = buildSuccessResponse("Clientes obtenidos exitosamente");
            response.put("data", Map.of(
                    "clientes", clientesPage.getContent(),
                    "currentPage", page,
                    "totalPages", clientesPage.getTotalPages(),
                    "totalItems", clientesPage.getTotalElements(),
                    "pageSize", size,
                    "search", search != null ? search : ""
            ));

            log.info("✅ Usuario {} obtuvo {} clientes exitosamente", user, clientesPage.getNumberOfElements());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en /clientes/api para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al cargar clientes: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: Buscar cliente por código (API)
    @GetMapping("/api/{codigo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerClientePorCodigoApi(@PathVariable String codigo) {
        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando cliente por código: {}", user, codigo);

        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                return buildErrorResponse("Código de cliente inválido", HttpStatus.BAD_REQUEST);
            }

            String codigoLimpio = codigo.trim();
            Optional<Cliente> cliente = clienteService.obtenerClientePorCodigo(codigoLimpio);

            if (cliente.isPresent()) {
                Map<String, Object> response = buildSuccessResponse("Cliente obtenido exitosamente");
                response.put("data", Map.of("cliente", cliente.get()));

                log.info("✅ Usuario {} obtuvo cliente {} exitosamente", user, codigoLimpio);
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ Usuario {} solicitó cliente no encontrado: {}", user, codigoLimpio);
                return buildErrorResponse("Cliente no encontrado con código: " + codigoLimpio, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("❌ Error en /clientes/api/{} para usuario {}: {}", codigo, user, e.getMessage(), e);
            return buildErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: Buscar clientes por nombre (API)
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarClientesPorNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} buscando clientes por nombre: '{}' - Página: {}, Tamaño: {}", user, nombre, page, size);

        try {
            // Validar parámetros
            if (nombre == null || nombre.trim().isEmpty()) {
                return buildErrorResponse("Término de búsqueda inválido", HttpStatus.BAD_REQUEST);
            }

            if (size <= 0) size = 10;
            if (size > 50) size = 50;

            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage = clienteService.buscarClientesPorNombre(nombre.trim(), pageable);

            Map<String, Object> response = buildSuccessResponse("Clientes obtenidos exitosamente");
            response.put("data", Map.of(
                    "clientes", clientesPage.getContent(),
                    "currentPage", page,
                    "totalPages", clientesPage.getTotalPages(),
                    "totalItems", clientesPage.getTotalElements(),
                    "searchTerm", nombre
            ));

            log.info("✅ Usuario {} encontró {} clientes por nombre '{}'", user, clientesPage.getNumberOfElements(), nombre);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en /clientes/api/buscar para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al buscar clientes: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: Búsqueda general avanzada (API)
    @GetMapping("/api/buscar-avanzado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarClientesAvanzado(
            @RequestParam String termino,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "15") @Positive int size) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} búsqueda avanzada: '{}' - Página: {}, Tamaño: {}", user, termino, page, size);

        try {
            // Validar parámetros
            if (termino == null || termino.trim().isEmpty()) {
                return buildErrorResponse("Término de búsqueda inválido", HttpStatus.BAD_REQUEST);
            }

            if (size <= 0) size = 15;
            if (size > 50) size = 50;

            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage = clienteService.buscarClientes(termino.trim(), pageable);

            Map<String, Object> response = buildSuccessResponse("Clientes obtenidos exitosamente");
            response.put("data", Map.of(
                    "clientes", clientesPage.getContent(),
                    "currentPage", page,
                    "totalPages", clientesPage.getTotalPages(),
                    "totalItems", clientesPage.getTotalElements(),
                    "searchTerm", termino
            ));

            log.info("✅ Usuario {} encontró {} clientes en búsqueda avanzada '{}'",
                    user, clientesPage.getNumberOfElements(), termino);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en /clientes/api/buscar-avanzado para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error en búsqueda avanzada: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: Estadísticas de clientes (API)
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasClientes() {
        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando estadísticas de clientes", user);

        try {
            Map<String, Object> estadisticas = clienteService.obtenerEstadisticasClientes();

            Map<String, Object> response = buildSuccessResponse("Estadísticas obtenidas exitosamente");
            response.put("data", Map.of("estadisticas", estadisticas));

            log.info("✅ Usuario {} obtuvo estadísticas de clientes exitosamente", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en /clientes/api/estadisticas para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al obtener estadísticas: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: Clientes con ubicación completa (para envíos)
    @GetMapping("/api/con-ubicacion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerClientesConUbicacion(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando clientes con ubicación - Página: {}, Tamaño: {}", user, page, size);

        try {
            // Validar parámetros
            if (size <= 0) size = 20;
            if (size > 50) size = 50;

            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage = clienteService.obtenerClientesConDireccionCompleta(pageable);

            Map<String, Object> response = buildSuccessResponse("Clientes con ubicación obtenidos exitosamente");
            response.put("data", Map.of(
                    "clientes", clientesPage.getContent(),
                    "currentPage", page,
                    "totalPages", clientesPage.getTotalPages(),
                    "totalItems", clientesPage.getTotalElements(),
                    "pageSize", size
            ));

            log.info("✅ Usuario {} obtuvo {} clientes con ubicación completa", user, clientesPage.getNumberOfElements());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en /clientes/api/con-ubicacion para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al cargar clientes con ubicación: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CORREGIDO: Clientes recientes (para dashboard)
    @GetMapping("/api/recientes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerClientesRecientes(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size) {

        String user = getCurrentUser();
        log.info("👤 Usuario {} solicitando clientes recientes - Página: {}, Tamaño: {}", user, page, size);

        try {
            // Validar parámetros
            if (size <= 0) size = 10;
            if (size > 30) size = 30;

            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage = clienteService.obtenerClientesRecientes(pageable);

            Map<String, Object> response = buildSuccessResponse("Clientes recientes obtenidos exitosamente");
            response.put("data", Map.of(
                    "clientes", clientesPage.getContent(),
                    "currentPage", page,
                    "totalPages", clientesPage.getTotalPages(),
                    "totalItems", clientesPage.getTotalElements(),
                    "pageSize", size
            ));

            log.info("✅ Usuario {} obtuvo {} clientes recientes", user, clientesPage.getNumberOfElements());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en /clientes/api/recientes para usuario {}: {}", user, e.getMessage(), e);
            return buildErrorResponse("Error al cargar clientes recientes: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ MÉTODOS AUXILIARES PARA RESPUESTAS ESTANDARIZADAS
    private Map<String, Object> buildSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String error, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", error);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }
}