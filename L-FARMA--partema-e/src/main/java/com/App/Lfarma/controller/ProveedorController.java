package com.App.Lfarma.controller;

import com.App.Lfarma.entity.Proveedor;
import com.App.Lfarma.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive; // ✅ IMPORTACIÓN FALTANTE
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Validated
@Controller
@RequestMapping("/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    private boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("esAdmin")
    public boolean esAdminAttribute() {
        return esAdmin();
    }

    // ✅ VISTA PRINCIPAL DE PROVEEDORES - CORREGIDO
    @GetMapping
    public String listarProveedores(
            @RequestParam(defaultValue = "0") int page, // ✅ QUITAR @Positive TEMPORALMENTE
            @RequestParam(defaultValue = "15") int size, // ✅ QUITAR @Positive TEMPORALMENTE
            @RequestParam(required = false) String search,
            Model model) {

        try {
            if (page < 0) page = 0;
            if (size <= 0) size = 15;
            if (size > 100) size = 100;

            Pageable pageable = PageRequest.of(page, size);
            Page<Proveedor> proveedoresPage;

            if (search != null && !search.trim().isEmpty()) {
                proveedoresPage = proveedorService.buscarProveedores(search.trim(), pageable);
                model.addAttribute("search", search.trim());
            } else {
                proveedoresPage = proveedorService.listarProveedoresPaginados(pageable);
            }

            model.addAttribute("proveedores", proveedoresPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", proveedoresPage.getTotalPages());
            model.addAttribute("totalItems", proveedoresPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("esAdmin", esAdmin());

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar proveedores: " + e.getMessage());
            model.addAttribute("proveedores", java.util.List.of());
        }

        return "proveedores";
    }

    // ✅ FORMULARIO NUEVO PROVEEDOR
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        if (!esAdmin()) {
            return "redirect:/proveedores?error=Sin permisos";
        }
        model.addAttribute("proveedor", new Proveedor());
        return "form-proveedor";
    }

    // ✅ GUARDAR PROVEEDOR
    @PostMapping("/guardar")
    public String guardarProveedor(@Validated Proveedor proveedor, RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para gestionar proveedores");
            return "redirect:/proveedores";
        }

        try {
            Proveedor proveedorGuardado = proveedorService.guardarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("success",
                    "Proveedor '" + proveedorGuardado.getNombre() + "' guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar proveedor: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    // ✅ EDITAR PROVEEDOR
    @GetMapping("/editar/{codigo}")
    public String mostrarFormularioEditar(@PathVariable String codigo, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar proveedores");
            return "redirect:/proveedores";
        }

        try {
            Optional<Proveedor> proveedor = proveedorService.obtenerProveedorPorCodigo(codigo);
            if (proveedor.isPresent()) {
                model.addAttribute("proveedor", proveedor.get());
                return "form-proveedor";
            } else {
                redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
                return "redirect:/proveedores";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar proveedor: " + e.getMessage());
            return "redirect:/proveedores";
        }
    }

    // ✅ ELIMINAR PROVEEDOR
    @PostMapping("/eliminar")
    public String eliminarProveedor(@RequestParam String codigo, RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar proveedores");
            return "redirect:/proveedores";
        }

        try {
            proveedorService.eliminarProveedor(codigo);
            redirectAttributes.addFlashAttribute("success", "Proveedor eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar proveedor: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    // ✅ API PARA OBTENER PROVEEDORES - CORREGIDO
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarProveedoresApi(
            @RequestParam(defaultValue = "0") int page, // ✅ QUITAR @Positive
            @RequestParam(defaultValue = "20") int size) { // ✅ QUITAR @Positive

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Proveedor> proveedoresPage = proveedorService.listarProveedoresPaginados(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("proveedores", proveedoresPage.getContent());
            response.put("currentPage", page);
            response.put("totalPages", proveedoresPage.getTotalPages());
            response.put("totalItems", proveedoresPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}