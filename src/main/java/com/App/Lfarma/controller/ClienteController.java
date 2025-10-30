package com.App.Lfarma.controller;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import com.App.Lfarma.DTO.DireccionDTO;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listarClientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        return "clientes";
    }

    @PostMapping("/agregar")
    public String agregarCliente(Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya existe un cliente con el mismo código
            Optional<Cliente> clienteExistente = clienteService.obtenerClientePorCodigo(cliente.getCodigo());
            if (clienteExistente.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un cliente con el código " + cliente.getCodigo());
                return "redirect:/clientes";
            }
            
            clienteService.agregarCliente(cliente);
            redirectAttributes.addFlashAttribute("success", "Cliente agregado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{codigo}")
    public String mostrarFormularioEditar(@PathVariable String codigo, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cliente> cliente = clienteService.obtenerClientePorCodigo(codigo);
        if (cliente.isPresent()) {
            model.addAttribute("cliente", cliente.get());
            return "editarCliente";
        } else {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
            return "redirect:/clientes";
        }
    }

    @PostMapping("/actualizar")
    public String actualizarCliente(Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            clienteService.guardarCliente(cliente);
            redirectAttributes.addFlashAttribute("success", "Cliente actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/eliminar")
    public String eliminarCliente(@RequestParam String codigo, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminarCliente(codigo);
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }
    @PostMapping("/api/direccion")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> actualizarDireccion(@RequestBody DireccionDTO dto) {
        try {
            // ✅ Obtener username de forma segura
            String username = obtenerUsernameAutenticado(dto);

            if (username == null || username.isBlank()) {
                return crearRespuestaError(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
            }

            // ✅ Buscar cliente
            Cliente cliente = clienteService.obtenerClientePorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + username));

            // ✅ Validar coordenadas
            ResponseEntity<?> validacion = validarCoordenadas(dto);
            if (validacion != null) {
                return validacion;
            }

            // ✅ Actualizar datos de ubicación
            actualizarUbicacionCliente(cliente, dto);
            clienteService.guardarCliente(cliente);

            logGuardadoExitoso(username, dto);

            return crearRespuestaExito("Dirección actualizada correctamente");

        } catch (Exception e) {
            logError(e);
            return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ✅ MÉTODOS AUXILIARES PRIVADOS
    private String obtenerUsernameAutenticado(DireccionDTO dto) {
        // Priorizar username del DTO si viene
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            return dto.getUsername();
        }

        // Obtener del contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }

        return null;
    }

    private ResponseEntity<?> validarCoordenadas(DireccionDTO dto) {
        if (dto.getLatitud() == null || dto.getLongitud() == null) {
            return crearRespuestaError(HttpStatus.BAD_REQUEST, "Coordenadas inválidas");
        }

        if (dto.getLatitud() < -90 || dto.getLatitud() > 90 ||
                dto.getLongitud() < -180 || dto.getLongitud() > 180) {
            return crearRespuestaError(HttpStatus.BAD_REQUEST, "Coordenadas fuera de rango válido");
        }

        return null;
    }

    private void actualizarUbicacionCliente(Cliente cliente, DireccionDTO dto) {
        cliente.setDireccion(dto.getDireccionTexto());
        cliente.setLatitud(dto.getLatitud());
        cliente.setLongitud(dto.getLongitud());
    }

    private void logGuardadoExitoso(String username, DireccionDTO dto) {
        System.out.println("✅ Dirección actualizada para usuario: " + username +
                " - Lat: " + dto.getLatitud() + " Lng: " + dto.getLongitud());
    }

    private void logError(Exception e) {
        System.err.println("❌ Error al actualizar dirección: " + e.getMessage());
    }

    private ResponseEntity<?> crearRespuestaExito(String mensaje) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", mensaje
        ));
    }

    private ResponseEntity<?> crearRespuestaError(HttpStatus status, String error) {
        return ResponseEntity.status(status)
                .body(Map.of("success", false, "error", error));
    }
    @GetMapping("/api/direccion/actual")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerDireccionActual(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return crearRespuestaError(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
            }

            String username = authentication.getName(); // ✅ Esta variable es "effectively final"
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsername(username);

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "direccion", cliente.getDireccion() != null ? cliente.getDireccion() : "",
                        "latitud", cliente.getLatitud(),
                        "longitud", cliente.getLongitud()
                ));
            } else {
                return crearRespuestaError(HttpStatus.NOT_FOUND, "Cliente no encontrado");
            }
        } catch (Exception e) {
            logError(e);
            return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}