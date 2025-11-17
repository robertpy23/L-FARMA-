package com.App.Lfarma.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @GetMapping("")
    public String mostrarCheckout(Model model) {
        // Esta podría ser una página diferente
        return "checkout"; // Asegúrate de que checkout.html existe
    }

    @GetMapping("/pasos")
    public String checkoutPasos(Model model) {
        // Agregar datos necesarios al modelo
        model.addAttribute("titulo", "Proceso de Pago - L-FARMA");
        model.addAttribute("pasoActual", 1);

        return "checkout-pasos";
    }
}