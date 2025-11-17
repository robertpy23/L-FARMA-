package com.App.Lfarma.controller;

import com.App.Lfarma.entity.Usuario;
import com.App.Lfarma.service.UsuarioService;
import com.App.Lfarma.service.DashboardService;
import com.App.Lfarma.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private FacturaService facturaService;

    // === LOGIN ===
    @GetMapping("/login")
    public String loginForm(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registerSuccess,
            Model model) {

        if(error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }

        if(logout != null) {
            model.addAttribute("message", "Has cerrado sesión correctamente");
        }

        if(registerSuccess != null) {
            model.addAttribute("message", "Registro exitoso! Por favor inicia sesión");
        }

        return "login";
    }

    // === API ===
    @GetMapping("/api/usuario-actual")
    @ResponseBody
    public Map<String, String> getUsuarioActual(Authentication authentication) {
        Map<String, String> usuario = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            usuario.put("username", authentication.getName());
        }
        return usuario;
    }

    // === REGISTRO UNIVERSAL ===
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        return procesarRegistro(username, password, role, redirectAttributes, "Registro exitoso. Ya puedes iniciar sesión.");
    }

    // Endpoint alternativo para compatibilidad
    @PostMapping("/auth/register")
    public String registerAuth(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        return procesarRegistro(username, password, role, redirectAttributes, "Registro exitoso. Ya puedes iniciar sesión.");
    }

    // === REGISTRO RÁPIDO ADMIN ===
    @PostMapping("/register-admin")
    public String registerAdmin(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        return procesarRegistro(username, password, "ADMIN", redirectAttributes, "Administrador registrado exitosamente.");
    }

    // === REGISTRO RÁPIDO EMPLEADO ===
    @PostMapping("/register-empleado")
    public String registerEmpleado(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        return procesarRegistro(username, password, "EMPLEADO", redirectAttributes, "Empleado registrado exitosamente.");
    }

    // === MÉTODO PRIVADO PARA REUTILIZAR LÓGICA ===
    private String procesarRegistro(String username, String password, String role,
                                    RedirectAttributes redirectAttributes, String mensajeExito) {

        try {
            if (usuarioService.existeUsuario(username)) {
                redirectAttributes.addFlashAttribute("error", "El usuario ya existe");
                return "redirect:/register";
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setPassword(password);
            nuevoUsuario.setRol(role);

            usuarioService.registrar(nuevoUsuario);

            redirectAttributes.addFlashAttribute("success", mensajeExito);
            return "redirect:/login?registerSuccess";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error en el registro: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // === DASHBOARDS ===
    @GetMapping("/dashboard_admin")
    public String dashboardAdmin(Model model) {
        try {
            long totalClientes = dashboardService.countClientes();
            long totalProductos = dashboardService.countProductos();
            int ventasHoy = dashboardService.countVentasHoy();
            double ingresosHoy = dashboardService.ingresosHoy();
            double gananciaNeta = dashboardService.gananciaNetaHoy();
            long alertasStock = dashboardService.alertasStock(5);

            model.addAttribute("totalClientes", totalClientes);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("ventasHoy", ventasHoy);
            model.addAttribute("ingresosHoy", ingresosHoy);
            model.addAttribute("gananciaNeta", gananciaNeta);
            model.addAttribute("alertasStock", alertasStock);
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo cargar la información del dashboard: " + e.getMessage());
        }
        return "dashboard_admin";
    }

    @GetMapping("/dashboard_empleado")
    public String dashboardEmpleado(Model model) {
        try {
            long totalClientes = dashboardService.countClientes();
            long totalProductos = dashboardService.countProductos();
            int ventasHoy = dashboardService.countVentasHoy();
            double ingresosHoy = dashboardService.ingresosHoy();

            model.addAttribute("totalClientes", totalClientes);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("ventasHoy", ventasHoy);
            model.addAttribute("ingresosHoy", ingresosHoy);

            // Ventas recientes (últimas 5)
            try {
                var ventasRecientes = facturaService.obtenerFacturasRecientes(5);
                model.addAttribute("ventasRecientes", ventasRecientes);
            } catch (Exception e) {
                model.addAttribute("ventasRecientes", new java.util.ArrayList<>());
            }

        } catch (Exception e) {
            model.addAttribute("error", "No se pudo cargar la información del dashboard empleado: " + e.getMessage());
        }
        return "dashboard_empleado";
    }

    @GetMapping("/vistaClientes")
    public String dashboardCliente() {
        return "vistaClientes";
    }
}