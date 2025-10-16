package com.App.Lfarma.controller;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
}