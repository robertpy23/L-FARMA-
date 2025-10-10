// RegistroController.java
package com.App.Lfarma.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.App.Lfarma.dto.RegistroDTO;
import com.App.Lfarma.service.RegistroService;
import com.App.Lfarma.entity.Cliente;

@Controller
@RequestMapping("/auth")
public class RegistroController {

    @Autowired
    private RegistroService registroService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registroDTO", new RegistroDTO());
        return "register"; // views/register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegistroDTO registroDTO, RedirectAttributes redirect) {
        try {
            Cliente cliente = registroService.registrarUsuarioYCliente(registroDTO);
            redirect.addFlashAttribute("success", "Registro exitoso. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}

