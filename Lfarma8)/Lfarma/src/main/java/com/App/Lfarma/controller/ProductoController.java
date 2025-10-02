package com.App.Lfarma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.service.ProductoService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    private List<String> obtenerCategorias() {
        return Arrays.asList("Medicamento", "Higiene", "Cosmético", "Suplemento", "Otros");
    }

    @GetMapping
    public String listarProductos(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        List<String> categorias = obtenerCategorias();
        model.addAttribute("categorias", categorias);

        Sort sort = Sort.by(sortBy);
        sort = sortDir.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Producto> pageProductos = productoService.buscarProductos(searchTerm, categoria, pageable);

        model.addAttribute("productos", pageProductos.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageProductos.getTotalPages());
        model.addAttribute("totalItems", pageProductos.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("categoria", categoria);

        return "visualizar-productos";
    }

    @GetMapping("/registrar-productos")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", obtenerCategorias());
        return "registrar-productos";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", obtenerCategorias());
            model.addAttribute("error", "Por favor, verifica los datos del formulario.");
            return "registrar-productos";
        }

        try {
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success", "Producto registrado exitosamente");
            return "redirect:/productos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", obtenerCategorias());
            return "registrar-productos";
        }
    }

    @GetMapping("/actualizar-productos")
    public String mostrarFormularioEditar(@RequestParam String id, Model model) {
        Optional<Producto> producto = productoService.buscarPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            model.addAttribute("categorias", obtenerCategorias());
            return "actualizar-productos";
        } else {
            return "redirect:/productos";
        }
    }

    @PostMapping("/actualizar")
    public String actualizarProducto(@ModelAttribute Producto producto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", obtenerCategorias());
            model.addAttribute("error", "Por favor, verifica los datos del formulario.");
            return "actualizar-productos";
        }

        try {
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success", "Producto actualizado exitosamente");
            return "redirect:/productos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", obtenerCategorias());
            return "actualizar-productos";
        }
    }

    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam String id, RedirectAttributes redirectAttributes) {
        productoService.eliminarPorId(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente");
        return "redirect:/productos";
    }

    @GetMapping("/buscar-productos")
    public String mostrarFormularioBuscar(Model model) {
        model.addAttribute("producto", new Producto());
        return "buscar-productos";
    }

    @PostMapping("/buscar")
    public String buscarProductoPorCodigo(@RequestParam String codigoProducto, Model model) {
        Optional<Producto> producto = productoService.buscarPorCodigo(codigoProducto);

        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "resultado-busqueda";
        } else {
            model.addAttribute("mensaje", "Producto no encontrado");
            return "buscar-productos";
        }
    }
}
