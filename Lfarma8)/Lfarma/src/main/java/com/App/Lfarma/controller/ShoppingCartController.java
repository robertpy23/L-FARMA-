package com.App.Lfarma.controller;

import org.springframework.security.core.Authentication;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.entity.DetalleFactura;
import com.App.Lfarma.entity.ItemCarrito;
import com.App.Lfarma.entity.ShoppingCart;
import com.App.Lfarma.service.ClienteService;
import com.App.Lfarma.service.FacturaService;
import com.App.Lfarma.service.ProductoService;
import com.App.Lfarma.service.ShoppingCartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/carrito")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final ProductoService productoService;
    private final FacturaService facturaService;
    private final ClienteService clienteService;

    public ShoppingCartController(ShoppingCartService shoppingCartService,
                                  ProductoService productoService,
                                  FacturaService facturaService,
                                  ClienteService clienteService) {
        this.shoppingCartService = shoppingCartService;
        this.productoService = productoService;
        this.facturaService = facturaService;
        this.clienteService = clienteService;
    }

    // Ver carrito (vista Thymeleaf)
    @GetMapping("/{usuarioId}")
    public String verCarrito( Model model,  Authentication authentication) {
        String usuarioId = authentication.getName();
        ShoppingCart cart = shoppingCartService.getOrCreateCart(usuarioId);
        model.addAttribute("carrito", cart);
        model.addAttribute("total", shoppingCartService.calculateTotal(cart));
        // si quieres mostrar lista de productos para agregar desde la misma vista
        model.addAttribute("productos", productoService.listarProductos());
        return "carrito"; // tu plantilla carrito.html
    }

    // Agregar producto al carrito (desde formulario)
    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam String usuarioId,
                                   @RequestParam String productoId,
                                   @RequestParam(defaultValue = "1") int cantidad,
                                   RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.addItem(usuarioId, productoId, cantidad);
            redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo agregar: " + e.getMessage());
        }
        return "redirect:/carrito/" + usuarioId;
    }

    // Actualizar cantidad (form)
    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam String usuarioId,
                                     @RequestParam String productoId,
                                     @RequestParam int cantidad,
                                     RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.updateItem(usuarioId, productoId, cantidad);
            redirectAttributes.addFlashAttribute("success", "Cantidad actualizada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/carrito/" + usuarioId;
    }

    // Eliminar item
    @GetMapping("/eliminar")
    public String eliminarItem(@RequestParam String usuarioId,
                               @RequestParam String productoId,
                               RedirectAttributes redirectAttributes) {
        shoppingCartService.removeItem(usuarioId, productoId);
        redirectAttributes.addFlashAttribute("success", "Item eliminado");
        return "redirect:/carrito/" + usuarioId;
    }

    // Checkout: crear factura a partir del carrito
    @PostMapping("/checkout")
    public String checkout(@RequestParam String usuarioId,
                           @RequestParam String codigoCliente,
                           RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.obtenerClientePorCodigo(codigoCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            ShoppingCart cart = shoppingCartService.getOrCreateCart(usuarioId);
            List<DetalleFactura> detalles = new ArrayList<>();
            if (cart.getItems() != null) {
                for (ItemCarrito it : cart.getItems()) {
                    DetalleFactura d = new DetalleFactura();
                    d.setProducto(it.getProducto());
                    d.setCantidad(it.getCantidad());
                    d.setPrecioUnitario(it.getPrecioUnitario());
                    detalles.add(d);
                }
            }

            // usa el servicio de facturas que ya tienes
            facturaService.crearFactura(cliente, detalles);

            // limpiar carrito
            shoppingCartService.clearCart(usuarioId);

            redirectAttributes.addFlashAttribute("success", "Compra realizada con éxito");
            return "redirect:/facturas"; // o la vista que quieras
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error en checkout: " + e.getMessage());
            return "redirect:/carrito/" + usuarioId;
        }
    }
}