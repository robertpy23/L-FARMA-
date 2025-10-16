package com.App.Lfarma.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @GetMapping
    public String mostrarCheckout(Model model) {
        return "checkout";
    }

    // CORREGIR: Ruta correcta
    @GetMapping("/pasos")
    public String checkoutPasos() {
        return "checkout-pasos";
    }
}