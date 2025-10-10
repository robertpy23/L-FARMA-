package com.App.Lfarma.controller;

import com.App.Lfarma.entity.Usuario;
import com.App.Lfarma.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registerSuccess", required = false) String registerSuccess,
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

    @GetMapping("/api/usuario-actual")
    @ResponseBody
    public Map<String, String> getUsuarioActual(Authentication authentication) {
        Map<String, String> usuario = new HashMap<>();
        usuario.put("username", authentication.getName());
        return usuario;
    }

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
            Model model) {

        if (usuarioService.existeUsuario(username)) {
            model.addAttribute("error", "El usuario ya existe");
            return "register";
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(password);
        nuevoUsuario.setRol(role);

        usuarioService.registrar(nuevoUsuario);

        return "redirect:/login?registerSuccess";
    }

    @GetMapping("/dashboard_admin")
    public String dashboardAdmin() {
        return "dashboard_admin";
    }

    @GetMapping("/dashboard_empleado")
    public String dashboardEmpleado() {
        return "dashboard_empleado";
    }

    @GetMapping("/vistaClientes")
    public String dashboardCliente() {
        return "vistaClientes";
    }
}