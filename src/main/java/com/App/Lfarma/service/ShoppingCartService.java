package com.App.Lfarma.service;

import com.App.Lfarma.entity.ItemCarrito;
import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.entity.ShoppingCart;
import com.App.Lfarma.repository.ShoppingCartRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductoService productoService;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository,
                               ProductoService productoService) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.productoService = productoService;
    }

    // Obtiene el carrito del usuario o crea uno nuevo
    public ShoppingCart getOrCreateCart(String usuarioId) {
        Optional<ShoppingCart> opt = shoppingCartRepository.findByUsuario_Id(usuarioId);
        if (opt.isPresent()) return opt.get();

        opt = shoppingCartRepository.findByUsuarioId(usuarioId);
        if (opt.isPresent()) return opt.get();

        ShoppingCart nuevo = new ShoppingCart();
        // Si tu ShoppingCart tiene campo usuarioId en vez de Usuario:
        try {
            // intenta setear usuarioId si existe el campo
            nuevo.getClass().getMethod("setUsuarioId", String.class).invoke(nuevo, usuarioId);
        } catch (Exception ignored) {
            // si no existe, se supone que ShoppingCart tiene Usuario @DBRef y lo manejarás aparte
        }
        nuevo.setItems(new ArrayList<>());
        return shoppingCartRepository.save(nuevo);
    }

    // Agrega un item (o suma cantidad si ya existe)
    public ShoppingCart addItem(String usuarioId, String productoId, int cantidad) {
        Producto producto = productoService.buscarPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ShoppingCart cart = getOrCreateCart(usuarioId);
        List<ItemCarrito> items = cart.getItems();
        if (items == null) {
            items = new ArrayList<>();
            cart.setItems(items);
        }

        boolean updated = false;
        for (ItemCarrito it : items) {
            // comparamos por id del producto (suponiendo getProducto().getId())
            if (it.getProducto() != null && producto.getId().equals(it.getProducto().getId())) {
                it.setCantidad(it.getCantidad() + cantidad);
                it.setPrecioUnitario(producto.getPrecio()); // sincroniza precio
                updated = true;
                break;
            }
        }
        if (!updated) {
            ItemCarrito nuevo = new ItemCarrito();
            nuevo.setProducto(producto);
            nuevo.setCantidad(Math.max(1, cantidad));
            nuevo.setPrecioUnitario(producto.getPrecio());
            items.add(nuevo);
        }
        return shoppingCartRepository.save(cart);
    }

    // Actualiza la cantidad de un item (si cantidad <=0 lo elimina)
    public ShoppingCart updateItem(String usuarioId, String productoId, int cantidad) {
        ShoppingCart cart = getOrCreateCart(usuarioId);
        List<ItemCarrito> items = cart.getItems();
        if (items == null) return cart;

        items.removeIf(it -> {
            if (it.getProducto() != null && productoId.equals(it.getProducto().getId())) {
                // si cantidad <= 0 -> eliminar
                return cantidad <= 0;
            }
            return false;
        });

        for (ItemCarrito it : items) {
            if (it.getProducto() != null && productoId.equals(it.getProducto().getId())) {
                it.setCantidad(cantidad);
                it.setPrecioUnitario(it.getProducto().getPrecio());
                break;
            }
        }
        return shoppingCartRepository.save(cart);
    }

    // Elimina un item del carrito
    public ShoppingCart removeItem(String usuarioId, String productoId) {
        ShoppingCart cart = getOrCreateCart(usuarioId);
        List<ItemCarrito> items = cart.getItems();
        if (items == null) return cart;

        items.removeIf(it -> it.getProducto() != null && productoId.equals(it.getProducto().getId()));
        return shoppingCartRepository.save(cart);
    }

    // Vacía carrito
    public void clearCart(String usuarioId) {
        Optional<ShoppingCart> opt = shoppingCartRepository.findByUsuario_Id(usuarioId);
        if (!opt.isPresent()) opt = shoppingCartRepository.findByUsuarioId(usuarioId);
        if (opt.isPresent()) {
            ShoppingCart cart = opt.get();
            cart.setItems(new ArrayList<>());
            shoppingCartRepository.save(cart);
        }
    }

    // Calcula total (si tu ShoppingCart no tiene método getTotal)
    public double calculateTotal(ShoppingCart cart) {
        if (cart == null || cart.getItems() == null) return 0.0;
        return cart.getItems().stream()
                .mapToDouble(it -> it.getPrecioUnitario() * it.getCantidad())
                .sum();
    }
}
