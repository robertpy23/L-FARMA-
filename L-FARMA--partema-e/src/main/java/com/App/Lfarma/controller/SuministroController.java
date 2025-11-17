package com.App.Lfarma.controller;

import com.App.Lfarma.entity.*;
import com.App.Lfarma.service.ProveedorService;
import com.App.Lfarma.service.SuministroService;
import com.App.Lfarma.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/suministros")
@CrossOrigin(origins = "*")
public class SuministroController {

    @Autowired
    private SuministroService suministroService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoService productoService;

    private boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    // ✅ VISTA PRINCIPAL DE SUMINISTROS
    @GetMapping
    public String listarSuministros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Suministro> suministrosPage = suministroService.listarSuministrosPaginados(pageable);

            model.addAttribute("suministros", suministrosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", suministrosPage.getTotalPages());
            model.addAttribute("totalItems", suministrosPage.getTotalElements());
            model.addAttribute("pageSize", size);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar suministros: " + e.getMessage());
            model.addAttribute("suministros", List.of());
        }

        return "suministros";
    }

    // ✅ FORMULARIO NUEVO SUMINISTRO
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        if (!esAdmin()) {
            return "redirect:/suministros?error=Sin permisos";
        }

        List<Proveedor> proveedores = proveedorService.listarProveedores();
        List<Producto> productos = productoService.listarProductos();

        model.addAttribute("suministro", new Suministro());
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("productos", productos);
        model.addAttribute("detalles", new ArrayList<DetalleSuministro>());

        return "form-suministro";
    }

    // ✅ GUARDAR SUMINISTRO (ACTUALIZA STOCK AUTOMÁTICAMENTE)
    @PostMapping("/guardar")
    public String guardarSuministro(
            @RequestParam String proveedorId,
            @RequestParam List<String> productoIds,
            @RequestParam List<Integer> cantidades,
            @RequestParam List<Double> preciosCompra,
            @RequestParam List<Double> preciosVenta,
            @RequestParam(required = false) List<String> lotes,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para registrar suministros");
            return "redirect:/suministros";
        }

        try {
            // Obtener proveedor
            Optional<Proveedor> proveedorOpt = proveedorService.obtenerProveedorPorId(proveedorId);
            if (!proveedorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
                return "redirect:/suministros/nuevo";
            }

            // Crear suministro
            Suministro suministro = new Suministro();
            suministro.setProveedor(proveedorOpt.get());
            suministro.setNumeroFactura("FAC-" + System.currentTimeMillis());

            // Crear detalles
            List<DetalleSuministro> detalles = new ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                Optional<Producto> productoOpt = productoService.buscarPorId(productoIds.get(i));
                if (productoOpt.isPresent()) {
                    DetalleSuministro detalle = new DetalleSuministro();
                    detalle.setProducto(productoOpt.get());
                    detalle.setCantidad(cantidades.get(i));
                    detalle.setPrecioCompra(preciosCompra.get(i));
                    detalle.setPrecioVentaSugerido(preciosVenta.get(i));

                    if (lotes != null && i < lotes.size()) {
                        detalle.setLote(lotes.get(i));
                    }

                    detalles.add(detalle);
                }
            }

            suministro.setDetalles(detalles);

            // ✅ REGISTRAR SUMINISTRO (ACTUALIZA STOCK AUTOMÁTICAMENTE)
            Suministro suministroGuardado = suministroService.registrarSuministro(suministro);

            redirectAttributes.addFlashAttribute("success",
                    "Suministro registrado exitosamente. Stock actualizado automáticamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar suministro: " + e.getMessage());
        }

        return "redirect:/suministros";
    }

    // ✅ DETALLE DE SUMINISTRO
    @GetMapping("/{id}")
    public String verDetalleSuministro(@PathVariable String id, Model model) {
        try {
            Optional<Suministro> suministro = suministroService.obtenerSuministroPorId(id);
            if (suministro.isPresent()) {
                model.addAttribute("suministro", suministro.get());
                return "detalle-suministro";
            } else {
                model.addAttribute("error", "Suministro no encontrado");
                return "redirect:/suministros";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar suministro: " + e.getMessage());
            return "redirect:/suministros";
        }
    }

    // ✅ API PARA SUMINISTROS POR PROVEEDOR
    @GetMapping("/api/proveedor/{codigoProveedor}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarSuministrosPorProveedor(
            @PathVariable String codigoProveedor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Suministro> suministrosPage = suministroService.buscarSuministrosPorProveedor(codigoProveedor, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suministros", suministrosPage.getContent());
            response.put("currentPage", page);
            response.put("totalPages", suministrosPage.getTotalPages());
            response.put("totalItems", suministrosPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}